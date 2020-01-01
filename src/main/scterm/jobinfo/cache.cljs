(ns scterm.jobinfo.cache
  (:require [re-frame.core :as rf :refer [reg-cofx reg-fx]]
            [clojure.string :as str]
            [cuerdas.core :as cstr]
            [scterm.config :as conf]
            ["fs" :as fs]
            [cljs-node-io.core :as io]
            [scterm.utils :as u]
            [scterm.log :refer [log]]))

;; (set! (.. js/process -env -NC) "1")
(defn cache-enabled? []
  (and
   conf/*use-cache*
   (not (when-some [env (some u/read-env ["NC" "NOCACHE"])]
          (-> env
              clojure.string/upper-case
              #{"1" "YES" "ON"}
              boolean)))))


(defn get-cache-dir "The directory to put all the cache files"
  []
  (u/urljoin conf/*cache-dir* "scterm-cache"))

(defn get-job-cache-file [job-id type]
  (let [canon-job-id (str/replace job-id "/" "-")
        cache-dir (get-cache-dir)]
    (u/ensure-dir cache-dir)
    (cstr/format "%s/%s.%s.cache.json" cache-dir canon-job-id type)))

(defn get-cached-job-info [job-id]
  (let [cache-file (get-job-cache-file job-id "info")]
    #_(log "cache-file = %s" cache-file)
    (if (.existsSync fs cache-file)
      (do
        #_(log "cache file %s found!" cache-file)
        (-> (io/slurp cache-file)
            u/json-loads))
      (do #_(log "cache file %s not found" cache-file)
          nil))))

(defn write-cached-job-info [job-id content]
  (let [cache-file (get-job-cache-file job-id "info")]
    ;; (log "writing cached job info file %s" cache-file)
    (io/spit cache-file content)))

(reg-cofx
 :job-info-cache
 (fn [{:keys [db] :as cofx} _]
   (assoc cofx
          :job-info-cache
          (when (cache-enabled?)
            #_(log "cache is enabled")
            (get-cached-job-info (:job-id db))))))

(reg-fx
 :cache/write-cached-job-info
 (fn [{:keys [job-id body]}]
   (write-cached-job-info job-id body)))
