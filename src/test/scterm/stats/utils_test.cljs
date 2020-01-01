(ns scterm.stats.utils-test
  (:require [cljs.test :refer [deftest is]]
            [clojure.string :as str]
            [day8.re-frame.test :refer [run-test-sync]]
            [medley.core :as m]
            ["chalk" :as chalk]
            ["strip-ansi" :as strip-ansi]
            [scterm.log :refer [log]]
            [scterm.utils :as u]
            [scterm.test.utils :as tu]
            [scterm.stats.utils :refer [find-stats format-job-stats]]))

(tu/deftest test-find-stats
  (let [stats {:foo 1
               :bar 2
               :barz 3}
        stats (m/map-keys u/keyword->str stats)]
    ;; The find results keys are highlighted, but during tests there
    ;; is no real terminal so the keys are still plain string.
    (is (= (->> (find-stats stats "ba")
                (m/map-keys strip-ansi))
           {"bar" 2 "barz" 3}))))

(tu/deftest test-format-stats
  (let [stats {:foo 1
               :bar 2
               :barz 3}]
    (is (str/starts-with? (format-job-stats stats nil) "bar"))))


(comment

  (def s1 (.red chalk "123"))

  (strip-ansi s1)

  (def stats {:foo 1, :bar 2, :barz 3})
  (format-job-stats stats "a")
  )
