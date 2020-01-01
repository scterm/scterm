(ns scterm.specs
  "Helpers for core.spec."
  (:require [clojure.spec.alpha :as s]
            [cuerdas.core :as cstr]
            [scterm.log :refer [log]])
  (:require-macros [scterm.specs :refer [self-or-var]]))

;; (macroexpand-1 '(self-or-var fn?))

(s/def ::callable (self-or-var fn?))
