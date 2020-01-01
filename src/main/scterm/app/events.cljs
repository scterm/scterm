(ns scterm.app.events
  (:require [cuerdas.core :as cstr]
            [re-frame.core :as re-frame :refer
             [reg-event-db reg-event-fx inject-cofx path after dispatch
              reg-cofx inject-cofx reg-fx]]
            [scterm.log :refer [log]]
            [scterm.utils :as u]
            [scterm.config :as conf]
            [scterm.db :refer [screen]]
            [scterm.db :refer [default-db]]))

(reg-event-fx
 :initialize
 (fn [{:keys [db]} [_ {:keys [api-key job-id routes test] :as args}]]
   (log "initialize with %s"
        (-> args
            (assoc :routes "...")
            (u/maybe-truncate :api-key 7)
            str))
   (set! conf/*job-id* job-id)
   (set! conf/*api-key* api-key)
   {:dispatch-n
    (list
     [:routing/init routes]
     [:info/fetch]
     )
    :db (-> default-db
            (merge
             {:api-key api-key
              :test test
              :job-id job-id}))}))

;; sometimes we need to manually trigger a render, maybe a blessed
;; bug?
(reg-event-db
 :screen/render
 (fn [db & _]
   ;; (log "noop!")
   (when-some [screen @screen]
     (js/setTimeout #(.render ^js screen) 1000))
   db
   ))

;; sometimes we need to manually trigger a render, maybe a blessed
;; bug?
(reg-event-db
 :screen/resized
 (fn [db [_ w h]]
   (log "screen resized to %sx%s" w h)
   (assoc db :screen {:width w :height h})))
