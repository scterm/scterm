(ns scterm.debug.events
  (:require [re-frame.core :refer [path reg-event-db dispatch]]
            [scterm.log :refer [log]]
            [scterm.utils :as u]))

(def debug-path [(path :debug)])

(reg-event-db
 :debug/toggle-fullscreen
 debug-path
 (fn [debug _]
   ;; (log "debug: toggle fullscreen ")
   (dispatch [:screen/render])
   (update debug :fullscreen not)))
