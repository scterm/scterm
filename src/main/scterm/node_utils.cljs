(ns scterm.node-utils
  (:require [clojure.core.async :as async :include-macros true]))

;; (defonce exit-signal-chan (async/chan))

;; (defn wait-for-exit []
;;   (.on js/process "beforeExit"
;;        (fn [_]
;;          (async/go
;;            (async/>! exit-signal-chan true))))
;;   (async/take! ))
