(ns scterm.nav.events
  (:require [cuerdas.core :as cstr]
            [re-frame.core :as re-frame :refer
             [reg-event-db reg-event-fx inject-cofx path after dispatch
              reg-cofx inject-cofx reg-fx]]
            [medley.core :as m]
            [scterm.log :refer [log]]
            [scterm.utils :as u]
            [scterm.db :refer [screen default-db]]
            [scterm.nav.anims :as anims]
            [scterm.app.tabs :as tabs]))

(def nav-path [(path :nav)])

(reg-event-fx
 :nav/set-active-tab-index
 (fn [{:keys [db]} [_ tab-index]]
   ;; (log "active tab changed from %s to %s" (get-in db [:nav :active-tab-index]) tab-index)
   {:db (assoc-in db [:nav :active-tab-index] tab-index)}))

(reg-event-db
 :nav/set-active-view
 nav-path
 (fn [nav [_ [iview args]]]
   (assoc nav :active-view [iview args])))

(reg-event-db
 :nav/counters-updated
 nav-path
 (fn [nav [_ changed]]
   ;; For changed counters, reset their animation status so they can animate.
   (update nav :anims merge (zipmap changed (repeat 0)))))

(reg-event-db
 :nav/update-anims
 nav-path
 (fn [nav]
   ;; (log "nav/update-anims triggered")
   (update nav :anims anims/update-anims)))

(reg-event-db
 :nav/prev-tab
 (fn [db [_]]
   (let [current-tab (get-in db [:nav :active-tab-index] 0)
         new-active-tab (tabs/prev-index current-tab)
         path (tabs/index->path new-active-tab)]
     (dispatch [:routing/push-state path])
     (dispatch [:nav/set-active-tab-index new-active-tab]))
   db))

(reg-event-db
 :nav/next-tab
 (fn [db [_]]
   #_(log "next-tab is called")
   (let [current-tab (get-in db [:nav :active-tab-index] 0)
         new-active-tab (tabs/next-index current-tab)
         path (tabs/index->path new-active-tab)]
     #_(log "path => %s, active-tab => %s" path new-active-tab)
     (dispatch [:routing/push-state path])
     (dispatch [:nav/set-active-tab-index new-active-tab]))
   db))
