(ns scterm.jobinfo.events
  (:require [cuerdas.core :as cstr]
            [re-frame.core :as re-frame :refer
             [reg-event-db reg-event-fx inject-cofx path after dispatch
              reg-cofx inject-cofx reg-fx subscribe]]
            [scterm.db :refer [default-db]]
            [scterm.jobinfo.utils :as ju]
            [scterm.log :refer [log]]
            [user]
            [scterm.config]
            [scterm.db :refer [screen]]
            [scterm.utils :as u]))

(def info-path [(path :info)])

(u/deftimer jobinfo-update :info/maybe-update 3000)

(reg-event-db
 :info/maybe-update
 (fn [db _]
   (let [info (:info db)]
     (if user/*fake-fetch*
       (do #_(log "use fake fetch")
           (dispatch [:info/success (user/gen-fake-info (:details info))]))
       (when (and (not (ju/job-finished? (:details info)))
                  #_(not @(subscribe [:error]))
                  (not= (:fetch-status info) :loading))
         (dispatch [:info/fetch]))))
   db))

(reg-event-fx
 :info/fetch-status-update
 info-path
 (fn [{info :db} [_ status]]
   {:dispatch [:screen/render]
    :db (assoc info :fetch-status status)}))

(reg-event-fx
 :info/fetch
 [(inject-cofx :job-info-cache)]
 (fn [{:keys [job-info-cache db]} _]
   (if job-info-cache
     (do (log "job info: use cached response")
         {:dispatch-n
          (list [:info/success job-info-cache]
                [:info/fetch-status-update :loaded])})
     (do
       #_(log "job-info-cache = %s" job-info-cache)
       {:dispatch [:info/fetch-status-update :loading]
        :http-xhrio (ju/job-info-request)}))))


(reg-event-fx
 :info/success
 info-path
 (fn [{info :db} [_ details]]
   (def vdetails details)
   ;; (log ":info/success called with %s" (str details))
   (let [details (js->clj details :keywordize-keys true)]
     (merge {:db (merge info {:api-error nil
                              :api-raw-error nil
                              :booted true
                              :details details})}
            (ju/check-counter-updates (:details info) details)))))

(reg-event-db
 :info/fail
 info-path
 (fn [info [_ error-details raw-error]]
   (merge info {:api-error error-details
                :api-raw-error raw-error})))
