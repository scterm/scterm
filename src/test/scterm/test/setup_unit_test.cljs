(ns scterm.test.setup-unit-test
  "Setup the fail fast for unit tests. The name of this ns ends with
  `-test` so it would be loaded by the test runner."
  (:require [cljs.test :as ct]
            [clojure.string :as str]
            [scterm.config]
            [scterm.test.failfast :refer [setup-fail-fast]]))

(setup-fail-fast)

(set! scterm.config/*unit-tests* true)

(defn print-marker-line []
  (let [num-markers 60
        marker (->> (repeat 60 "-")
                    (str/join ""))]
    (.log js/console marker)
    (.log js/console "\n")))

(defn setup-test-report-marker []
  (.on js/process "exit" print-marker-line))

(defonce _ (setup-test-report-marker))

;; (defonce orig-summary-handler (get-method ct/report [::ct/default :summary]))

;; (defn setup-test-report-marker []
;;   (defmethod ct/report [::ct/default :summary] [m]
;;     (let [marker (->> (repeat 30 "-")
;;                       (str/join ""))]
;;       (.log js/console marker))
;;     (orig-summary-handler m)))


;; (defonce _ (setup-test-report-marker))
