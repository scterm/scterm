(ns scterm.logs.logs-test
  (:require [cljs.test :refer [is use-fixtures]]
            [clojure.core.async :refer [go-loop timeout <!]]
            [clojure.string :as str]
            ;; for registering :http-xhrio fx
            [day8.re-frame.http-fx]
            [day8.re-frame.test :refer [run-test-sync]]
            [re-frame.core :refer [dispatch dispatch-sync reg-cofx reg-fx reg-event-db subscribe]]
            [re-frame.db :refer [app-db]]
            ["strip-ansi" :as strip-ansi]
            scterm.app.events
            scterm.app.subs
            [scterm.common.test-utils :as test.common]
            [scterm.config :as conf]
            [scterm.db :refer [default-db]]
            scterm.logs.events
            scterm.logs.subs
            [scterm.logs.views :refer [format-one-line]]
            [scterm.logs.utils :as lu]
            [scterm.log :refer [log]]
            [scterm.test.fixtures :as tf]
            [scterm.test.utils :as tu :refer [defasynctest wait-for mock-event-handler mock-effect-handler]]
            [scterm.utils :as u]))

(use-fixtures :once
  tf/global-fixtures
  tf/use-fake-server
  tf/mark-async-test)

(use-fixtures :each
  ;; disable cache by default
  tf/disable-cache)

(defn setup-db [api-key & [job-id]]
  (reset! app-db default-db)
  (dispatch [:initialize {:api-key api-key
                          :job-id (or job-id test.common/running-job-id)
                          :routes []}]))

(defasynctest test-logs-fetch-success
  (setup-db test.common/correct-api-key)
  (wait-for (-> @(subscribe [:logs/entries]) :ready?))
  (wait-for (not @(subscribe [:logs/fetching]))))

(defasynctest test-logs-fetch-error
  (setup-db test.common/incorrect-api-key)
  @(subscribe [:logs/entries])
  ;; (println (-> @app-db :logs))
  (wait-for @(subscribe [:logs/api-error]) 300))

(tu/deftest test-format-one-line
  (let [line (format-one-line 0 {:time 0 :level 20 :message "Log opened"})]
    (is (= (strip-ansi line) "0: 1970-01-01 00:00:00 INFO Log opened"))))

(defn send-key [key]
  (dispatch [:logs/keypress
             (tu/make-one-key-clj key)]))

(defasynctest test-logs-load-more
  (setup-db test.common/correct-api-key)
  (wait-for (-> @(subscribe [:logs/entries]) :ready?))
  (mock-effect-handler :http-xhrio
    (dispatch [:logs/update-scroll {:base 30 :percent 100}])
    (is @called
        "Error: logs shall be fetched when reaching the bottom")))

(defasynctest test-logs-no-load-more-when-reaching-the-end
  (setup-db test.common/correct-api-key)
  (wait-for (-> @(subscribe [:logs/entries]) :ready?))
  (mock-effect-handler :http-xhrio
    (dispatch [:logs/stats-fetch-success {:totals {:input_values 20}}])
    (dispatch [:logs/update-scroll {:base 30 :percent 100}])
    (is @called
        "Error: logs shall be fetched when reaching the bottom"))
  
  (mock-effect-handler :http-xhrio
    (dispatch [:logs/stats-fetch-success {:totals {:input_values 10}}])
    (dispatch [:logs/update-scroll {:base 30 :percent 100}])
    (is (not @called)
        "Error: logs shall be not fetched when all logs has been fetched")
    ))
