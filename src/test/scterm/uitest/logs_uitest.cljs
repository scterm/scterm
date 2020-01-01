(ns scterm.uitest.logs-uitest
  (:require [cljs.test :as ct :refer [is use-fixtures]]
            [clojure.core.async :refer [<! go timeout]]
            [clojure.string :as str]
            [scterm.blessed-utils :as bu]
            [scterm.log :refer [log]]
            [scterm.test.fixtures :as tf]
            [scterm.common.test-utils :as test.common]
            [scterm.config :as conf]
            [scterm.test.utils :as tu :refer
             [assert-current-view defasynctest wait-for wait-for-element]]
            [scterm.uitest.fixtures :as uf]
            [scterm.uitest.utils :as uu]))

(use-fixtures :once
  uf/uitest-global-fixture
  tf/use-fake-server
  tf/disable-cache)

(use-fixtures :each
  uf/uitest-fixture)

(defasynctest test-basic-logs-view
  (wait-for-element :infobar)
  (assert-current-view "/")

  (dotimes [_ 3]
    (uu/send-key "S-right")
    (<! (timeout 100)))

  (assert-current-view "/logs")
  (wait-for (bu/select :logs) 1000)
  (is (uu/has-text :logs "Log opened"))
  (is (uu/has-text :logs "9: "))
  (is (not (uu/has-text :logs "19: ")))

  (uu/send-key "S-g")
  (wait-for (bu/select :logs-loading) 500)
  (wait-for (not (bu/select :logs-loading)) 1000)

  (is (uu/has-text :logs "19: ")))


(defasynctest test-logs-load-until-reaching-the-end
  (wait-for-element :infobar)
  (assert-current-view "/")

  (dotimes [_ 3]
    (uu/send-key "S-right")
    (<! (timeout 100)))

  (assert-current-view "/logs")
  (wait-for (bu/select :logs) 1000)
  (is (uu/has-text :logs "Log opened"))
  (is (uu/has-text :logs "9: "))
  (is (not (uu/has-text :logs "19: ")))

  (uu/send-key "S-g")
  (wait-for (bu/select :logs-loading) 500)
  (wait-for (not (bu/select :logs-loading)) 1000)
  (is (uu/has-text :logs "19: "))
  (is (not (uu/has-text :logs "20: ")))

  (uu/send-key "S-g")
  (wait-for (bu/select :logs-loading) 500)
  (wait-for (not (bu/select :logs-loading)) 1000)
  (is (uu/has-text :logs "29: "))
  (is (not (uu/has-text :logs "30: ")))

  ;; The total logs is 30. We have reached the end.
  (uu/send-key "S-g")
  (dotimes [_ 10]
    (<! (timeout 50))
    (is (not (bu/select :logs-loading))))
  (is (not (uu/has-text :logs "30: "))))

(defasynctest test-logs-api-failed-on-first-load
  (wait-for-element :infobar)
  (assert-current-view "/")

  (set! conf/*api-key* test.common/incorrect-api-key)

  (dotimes [_ 3]
    (uu/send-key "S-right")
    (<! (timeout 100)))

  (assert-current-view "/logs")
  (wait-for (bu/select :logs-error) 1000)
  (is (not (bu/select :logs))))

(defasynctest test-logs-api-failed-on-updating
  (wait-for-element :infobar)
  (assert-current-view "/")

  (dotimes [_ 3]
    (uu/send-key "S-right")
    (<! (timeout 100)))

  (assert-current-view "/logs")
  (wait-for (bu/select :logs) 1000)
  (is (uu/has-text :logs "Log opened"))
  (is (not (bu/select :logs-error)))

  (set! conf/*api-key* test.common/incorrect-api-key)
  (uu/send-key "S-g")
  (<! (timeout 100))
  (set! conf/*api-key* test.common/correct-api-key)

  ;; (wait-for (bu/select :logs-loading) 500)
  (wait-for (not (bu/select :logs-loading)) 500)

  (is (uu/has-text :logs "Log opened"))
  (is (not (bu/select :logs-error))))

(comment

  (uu/send-key "S-left")
  (uu/send-key "S-right")
  (uu/send-key "g")
  (uu/send-key "S-g")
  (uu/send-key "down")

  ())
