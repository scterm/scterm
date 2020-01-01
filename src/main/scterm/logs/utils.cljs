(ns scterm.logs.utils
  (:require [ajax.core :as ajax]
            [re-frame.core :refer [dispatch reg-event-fx]]
            [scterm.config :as conf]
            [scterm.log :refer [log]]
            [scterm.utils :as u]
            [scterm.utils.format :refer [format-kvs-as-table max-key-length]]))

(defn fetch-logs-request [{:keys [start]}]
  (let [job-id conf/*job-id*
        api-key conf/*api-key*
        url (u/urljoin conf/*hs-api-url* "/logs/" job-id)]
    #_(log "logs: fetching from %s" url)
    {:method :get
     :uri url
     :params {:apikey api-key
              :format "json"
              :count "50"
              :start (str job-id "/" start)}
     :headers {"User-Agent" "SCTerm"}
     :response-format (ajax/raw-response-format {:keywords? false})
     :on-success [::on-fetch-logs-success job-id]
     :on-failure [::on-fetch-logs-failure job-id]}))

(reg-event-fx
 ::on-fetch-logs-success
 (fn [_ [_ job-id body]]
   #_(log "body = %s" body)
   (let  [logs (u/json-loads body)]
     {:dispatch-n (list [:logs/is-fetching false]
                        [:logs/fetch-success logs])})))

(u/reg-event-fx-noop
 ::on-fetch-logs-failure
 (fn [_ [_ job-id result]]
   (log "http error for %s : %s" job-id (str result))
   (dispatch [:logs/is-fetching false])
   (dispatch [:logs/fetch-fail (u/format-http-error result) result])))

(defn fetch-logs-stats-request []
  (let [job-id conf/*job-id*
        api-key conf/*api-key*
        url (u/urljoin conf/*hs-api-url* "/logs/" job-id "stats")]
    #_(log "logs: fetching from %s" url)
    {:method :get
     :uri url
     :params {:apikey api-key
              :format "json"}
     :headers {"User-Agent" "SCTerm"}
     :response-format (ajax/raw-response-format {:keywords? false})
     :on-success [::on-fetch-logs-stats-success job-id]
     :on-failure [::on-fetch-logs-stats-failure job-id]}))

(reg-event-fx
 ::on-fetch-logs-stats-success
 (fn [_ [_ job-id body]]
   #_(log "body = %s" body)
   (let  [stats ^js (u/json-loads body)]
     {:dispatch-n (list [:logs/stats-is-fetching false]
                        [:logs/stats-fetch-success (js->clj stats :keywordize-keys true)])})))

(u/reg-event-fx-noop
 ::on-fetch-logs-stats-failure
 (fn [_ [_ job-id result]]
   (log "stats http error for %s : %s" job-id (str result))
   (dispatch [:logs/stats-is-fetching false])
   (dispatch [:logs/stats-fetch-fail (u/format-http-error result) result])))

(comment
  ())
