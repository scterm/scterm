(ns scterm.logs.subs
  (:require [cuerdas.core :as cstr]
            [re-frame.core :as rf :refer [dispatch reg-sub]]
            [scterm.log :refer [log]]
            [scterm.common.utils :as cu]
            [scterm.logs.utils :as lu]))

#_(defonce sample-logs
  (->> (cu/load-logs-file "src/main/scterm/logs/logs.txt")
       (take 3)))

(reg-sub
 :logs
 (fn [db _] (:logs db)))

(reg-sub
 :logs/fetching
 :<- [:logs]
 (fn [logs _]
   (:fetching logs)))

;; TODO: simply this as a function
;; (reg-simple-sub :logs/booted)
(reg-sub
 :logs/booted
 :<- [:logs]
 (fn [logs _]
   (:booted logs)))

(reg-sub
 :logs/entries
 :<- [:logs]
 (fn [logs _]
   (if-let [entries (:entries logs)]
     {:ready? true
      :entries entries}
     (do
       (dispatch [:logs/maybe-fetch])
       {:ready? false}))))

(reg-sub
 :logs/scroll
 :<- [:logs]
 (fn [logs _]
   (:scroll logs)))

(reg-sub
 :logs/api-error
 :<- [:logs]
 (fn [logs _]
   (:api-error logs)))
