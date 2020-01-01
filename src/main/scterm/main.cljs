(ns scterm.main
  (:require [clojure.spec.alpha :as s]
            [expound.alpha :as expound :include-macros true]
            [cli-matic.core :refer [run-cmd]]
            [clojure.string :as str]
            [goog]
            ["react-blessed" :as react-blessed :refer [start]]
            ;; project deps
            [scterm.routing]
            [scterm.config :as conf]
            [scterm.utils :as u]
            [scterm.log :refer [log]]
            ;; TODO: not include these two ns in release builds.
            [scterm.unit-tests]
            [scterm.ui-tests]
            [scterm.force-build]
            [scterm.common.test-utils :as test.common]
            [scterm.main-impl :refer [setup-app! start-app!]]))

(defn fake-job? [job-id]
  (str/starts-with? job-id "1/1/"))

(defn main-cmd
  [{:keys [job api-key test]}]
  (setup-app! {:test test})
  (let [job-id (s/conform ::job-id job)
        fake (fake-job? job-id)
        api-key (if fake
                  test.common/correct-api-key
                  api-key)]
    (when (fake-job? job-id)
      (log "using fake server")
      (conf/use-fake-server!))
    (start-app! {:job-id job-id
                 :api-key api-key
                 :test test})))

(expound/def ::job-id
  (u/make-re-find-spec #"\d+/\d+/\d+$")
   "is not a valid job id. It should be like 1111/2/33")

(expound/def ::api-key
  (s/and string? not-empty (u/make-re-matches-spec #"^[0-9a-z]{32}$"))
  "is not a 32-bit api-key")

;; TODO: Remove the usage of clj-matic? We only need one param, after
;; all. When we see the job id is like 1/1/<xx>, then it's a fake
;; value. Or we can use tools.cli directly.
(def configuration
  {:app {:command "scterm",
         :description "Scrapy Cloud on the terminal",
         :version "0.0.1"},
   :global-opts [{:option "verbose",
                  :as "Turn on verbose logging",
                  :type :with-flag}],
   :commands [{:command "run",
               :description "start it!",
               :runs main-cmd
               :opts [{:option "job",
                       :as "The job id, like 123/4/5",
                       :type :string
                       :spec ::job-id}
                      {:option "api-key",
                       :as "The dash api key. You can also set
                               the SHUB_APIKEY environment
                               variable",
                       :type :string
                       :spec ::api-key
                       :env "SHUB_APIKEY"}]
               }]})

(defn main! [& args]
  (run-cmd args configuration)
  ;; TODO: explicitly wait for nodejs event loop to terminate?
  )
