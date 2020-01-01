(ns scterm.debug.logger
  (:require [re-frame.core :refer [subscribe reg-sub reg-event-db path]]
            [clojure.string :as str]
            [scterm.utils :as u]
            [scterm.debug.events]
            [scterm.debug.subs]
            [reagent.core :as reagent :refer [merge-props]]))

(defonce setup-called (volatile! false))
(defonce real-console-log (volatile! nil))

(defonce logger
  (reagent/atom []))

(defn clear-logs []
  (reset! logger []))

(defn dump-all-logs [test-mode code]
  (when (or test-mode
            (not= code 0))
    (let [logfn @real-console-log
          long-line (str/join "" (repeat 20 "-"))]
      (logfn (str long-line "\nShowing all logs:\n" long-line))
      (run! logfn @logger))))

(def max-logs 50)

(defn ignore-log? [line]
  (some #(str/includes? line %)
        ["CLJS DevTools"
         "Tracing is not enabled"
         ]))

(defn log-fn [& args]
  (let [line (clojure.string/join " " args)]
    (when-not (ignore-log? line)
      (u/noop
       (swap! logger (fn [old]
                       (-> old
                           (conj line)
                           (u/take-lastv max-logs))))))))

(defn setup! [test-mode]
  (when-not @setup-called
    (vreset! setup-called true)
    (.on js/process "exit" (partial dump-all-logs test-mode))
    (let [oldlog (.bind (.-log js/console) js/console)]
      (vreset! real-console-log oldlog)
      (set! (.-log js/console) log-fn)
      (set! (.-warn js/console) log-fn))))
