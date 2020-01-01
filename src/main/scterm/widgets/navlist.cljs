(ns scterm.widgets.navlist
  "The standard List widget provided by blessed does not support speficy
  a non-zero default selected item. We have to implement one from
  scratch."
  (:require [reagent.core :as reagent]
            [scterm.log :refer [log]]
            [scterm.utils :refer [maybe-cycle]]
            ))

(defn make-list-item [active-index index item]
  ;; (log "active = %s, index = %s" active index)
  (let [active (= active-index index)
        style (if active
                {:fg :white
                 :bg :blue
                 :border {:fg :green}
                 }
                {:fg :none
                 :bg :none
                 :border {:fg :none}
                 }
                )
        text (str " " item)]
    [:box (reagent/merge-props
            {:key (str index)
             :width 20
             :align :center
             :valign :middle
             :height 3
             :name :list-item
             :style style
             :content text
             :border :line
             }
            #_(when active
              {:border :line}
              )
            )]))

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

(defn navlist-widget
  [{:keys [items active] :or {active 0} :as props}]
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
         :name :list-widget}
        (dissoc props :items))
       item-boxes])))
