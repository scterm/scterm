(ns scterm.test.env)

(def warn (js/console.log.bind js/console))
(re-frame.loggers/set-loggers!
 {:error (fn [& args]
          (cond
            (= ["re-frame: no" ":event"] (take 2 args)) nil
            :else (apply warn args)))})
