(ns scterm.react-devtools
  "Connect to standalone react devtools. Used as an preload ns."
  (:require ["react-devtools-core" :as react-devtools-core :refer [connectToDevTools]]))

(defonce app-ready? (atom false))

(defn- connect []
  (connectToDevTools #js {:isAppActive (fn []
                                         @app-ready?)
                          :host "127.0.0.1"
                          :port 8097
                          :resolveRNStyle nil}))

(defonce _ (connect))


