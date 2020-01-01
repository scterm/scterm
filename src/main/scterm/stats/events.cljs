(ns scterm.stats.events
  (:require [cuerdas.core :as cstr]
            [re-frame.core :as re-frame :refer [reg-event-db]]
            [scterm.db :refer [default-db screen]]
            [scterm.log :refer [log]]
            [scterm.kbd-utils :as kbd]
            [scterm.utils :as u]))

(def filtering-text-path [:stats :filtering-text])
(def filtering-path [:stats :filtering])

(defn handle-stats-keypress [db keyobj]
  (let [key (kbd/get-char keyobj)
        filtering? (get-in db filtering-path)
        set-filtering #(assoc-in %1 filtering-path %2)
        update-filtering-text #(update-in %1 filtering-text-path %2)]
    #_(log "stats: handle-keypress for key %s" key)
    ;; (def vkey key)
    (if-not filtering?
      (cond-> db
        (= key "/")
        (set-filtering true))
      (let [turn-off-filtering-if-empty (fn [db]
                (cond-> db
                  (empty? (get-in db filtering-text-path))
                  (set-filtering false)))]
       (cond
         (kbd/text? key)
         (update-filtering-text db #(str % key))

         (= key "backspace")
         (-> db
             ;; "filtering off" check must be performed before
             ;; update-filtering-text because we only turn off the filtering
             ;; mode when backspace is pressed after filtering text becomes
             ;; empty.
             (turn-off-filtering-if-empty)
             (update-filtering-text #(cstr/slice % 0 -1)))
         :else
         db
         )))
    ))

(reg-event-db
 :stats/keypress
 (fn [db [_ keyobj]]
   (handle-stats-keypress db keyobj)))
