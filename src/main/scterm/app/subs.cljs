(ns scterm.app.subs
  (:require [re-frame.core :refer [reg-sub]]
            #_[scterm.log :refer [log]]))

(reg-sub
 :job-id
 (fn [db _] (:job-id db)))

(reg-sub
 :error
 (fn [db _]
   (:error db)))

(reg-sub
 :screen/height
 (fn [db _]
   (get-in db [:screen :height])))
