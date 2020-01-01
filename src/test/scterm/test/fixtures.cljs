(ns scterm.test.fixtures
  (:require [scterm.config :as conf]
            [cljs.test :as ct]
            [scterm.log :refer [log]]
            ;; [scterm.test.unit-test-runner]
            [scterm.utils :as u]))

(def global-fixtures 
  {:before
   (fn [])})

(def use-fake-server
  {:before
   (fn []
     (set! conf/*hs-api-url* "http://127.0.0.1:18000"))

   :after
   (fn []
     (set! conf/*hs-api-url* "https://storage.scrapinghub.com"))})

(def mark-async-test
  {:before
   (fn []
     (set! conf/*in-async-test* true))

   :after
   (fn []
     (set! conf/*in-async-test* false))})

(def disable-cache
  {:before
   (fn []
     (set! conf/*use-cache* false))

   :after
   (fn []
     (set! conf/*use-cache* true))})

(def set-cache-dir
  {:before
   (fn []
     (let [dir (u/make-temp-dir)]
       (log "cache-dir is %s" dir)
       (set! conf/*cache-dir* dir)))

   :after
   (fn []
     (set! conf/*use-cache* true))})
