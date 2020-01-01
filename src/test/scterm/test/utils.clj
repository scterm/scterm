(ns scterm.test.utils
  (:require [clojure.core.async :refer [go-loop timeout <!]]
            [cljs.test :refer [async]]
            [re-frame.core :refer [dispatch dispatch-sync reg-cofx reg-fx reg-event-db subscribe]]
            [day8.re-frame.test :refer [run-test-sync]]))

(defmacro deftest
  "Like cljs.test/deftest but adds one layer of wrapper function to the
  body. Only this way the call site of a test FAIL would be shown
  correctly."
  [name & body]
  (let [impl (-> name (str "-impl") symbol)]
    `(do
       (defn ~impl []
         ~@body
         )
       (cljs.test/deftest ~name
         (~impl)))))

(defmacro defsynctest
  "Like deftest but also wrap the body in a run-test-sync block."
  [name & body]
  (let [impl (-> name (str "-impl") symbol)]
    `(do
       (defn ~impl []
         ~@body
         )
       (cljs.test/deftest ~name
         (run-test-sync
          (~impl))))))

(defmacro defasynctest
  "Wrap the test in a core.async go block.

  So that we can use (viz., emulate) blocking wait."
  [name & body]
  `(cljs.test/deftest ~name
    (async ~'done
      (clojure.core.async/go
        (try
          (<! (timeout 50))
          ~@body
          ;; The async block is not protected by
          ;; cljs.test/test-var-block* because it is, well, async.
          (catch :default e#
            (cljs.test/do-report
             {:type :error
              :message "Uncaught exception, not in assertion."
              :expected nil
              :actual e#}))
          (finally
            (~'done)))))))

(defmacro wait-for
  "Blocking wait until the `expr` to evaluates to logical true or util
  the timeout is triggered.

  This macro can not be wrapped in a function (as is the best practice
  to write clojure macros) because it has core.async constructs like
  `go` and `timeout` which could not be in a nested function
  call. "
  ([expr]
   `(wait-for ~expr 300))
  ([expr max-wait]
   `(let [interval# (-> (/ ~max-wait 5)
                        int
                        (max 10))
          checker# (go-loop [total-wait# 0]
                     (cond
                       ~expr
                       :ok

                       (>= total-wait# ~max-wait)
                       :timeout

                       :else
                       (do
                         (<! (timeout interval#))
                         (recur (+ total-wait# interval#)))))]
      (let [outcome# (<! checker#)]
        (when (and (= outcome# :timeout)
                   scterm.config/*debug-test*)
          (scterm.log/log "Entering debugging mode for %s" (quote ~expr))
          ;; TODO: press a key to exit?
          (<! (timeout 100000))))
      (cljs.test/is ~expr ~(str "Wait timeouts after " max-wait " msecs")))))

(defmacro assert-current-view [path]
  `(wait-for (= (get-current-path) ~path) 1000))

(defmacro wait-for-element [id]
  `(wait-for (some? (scterm.blessed-utils/select ~id)) 100))

(defmacro print-ns-name
  "A demo macro to show how to get the calling ns info using a macro.
  This is only viable with macros because unlike CLJ, in CLJS at
  runtime there is no namespace information."
  []
  (let [current-ns (-> &env :ns :name)]
    `(println {:ns-name (quote ~current-ns)})))

(defmacro mock-event-handler
  "Helper to verify an event handler is called or not called.

  See also [[mock-effect-handler]]."
  {:style/indent 1}
  [event & body]
  `(do-mock-event-handler ~event (fn [~'called] ~@body)))

(defmacro mock-effect-handler
  "Helper to verify an effect handler is called or not called.

  See also [[mock-event-handler]]."
  {:style/indent 1}
  [effect & body]
  `(do-mock-effect-handler ~effect (fn [~'called] ~@body)))

(defmacro run-current-ns-tests
  "NS that uses this macro must import shadow.test.env and shadow.test."
  []
  (let [ns (-> &env :ns :name)]
    `(do
       (-> (shadow.test.env/get-test-data)
           (shadow.test.env/reset-test-data!))
       (shadow.test/test-ns (quote ~ns)))))
