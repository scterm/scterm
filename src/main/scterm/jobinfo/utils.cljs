(ns scterm.jobinfo.utils
  (:require ["moment" :as moment]
            ["fs" :as fs]
            [ajax.core :as ajax]
            [clojure.string :as str]
            [cuerdas.core :as cstr]
            [re-frame.core :refer [dispatch reg-event-fx]]
            [scterm.config :as conf]
            [scterm.log :refer [log]]
            [scterm.utils :as u]
            [scterm.jobinfo.cache :as cache]
            [better-cond.core :as b :include-macros true]
            [scterm.utils.format :refer [format-kvs-as-table]]))

;; TODO: move to utils?
(defn format-time
  [ts]
  (if (nil? ts)
    "N/A"
    (let [date (.utc ^js moment ts)]
      (if (.isValid date) (.format date "YYYY-MM-DD HH:mm:ss UTC") ts))))

(defn format-spider-args
  [args]
  (->> (map (fn [[k v]] (cstr/format "-a %s=%s" (name k) v)) args)
       (str/join " ")))

(defn format-runtime [running_time finished_time]
  (let [delta (-> (if finished_time
                    (moment finished_time)
                    (moment))
                  (.diff (moment running_time)))]
    (u/readable-time-delta (/ delta 1000))))

(def jobattr->display-fn
  ;; Here we use array map to keep the display order.
  (array-map :spider nil
             :spider_args format-spider-args
             :version nil
             :units nil
             :state nil
             :close_reason (fnil str "N/A")
             :errors (fnil str 0)
             :pending_time ["Schedule time" format-time]
             :running_time ["Start time" format-time]
             :finished_time ["Finish time" format-time]))

(defn get-runtime [jobinfo]
  (if-let [running_time (:running_time jobinfo)]
    (format-runtime running_time (:finished_time jobinfo))
    0
    ))

(defn get-derived-kvs [jobinfo]
  [["Runtime" (get-runtime jobinfo)]])

(defn get-api-kvs [attr->display-fn jobinfo]
  ;; Do not use medely m/map-kv since it gives back a map and thus
  ;; loses the order of keys.
  (map
   (fn [[attr display-fn]]
     (let [default-name (-> attr
                            u/keyword->str
                            str/capitalize)
           [attr-name value-fn] (cond
                                  (nil? display-fn)
                                  [default-name identity]

                                  (vector? display-fn)
                                  (take 2 display-fn)

                                  :else
                                  [default-name display-fn])]
       [attr-name
        (value-fn (attr jobinfo))]))
   attr->display-fn))

(defn get-kvs [attr->display-fn jobinfo]
  (concat (get-api-kvs attr->display-fn jobinfo)
          (get-derived-kvs jobinfo)))

#_(get-kvs jobattr->display-fn vd)

(defn format-job-summary
  [job-details]
  (def vd job-details)
  (let [kvs (get-kvs jobattr->display-fn job-details)]
    (def vkvs kvs)
    (format-kvs-as-table kvs)))

(defn job-info-request []
  (let [job-id conf/*job-id*
        api-key conf/*api-key*
        url (u/urljoin conf/*hs-api-url* "/jobs/" job-id)]
    #_(log "job info: fetching from server %s" conf/*hs-api-url*)
    {:method :get
     :uri url
     :params {:apikey api-key
              :add_summary "1"}
     :headers {"User-Agent" "SCTerm"}
     :response-format (ajax/raw-response-format {:keywords? false})
     :on-success [::on-job-info-success job-id]
     :on-failure [::on-job-info-failure job-id]}))

(defn job-finished? [info]
  (let [get-state (fn [o]
                    (if (map? o)
                      (:state o)
                      (.-state ^js o)))]
    (some-> info
            get-state
            #{"deleted" "finished"})))

(defn on-empty-response [job-id body]
  (log "empty response, does job %s really exist?" job-id)
  (let [fail-msg (cstr/format "Empty response, job %s seems does not exist" job-id)]
    {:dispatch-n (list [:info/fetch-status-update :failed]
                       [:info/fail fail-msg {:response body}])}))

(reg-event-fx
 ::on-job-info-success
 (fn [_ [_ job-id body]]
   #_(log "body = %s" body)
   (let  [info (cond
                 (str/blank? body)
                 nil

                 (= body "{}")
                 nil

                 :else
                 (u/json-loads body))]
     (if-not info
       (on-empty-response job-id body)
       (merge {:dispatch-n (list [:info/fetch-status-update :loaded]
                                 [:info/success info])}
              (when (job-finished? info)
                {:cache/write-cached-job-info {:job-id job-id
                                               :body body}}))))))

(u/reg-event-fx-noop
 ::on-job-info-failure
 (fn [_ [_ job-id result]]
   (log "http error for %s : %s" job-id (str result))
   (dispatch [:info/fetch-status-update :failed])
   (dispatch [:info/fail (u/format-http-error result) result])))

(defn job-id->spider-id [job-id]
  (->> (clojure.string/split job-id "/")
       (take 2)
       (clojure.string/join "/")))

#_(job-id->spider-id "1/2/3")


(defn debug-not= [_ a b]
  ;; (log "%s %s => %s" v a b)
  (not= a b))

(defn check-counter-updates
  "Compare the counters and detect any updated counters so they can be
  animated."
  [old new]
  #_(log "check-counter-updates called")
  (when old
    (let [changed
          (doall (for [kind [:items :pages :logs]
                       :when (debug-not= kind (kind old) (kind new))]
                   kind))]
      ;; (log "changed = %s" changed)
      (when (not-empty changed)
        {:dispatch [:nav/counters-updated changed]}))))
