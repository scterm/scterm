(ns scterm.preloads
  (:require ["ws" :as ws]))

(defn- monkey-patch-for-react-devtools []
  (let [defineProperty (.-defineProperty js/Object)]
    (defineProperty js/global "WebSocket" #js {:value ws})
    (defineProperty js/global "window" #js {:value js/global})))

(defn- preload []
  (monkey-patch-for-react-devtools))

(defonce _ (preload))
