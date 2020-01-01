(ns scterm.uitest.utils
  (:require [day8.re-frame.test :refer [run-test-sync run-test-async]]
            [clojure.core.async :refer [go-loop timeout <!]]
            [cljs.test :refer [async]]))

;; TODO
(defmacro wait-for-text [sel s])

(comment

(macroexpand-1
 '(defuitest basic-test
    (+ 1 2)
    (+ 1 3)
    ))


(macroexpand-1 '(wait-for (= 1 2) 1000))

)
