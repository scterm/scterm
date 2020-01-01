(ns scterm.jobinfo.utils-test
  (:require [cljs.test :refer [is use-fixtures]]
            [clojure.core.async :refer [go-loop timeout <!]]
            [clojure.string :as str]
            ;; for registering :http-xhrio fx
            [day8.re-frame.http-fx]
            [day8.re-frame.test :refer [run-test-sync]]
            [re-frame.core :refer [dispatch dispatch-sync reg-cofx reg-fx reg-event-db subscribe]]
            [re-frame.db :refer [app-db]]
            scterm.app.events
            scterm.app.subs
            [scterm.common.test-utils :as test.common]
            [scterm.config :as conf]
            [scterm.db :refer [default-db]]
            scterm.jobinfo.events
            scterm.jobinfo.subs
            [scterm.jobinfo.utils :as ju]
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
  tf/disable-cache
  ;; but individual test case could enable cache on its own. And here
  ;; we setup a unique temp dir as the cache dir.
  tf/set-cache-dir)

(defn setup-db [api-key & [job-id]]
  (reset! app-db default-db)
  (dispatch [:initialize {:api-key api-key
                          :job-id (or job-id test.common/running-job-id)
                          :routes []}]))

(defasynctest test-api-success
  (setup-db test.common/correct-api-key)
  (wait-for (some->
             @(subscribe [:info/details])
             :key))
  (is (= @(subscribe [:info/loading]) false))
  (let [details @(subscribe [:info/details])]
    (is (= (:key details) test.common/running-job-id)))

  (mock-event-handler :info/fetch
    (scterm.jobinfo.events/dispatch-jobinfo-update)
    (is @called "Error: api request not refreshed")))

(defasynctest test-api-error
  (setup-db test.common/incorrect-api-key)
  (wait-for @(subscribe [:info/api-error]))
  (wait-for (is (= @(subscribe [:info/loading]) false))))

(defn info-details-ready []
  (some-> (subscribe [:info/details])
          deref
          :key))

(defasynctest test-finished-job-would-not-be-refreshed
  (setup-db test.common/correct-api-key test.common/finished-job-id)
  (wait-for (info-details-ready))
  (is (ju/job-finished? @(subscribe [:info/details])))
  (mock-event-handler :info/fetch
    (scterm.jobinfo.events/dispatch-jobinfo-update)
    (is (not @called) "Error: finished job shall not be auto refreshed")))

(defasynctest test-load-from-cache
  ;; Turn cache on since cache is disabled in the test fixutue
  (set! conf/*use-cache* true)

  ;; populate the cache
  (setup-db test.common/correct-api-key test.common/finished-job-id)
  (wait-for (info-details-ready))

  (mock-effect-handler :http-xhrio
    ;; re-init the app status again
    (setup-db test.common/correct-api-key test.common/finished-job-id)
    (is (not @called)
        "Error: api requests shall not be sent and cache shall be used")))

(defasynctest test-failed-request-would-be-refreshed
  (setup-db test.common/correct-api-key test.common/error-job-id)
  (wait-for @(subscribe [:info/api-error]))
  (mock-event-handler :info/fetch
    (scterm.jobinfo.events/dispatch-jobinfo-update)
    (is @called "Error: errored job shall be auto refreshed")))

(defasynctest test-non-existing-jobs-shall-print-error
  (setup-db test.common/correct-api-key test.common/nonexisting-job-id)
  (wait-for @(subscribe [:info/api-error]))
  (is (str/includes? @(subscribe [:info/api-error]) "does not exist")))
