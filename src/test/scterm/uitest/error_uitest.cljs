(ns scterm.uitest.error-uitest
  (:require [cljs.test :as ct :refer [is use-fixtures]]
            [clojure.core.async :refer [<! go timeout]]
            [clojure.string :as str]
            [scterm.blessed-utils :as bu]
            [scterm.config :as conf]
            [scterm.log :refer [log]]
            [scterm.test.fixtures :as tf]
            [scterm.test.utils
             :as
             tu
             :refer
             [assert-current-view defasynctest wait-for wait-for-element]]
            [scterm.uitest.fixtures :as uf]
            [scterm.uitest.utils :as uu :refer [has-text get-text]]))

(use-fixtures :once
  uf/uitest-global-fixture
  tf/use-fake-server
  tf/disable-cache)

(use-fixtures :each
  uf/uitest-error-fixture)

(defasynctest test-api-error
  (wait-for-element :info-error)
  (is (not (bu/select :info))))
