(ns scterm.log
  (:require ["moment" :as moment]
            [cuerdas.core :as cstr]
            [re-frame.loggers]))

(defn subst-nil [args]
  (doall (map (fn [x] (if (some? x) (str x) "null")) args)))

;; TODO: log level debug/info/warning etc.
(defn log [fmt & args]
  ;; null much be pre-processed before passing to goog.string.format
  (let [args    (subst-nil args)
        msg     (if (empty? args) fmt (apply cstr/format fmt args))
        msg     (-> (moment)
                    (.format)
                    (str " " msg))
        logfn   (.-log js/console)]
    (logfn msg)))

;; fix re-frame warnings about handlers been overwritten after hot-reloading
;; See https://github.com/day8/re-frame/issues/204#issuecomment-250337344
(def warn (js/console.log.bind js/console))
(re-frame.loggers/set-loggers!
 {:warn (fn [& args]
          (cond
            (= "re-frame: overwriting" (first args)) nil
            :else (apply warn args)))})
