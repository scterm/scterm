(ns scterm.widgets.list
  "The standard List widget provided by blessed does not support speficy
  a non-zero default selected item. We have to implement one from
  scratch."
  (:require [reagent.core :as reagent]
            [scterm.log :refer [log]]
            [better-cond.core :as b :include-macros true]
            [scterm.utils :refer [maybe-cycle]]
            ))

(defn make-list-item [active-index index item]
  ;; (log "active = %s, index = %s" active index)
  (let [style (if (= active-index index)
                {:fg :white
                 :bg :blue}
                {:fg :none
                 :bg :none}
                )
        text (str (if (= active-index index) " > " "   ") item)]
    [:text {:key (str index)
            :width "100%-2"
            :height 1
            :name :list-item
            :style style
            :content text
           }]))

(defn update-active-item [state f cb]
  (let [max-index (dec (:nitems @state))
        new-index (-> (:active @state)
                      f
                      (maybe-cycle max-index))]
    ;; (log "list-widget: new index is %s" new-index)
    (def vs state)
    (reagent/rswap! state assoc :active new-index)
    (when cb
      (let [active-index (:active @state)
            active-item (nth (:items @state) active-index)]
        (cb active-item active-index)))))

(defn handle-keypress [shortcuts on-navigate on-select state keyobj]
  (let [key (:name keyobj)]
    ;; (log "handle-keypress for key %s!" key)
    (def vstate @state)
    (b/cond
      (= key "up")
      (update-active-item state dec on-navigate)

      (= key "down")
      (update-active-item state inc on-navigate)

      (= key "right")
      (when on-select
                (let [active-index (:active @state)
                      active-item (nth (:items @state) active-index)]
                  (on-select active-item active-index)))

      :let [shortcut-fn (get shortcuts key)]
      shortcut-fn
      (shortcut-fn))))

(defn list-widget
  [{:keys [items active on-navigate on-select shortcuts] :or {active 0} :as props}]
  (reagent/with-let [state (reagent/atom {:active 0})]
    (let [item-boxes (map-indexed (fn [index item]
                                    (make-list-item active index item))
                                  items)]
      (swap! state assoc
             :nitems (count items)
             :active active
             :items items)
      [:layout
       (merge
        {:width "100%"
         :height "100%"
         :name :list-widget
         :on-keypress (fn [_ keyobj]
                        (handle-keypress shortcuts on-navigate on-select state
                                          (js->clj keyobj :keywordize-keys true)))}
        (dissoc props :items))
       item-boxes])))
