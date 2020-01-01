(ns server
  "Fake server to generate data for integration tests."
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [io.pedestal.http :as http]
            [io.pedestal.interceptor.helpers :as interceptor :refer [defbefore]]
            [io.pedestal.log :as log]
            [clojure.core.async :as async]
            [clojure.java.io :as io]
            [scterm.common.test-utils :as test.common]
            [scterm.common.utils :as cu]
            [io.pedestal.http.route :as http.route]))

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def log-request
  "Same as io.pedestal.http.log-request except it also shows the query
  params of the request."
  (interceptor/on-request
    ::http/log-request
    (fn [request]
      (log/info :msg (format "%s %s"
                             (clojure.string/upper-case (name (:request-method request)))
                             (str (:uri request)
                                  (when-let [query (:query-string request)]
                                    (str "?" query)))))
      (log/meter ::request)
      request)))

(def ok             (partial response 200))
(def not-authorized (partial response 401))
(def forbidden      (partial response 403))
(def internal-error (partial response 500))
(def not-found      (partial response 404))

(def running-job-info
  "Typical jobs api response."
  (-> (io/resource "files/info.json") slurp json/parse-string))

(def running-job-stats
  (-> (io/resource "files/stats.json") slurp json/parse-string))

(defn job-info [request]
  (let [apikey (-> request :query-params :apikey)
        job-id (-> request :path-params :job)]
    (def vrequest request)
    (cond
      (not= apikey test.common/correct-api-key)
      (not-authorized {:error "not-authorized"})

      (= job-id test.common/running-job-id)
      (ok running-job-info)

      (= job-id test.common/finished-job-id)
      (-> running-job-info
          (assoc "state" "finished"
                 "close_reason" "finished"
                 "finished_time" (+ 36000 (get running-job-info "running_time")))
          ok)

      (= job-id test.common/error-job-id)
      (internal-error {:error "internal error"})

      ;; Emulate the HS behavior that non-existing job would still get an 200
      :else
      (ok {}))))

(defn job-logs-stats [request]
  (let [apikey (-> request :query-params :apikey)
        job-id (->> (-> request :path-params)
                    ((juxt :project :spider :job))
                    (str/join "/"))]
    (def vrequest-stats request)
    (cond
      (not= apikey test.common/correct-api-key)
      (not-authorized {:error "not-authorized"})

      (or (= job-id test.common/running-job-id)
          (= job-id test.common/finished-job-id))
      (ok running-job-stats)

      (= job-id test.common/error-job-id)
      (internal-error {:error "internal error"})

      ;; Emulate the HS behavior that non-existing job would still get an 200
      :else
      (ok {}))))

(def running-job-logs (into [] (cu/load-logs-file "src/main/scterm/logs/logs.txt")))

(def logs-count 30)

(defn job-logs [request]
  (def vrequest1 request)
  (let [apikey (-> request :query-params :apikey)
        start  (-> request :query-params :start)
        start (if (str/blank? start)
                0
                (-> start
                    (str/split #"/")
                    last
                    Integer/parseInt))
        job-id (->> (-> request :path-params)
                    ((juxt :project :spider :job))
                    (str/join "/"))]
    (log/info :msg (format "job = %s, start = %s" job-id start))
    (cond
      (>= start logs-count)
      (ok [])

      (not= apikey test.common/correct-api-key)
      (not-authorized {:error "not-authorized"})

      (= job-id test.common/running-job-id)
      (async/go
        (async/<! (async/timeout 100))
        (ok running-job-logs))

      (= job-id test.common/finished-job-id)
      (async/go
        (async/<! (async/timeout 1000))
        (ok running-job-logs))

      (= job-id test.common/error-job-id)
      (internal-error {:error "internal error"})

      ;; Emulate the HS behavior that non-existing job would still get
      ;; an empty array
      :else
      (ok []))))

(defn- channel? [c] (instance? clojure.core.async.impl.protocols.Channel c))

#_(defbefore job-logs-async
  [{:keys [request] :as ctx}]
  (let [resp (job-logs request)]
    (if (channel? resp)
      (async/go
        (let [response (async/<! resp)]
          (assoc ctx :response response)))
      (do
        (assoc ctx :response resp)))))

(def job-logs-async
  {:name :job-logs-async
   :leave
   (fn
    [{:keys [request] :as ctx}]
    (let [resp (job-logs request)]
      (if (channel? resp)
        (async/go
          (let [response (async/<! resp)]
            (assoc ctx :response response)))
        (assoc ctx :response resp))))})

(defn echo [request]
  (def vr request)
  (-> request
      (select-keys [:async-supported?
                    :context-path
                    :headers
                    :path-info
                    :path-params
                    :protocol
                    :query-params
                    :query-string
                    :remote-addr
                    :request-method
                    :scheme
                    :server-name
                    :server-port
                    :uri])
      ok))

(defbefore echo-async
  [{:keys [request] :as ctx}]
  (let [resp (echo request)]
    (async/go
      (async/<! (async/timeout 10000))
      (assoc ctx :response resp))))

(def server-routes
  #{
    ["/jobs/*job"
     :get [http/json-body `job-info]
     :route-name :job-info
     :constraints {:job #"\d+/\d+/\d+"}]
    ["/logs/:project/:spider/:job/stats"
     :get [http/json-body `job-logs-stats]
     :route-name :job-logs-stats
     :constraints {:project #"\d+"
                   :spider #"\d+"
                   :job #"\d+"}]
    ["/logs/:project/:spider/:job"
     :get [http/json-body `job-logs-async]
     :route-name :job-logs-async
     :constraints {:project #"\d+"
                   :spider #"\d+"
                   :job #"\d+"}]
    #_["/logs/*job"
     :get [http/json-body `job-logs-async]
     :route-name :job-logs
     :constraints {:job #"\d+/\d+/\d+"}]
    ["/logs-sync/*job"
     :get [http/json-body `job-logs]
     :route-name :job-logs-sync
     :constraints {:job #"\d+/\d+/\d+"}]
    ["/echo/*path"
     :get [http/json-body `echo]
     :route-name :echo]
    ["/echo-async/*path"
     :get [http/json-body `echo-async]
     :route-name :echo-async]})

(defn- create-routes []
  ;; Reload route on every request. This way we'll see the changes
  ;; immediately after evaluating it.
  (fn []
    ;; (println "reloading routes")
    (http.route/expand-routes (var-get #'server-routes))
    ))

(defn- create-server! [routes]
  (let [host "127.0.0.1"
        port 18000]
    (http/create-server
     {::http/routes     routes
      ::http/type       :jetty
      ::http/host       host
      ::http/port       port
      ::http/join?      false
      ::http/request-logger #'log-request
      ;; Make the thread pool use daemon threads. Otherwise the
      ;; program won't quit when the port is occupied.
      ::http/container-options {:daemon? true}})))

(defonce server-ref (atom nil))

(defn start!
  "Start the server in fthe "
  []
  (reset! server-ref
        (-> (create-routes)
            (create-server!)
            http/start))
  nil)

(defn stop!
  "Start the server in fthe "
  []
  (when-some [server @server-ref]
    (http/stop server))
  nil)

(comment

  (do
    (stop!)
    (start!))


  (async/<!!
   (async/go
     (async/<! (async/timeout 2000))
     100))

  (println 100)

  (-> (io/resource "stats.json") slurp json/parse-string)
  )
