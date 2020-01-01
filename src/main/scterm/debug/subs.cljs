(ns scterm.debug.subs
  (:require [re-frame.core :refer [reg-sub]]
            [scterm.utils :as u]))

(reg-sub
 :debug
 (fn [db _] (:debug db)))

(reg-sub
 :debug/db
 (fn [db _]
   (-> db
       (u/maybe-truncate [:info :details] 100)
       (u/maybe-truncate [:api-key] 2)
       (update-in [:logs :entries] count))))

(reg-sub
 :debug/fullscreen
 :<- [:debug]
 (fn [debug _]
   (:fullscreen debug)))
