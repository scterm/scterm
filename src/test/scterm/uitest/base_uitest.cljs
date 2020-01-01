(ns scterm.uitest.base-uitest
  (:require [cljs.test :as ct :refer [is use-fixtures]]
            [clojure.core.async :refer [<! go timeout]]
            [clojure.string :as str]
            [scterm.blessed-utils :as bu]
            [scterm.log :refer [log]]
            [scterm.config :as conf]
            [scterm.test.fixtures :as tf]
            [scterm.test.utils :as tu :refer
             [assert-current-view defasynctest wait-for wait-for-element]]
            [scterm.uitest.fixtures :as uf]
            [scterm.uitest.utils :as uu :refer [send-key send-string has-text get-text]]))

(use-fixtures :once
  uf/uitest-global-fixture
  tf/use-fake-server
  tf/disable-cache)

(use-fixtures :each
  uf/uitest-fixture)

(defasynctest test-stats-filter
  (wait-for-element :infobar)
  (assert-current-view "/")

  (dotimes [_ 4]
    (send-key "S-right")
    (<! (timeout 100)))

  (assert-current-view "/stats")
  (is (not (has-text :infobar "Searching")))

  (send-key "/")
  (wait-for (has-text :infobar "Searching"))
  (is (has-text :stats "cookiejar") (get-text :stats))

  (send-string "log")
  (let [check-match (fn []
                     (let [stats (get-text :stats)]
                       (->> (str/split stats "\n")
                            (every? #(str/includes? % "log")))))]
    (wait-for (check-match))))

(comment

  (go
    (<! (timeout 2000))
    (send-key "/")
    #_(dotimes [_ 3] (.keyTap robotjs "backspace"))
    #_(.typeString robotjs "delay")

    )

  (send-key "backspace")
  (send-key "S-left")

  (macroexpand-1 '(wait-for (= 1 2) 1000))

(->> (str/split (get-text :stats) "\n")
     (every? #(str/includes? % "log")))
)
