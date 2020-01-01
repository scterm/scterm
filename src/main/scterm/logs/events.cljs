(ns scterm.logs.events
  (:require [cuerdas.core :as cstr]
            [re-frame.core :as re-frame :refer [reg-event-db path dispatch reg-event-fx subscribe]]
            [scterm.db :refer [default-db screen]]
            [scterm.log :refer [log]]
            [scterm.logs.utils :as lu]
            [scterm.kbd-utils :as kbd]
            [scterm.utils :as u]))

(def logs-path [(path :logs)])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; logs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn has-new-entries [logs]
  (let [total (or (some-> logs :stats :details :totals :input_values) -1)
        current (-> logs :entries count)]
    (cond
      (neg? total)
      ;; stats api not fetched yet
      true

      :else
      (< current total))))

(reg-event-db
 :logs/maybe-fetch
 logs-path
 (fn [{:keys [fetching] :as logs} _]
   #_(log "maybe/fetch-status = %s" fetch-status)
   (when (and (not fetching)
              (has-new-entries logs))
     (dispatch [:logs/fetch]))
   logs))

(reg-event-db
 :logs/is-fetching
 logs-path
 (fn [logs [_ fetching]]
   #_(log "fetch status %s => %s" (:fetching logs) fetching)
   (assoc logs :fetching fetching)))

(reg-event-fx
 :logs/fetch
 (fn [{:keys [db]}  _]
   {:dispatch [:logs/is-fetching true]
    :http-xhrio (lu/fetch-logs-request {:start (-> db :logs :entries count)})}))

(reg-event-db
 :logs/fetch-success
 logs-path
 (fn [logs  [_ entries]]
   (let [entries (js->clj entries :keywordize-keys true)
         current-entries (:entries logs)]
     (when (and (seq current-entries)
                (seq entries))
       ;; Scroll a bit to notify the user that new entries has
       ;; arrived.
       ;; TODO: use :dispatch-later?
       (js/setTimeout #(dispatch [:logs/post-loading-scroll]) 500))
     (merge logs {:api-error nil
                  :api-raw-error nil
                  :booted true
                  :entries (concat current-entries entries)}))))

(reg-event-db
 :logs/fetch-fail
 logs-path
 (fn [logs [_ error-details raw-error]]
   #_(log "fetch-fail called!")
   (merge logs
          {:api-error error-details
           :api-raw-error raw-error})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; logs element scrolling
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Save the scroll position so we can restore it when nav back to the
;; logs tab.
(reg-event-db
 :logs/update-scroll
 logs-path
 (fn [logs [_ {:keys [base percent]}]]
   (when (and (< (:percent logs) 100)
              (>= percent 100))
     #_(log "percent = %s" percent)
     (dispatch [:logs/maybe-fetch]))
   (update logs :scroll assoc :base base :percent percent)))

(reg-event-db
 :logs/ref
 logs-path
 (fn [logs [_ el]]
   (assoc logs :el el)))

(reg-event-db
 :logs/post-loading-scroll
 logs-path
 (fn [logs _]
   ;; scroll logs down a bit when more entries are loaded
   (when-let [el (:el logs)]
     (.scroll el 3))
   logs))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; logs stats
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(u/deftimer logs-refresh :logs/maybe-refresh 3000)

(defn refresh-logs-stats [logs]
  #_(log "refresh-logs-stats called")
  (let [fetch-stats? (cond
                       (get-in logs [:stats :fetching])
                       false

                       (nil? (get-in logs [:stats :details]))
                       true

                       @(subscribe [:info/finished])
                       false)]
    (when fetch-stats?
      (dispatch [:logs/fetch-stats]))
    logs))

(reg-event-fx
 :logs/fetch-stats
 (fn [{:keys [db]}  _]
   #_(log "fetching logs stats")
   {:dispatch [:logs/stats-is-fetching true]
    :http-xhrio (lu/fetch-logs-stats-request)}))

(reg-event-db
 :logs/stats-is-fetching
 logs-path
 (fn [logs [_ fetching]]
   (update logs :stats assoc :fetching fetching)))

(reg-event-db
 :logs/stats-fetch-success
 logs-path
 (fn [logs [_ stats]]
   (update logs :stats merge {:details stats
                              :api-error nil
                              :api-raw-error nil})))

(reg-event-db
 :logs/stats-fetch-fail
 logs-path
 (fn [logs [_ error-details raw-error]]
   #_(log "fetch-fail called!")
   (update logs :stats merge {:api-error error-details
                              :api-raw-error raw-error})))

(reg-event-db
 :logs/maybe-refresh
 logs-path
 (fn [logs _]
   (refresh-logs-stats logs)))
