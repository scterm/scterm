(ns scterm.specs
  "Helpers for core.spec."
  (:require [clojure.spec.alpha :as s]))

(defmacro self-or-var [spec]
  (let [var-spec `(s/and var? #(s/valid? ~spec (deref %)))]
    `(s/or :spec ~spec :var-spec ~var-spec)))

(macroexpand '(self-or-var fn?))

(s/def ::callable (self-or-var fn?))

#_(s/valid? ::callable #'inc)

#_(s/form ::callable)
