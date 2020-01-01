(ns scterm.widgets.spinner
  (:require [clojure.string :as str]
            [reagent.core :as reagent]
            [scterm.common.utils :as cu]
            [scterm.log :refer [log]]))

(defn inc-or-bounce [max old]
  (-> (inc old)
      (#(if (> % max) 0 %))
      ))

(defn spinner-widget
  [{:keys [title maxdots updating-msecs]
    :or {maxdots 3
         updating-msecs 500}
    :as props}]
  (reagent/with-let [dots-counter (reagent/atom (:maxdots props 3))]
    (let [loading-msg (str "Loading " title)
          ndots @dots-counter
          nspaces (- maxdots ndots)
          text (str loading-msg " "
                    (cu/repeat-chars "." ndots)
                    (cu/repeat-chars " " nspaces))]
      ;; (log "hi!")
      (js/setTimeout #(swap! dots-counter (partial inc-or-bounce maxdots)) updating-msecs)
      [:box (merge props #_{:border :line})
       [:text {:top :center
               :left :center
               :style {:bg :cyan}
               :content text}]
       ])))
