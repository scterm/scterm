(ns scterm.uitest.assertions
  (:require [cljs.test :as ct]))


(defmethod ct/assert-expr :fail-fast [menv msg form]
  ;; nil test: always fail
  `(ct/do-report {:type :xfail, :message ~msg}))
