(ns scterm.fixes
  (:require ["xmlhttprequest" :refer (XMLHttpRequest)]))

;;;;;;;;;;;;;;;;;;;;;;;;
;; various fixes
;;;;;;;;;;;;;;;;;;;;;;;;

;; Fix js/Symbol printting. Otherwise print a react element (e.g. when
;; REPL tries to show the result) would result in exceptions.
;; https://github.com/binaryage/cljs-devtools/issues/25#issuecomment-266711869
(when (exists? js/Symbol)
  (extend-protocol IPrintWithWriter
    js/Symbol
    (-pr-writer [sym writer _]
      (-write writer (str "\"" (.toString sym) "\"")))))

;; Fix ajax for nodejs. See
;; https://github.com/r0man/cljs-http/issues/94#issuecomment-484995542
;; TODO: this could be removed when we upgrade to
;; re-frame-http-fx-alpha which uses fetch (provided by node-fetch in
;; nodejs)
(set! js/XMLHttpRequest XMLHttpRequest)
