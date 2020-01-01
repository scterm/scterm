(ns scterm.debug.views
  (:require [re-frame.core :refer [subscribe reg-sub reg-event-db path]]
            [scterm.utils :as u]
            [scterm.debug.events]
            [scterm.debug.logger :as l]
            [scterm.debug.subs]
            [scterm.styles :refer [focused-style]]
            [reagent.core :as reagent :refer [merge-props]]))

(defn log-box [n]
  [:text#log
   (merge-props
    #_(when @(subscribe [:debug/fullscreen])
      focused-style
      )
    {:top        0
     :right      0
     :width      "50%-1"
     :height     n
     :style      {:fg :yellow :bg :grey}
     :scrollable true
     :alwaysScroll true
     :content    (->> (take-last n @l/logger)
                      (clojure.string/join "\n"))})])

(defn debug-box [{:keys [lines]
                  :or {lines l/max-logs}
                  :as props}]
  #_[:box (merge-props props {:content "hello"})]
  [:text (merge-props
                {:style  {:border {:fg :yellow}}
                 :border {:type :line}
                 :label  "Debug info"}
                props)
   [:text {:width   "50%-2"
           :content (str @(subscribe [:debug/db]))
           }]
   [log-box (- lines 2)]])
