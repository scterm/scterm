(ns scterm.stats.stats-test
  (:require [cljs.test :refer [is]]
            [clojure.string :as str]
            [re-frame.core :refer [dispatch subscribe]]
            [re-frame.db :refer [app-db]]
            ["strip-ansi" :as strip-ansi]
            [scterm.db :refer [default-db]]
            [scterm.log :refer [log]]
            scterm.stats.events
            scterm.stats.subs
            scterm.jobinfo.events
            [scterm.test.utils :as tu]))

(defn send-key [key]
  (dispatch [:stats/keypress
             (tu/make-one-key-clj key)]))

(tu/defsynctest test-stats-keypress-events
  (let [get-stats (fn [& args]
                    (get-in @app-db (concat [:stats] args)))]
    (reset! app-db default-db)
    (send-key "/")
    #_(log "db = %s" (str @app-db))
    (is (= (get-stats :filtering) true))
    (is (= (get-stats :filtering-text) nil))

    (send-key "a")
    (is (= (get-stats :filtering) true))
    (is (= (get-stats :filtering-text) "a"))

    (send-key "b")
    (is (= (get-stats :filtering) true))
    (is (= (get-stats :filtering-text) "ab"))

    (send-key "backspace")
    (is (= (get-stats :filtering) true))
    (is (= (get-stats :filtering-text) "a"))

    (send-key "backspace")
    (is (= (get-stats :filtering) true))
    (is (= (get-stats :filtering-text) ""))

    (send-key "backspace")
    (is (= (get-stats :filtering) false))
    (is (= (get-stats :filtering-text) ""))

    ))

(defn make-job-info []
  (let [info {:scrapystats {:foo 1
                            :bar 2
                            :baz 3}}]
    (clj->js info)))

(tu/defsynctest test-stats-subs
  (let [text (subscribe [:stats/filtered-stats])
        info (make-job-info)]
    (dispatch [:info/success info])
    ;; (log "db = %s" (str @app-db))
    ;; (log "stats = %s" (str @text))
    (is (str/starts-with? @text "bar"))

    (send-key "/")
    (is (str/starts-with? @text "bar"))

    (send-key "f")
    (is (str/starts-with? (strip-ansi @text) "foo"))

    (send-key "a")
    (is (empty? @text))))


(comment
  (scterm.test.utils/run-current-ns-tests)

  (tu/make-one-key "/")

  (def text @(subscribe [:stats/filtered-stats]))

  text

  )
