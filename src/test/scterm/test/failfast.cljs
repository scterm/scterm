(ns scterm.test.failfast
  (:require [scterm.log :refer [log]]
            [scterm.config :as conf]
            [cljs.test :as ct]
            [oops.core :refer [oget]]))

(defonce orig-fail-handler (get-method ct/report [::ct/default :fail]))
(defonce orig-error-handler (get-method ct/report [::ct/default :error]))

(def failfast-error-msg "---failfast-error-msg---")
(def outside-failfast-error-msg "---outside-failfast-error-msg---")

(defn setup-fail-fast []
  (log "Setting up fail fast")
  (defmethod ct/report [::ct/default :fail]
    [m]
    (orig-fail-handler m)
    (throw (js/Error. failfast-error-msg)))

  (defmethod ct/report [::ct/default :error]
    [m]
    (let [msg (-> m :actual (oget :message))]
      ;; (println "m = " m)
      #_(log "msg = '%s'" msg)
      (condp = msg
        ;; The original exception E1 thrown in :fail handler is caught
        ;; by the `is` macro and dispatched here, we need to throw
        ;; another exception E2 again to prevent current test case
        ;; from continuing.
        failfast-error-msg
        #_(when-not conf/*in-async-test*
          (throw (js/Error. outside-failfast-error-msg)))
        (throw (js/Error. outside-failfast-error-msg))

        ;; The exception E2 was caught by `test-var-block*` and again
        ;; dispatched here. We don't need to do anything since the
        ;; original :fail handler has already reported this event.
        outside-failfast-error-msg
        (do #_(log "noop!")
            :noop)

        (do #_(log "calling orig-error-handler for %s" (str m))
            (orig-error-handler m))))))

;; Special handling of async tests

#_(defn run-block
  "Invoke all functions in fns with no arguments. A fn can optionally
  return

  an async test - is invoked with a continuation running left fns

  a seq of fns tagged per block - are invoked immediately after fn"
  [fns]
  (when-first [f fns]
    (let [obj (f)]
      (if (ct/async? obj)
        (let [d (delay (run-block (rest fns)))
              next-step (fn []
                          (if (realized? d)
                            (println "WARNING: Async test called done more than one time.")
                            @d))]
          (obj next-step))
        (recur (cond->> (rest fns)
                 (::block? (meta obj)) (concat obj)))))))
