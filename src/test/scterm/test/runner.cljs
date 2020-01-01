(ns scterm.test.runner
  "This ns is a copy-and-modified version of shadow.test.node. The main
  feature is the -k switch to allow run only matched test cases.

  Note the :dev/always ns meta is required to tell shadow-cljs to skip
  caching for this ns so the tests-collecting macro could be inovked
  each time."

  {:dev/always true}
  (:require [cljs.test :as ct]
            [clojure.string :as str]
            [shadow.test :as st]
            [medley.core :as m]
            [scterm.config :as conf]
            ["chalk" :as chalk]
            [scterm.log :refer [log]]
            [clojure.tools.cli :refer [parse-opts]]
            [shadow.test.env :as env]))

;; FIXME: add option to not exit the node process?
(defmethod ct/report [::ct/default :end-run-tests] [m]
  (if (ct/successful? m)
    (js/process.exit 0)
    (js/process.exit 1)))

(defmethod ct/report [::ct/default :begin-test-var] [m]
  (println ">>>>>>>> Testing" (-> m :var meta :name)))

(def cli-options
  [["-k" "--test EXPR"
    "The test expression. If \"-k foo\" is specified, only tests whose ns name or test name contains \"foo\" would be executed"]
   ["-w" "--wait"
    "If an async test fails, blocking wait there instead of continuing"]])

(defn partition-map-by-keys
  "Partition a map into two submaps based on the result of (pred k)."
  [pred m]
  (let [groups (->> m
                    (group-by (fn [[k _]] (pred k)))
                    (m/map-vals #(into {} %)))]
    [(get groups true)
     (get groups false)]))

(defn make-filter-fn [expr]
  (fn [v]
    (-> v
        ;; v could be either a ns Symbol or a test Var. Here we turn
        ;; it to a Symbol if it's a Var
        (as-> x (cond-> x
                  (var? x)
                  (.-sym)))
        name
        (str/includes? expr))))

;; test-data is a map of shape Map[NS, Map[:vars, List[TestVar]]]
(defn filter-tests [test-data test-expr]
  (let [filter-fn (make-filter-fn test-expr)
        [matched-ns unmatched-ns] (partition-map-by-keys filter-fn test-data)
        matched-vars (->> unmatched-ns
                          (m/map-vals
                           (fn [{:keys [vars] :as v}]
                             ;; Only keep the matched test vars. If
                             ;; there are no matched test vars, the
                             ;; whole ns could be dropped.
                             (when-let [new-vars (seq (filter filter-fn vars))]
                               (assoc v :vars new-vars))))
                          (m/filter-vals not-empty))]
    (merge matched-ns matched-vars)))

(defn exit-with-errors [{:keys [summary errors]}]
  (println (->> (str "Error:" (str/join "\n" errors))
                (.bgRed chalk)))
  (println (str "\nUsage:\n\n" summary))
  (.exit js/process 1))

(defn main [& args]
  (let [opts (parse-opts args cli-options)
        _ (if (:errors opts)
            (exit-with-errors opts))
        test-expr (get-in opts [:options :test])
        test-data (cond-> (env/get-test-data)
                    test-expr
                    (filter-tests test-expr))]
    (when test-expr
      (log "test-expr = %s" test-expr))
    (when (get-in opts [:options :wait])
      (set! conf/*debug-test* true))
    (env/reset-test-data! test-data)
    (st/run-all-tests)))

(comment

  (parse-opts ["--test" "foo"] cli-options)
  (parse-opts ["-k"] cli-options)
  (parse-opts ["-w"] cli-options)

())
