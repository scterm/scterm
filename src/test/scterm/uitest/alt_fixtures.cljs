(ns scterm.uitest.alt-fixtures
  (:require [scterm.main-impl :refer [setup-app! start-app! before-reload]]
            [scterm.screen :refer [exit-screen]]
            [scterm.db :refer [screen]]
            [cljs.test :refer [async]]
            [scterm.log :refer [log]]
            [scterm.test.failfast :refer [setup-fail-fast]]))

(setup-fail-fast)

(defn uitest-global-fixture [f]
  (setup-app! {:test true})
  (try
    (f)
    (finally
      (.destroy @screen))))

(defn uitest-fixture [f]
  (start-app! {:job-id "1886/682/84" :api-key "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" :test true})
  (async done
    (js/setTimeout
     (fn []
       (log "xxxxxxxxxxxx")
       (try
         (f)
         (finally
           (before-reload)
           (done))))
     100)))
