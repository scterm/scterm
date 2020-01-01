(ns scterm.jobinfo.subs
  (:require ["moment" :as moment]
            [blessed :refer (parseTags)]
            [cuerdas.core :as cstr]
            [re-frame.core :refer [reg-sub subscribe]]
            [scterm.jobinfo.utils :as ju]
            [scterm.log :refer [log]]))

(reg-sub
 :info
 (fn [db _] (:info db)))

(reg-sub
 :info/details
 :<- [:info]
 (fn [info _] (:details info)))

(reg-sub
 :info/api-error
 :<- [:info]
 (fn [info _] (:api-error info)))

(reg-sub
 :info/booted
 :<- [:info]
 (fn [info _] (:booted info)))

(reg-sub
 :info/loading
 :<- [:info]
 (fn [info _]
   (contains? #{:init :loading} (:fetch-status info))))

(reg-sub
 :info/summary
 :<- [:info/details]
 (fn [details _] (ju/format-job-summary (js->clj details))))

(reg-sub
 :info/finished
 :<- [:info/details]
 (fn [details _] (ju/job-finished? details)))
