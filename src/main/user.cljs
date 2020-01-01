(ns user
  (:require [cuerdas.core :as cstr]
            [clojure.string :as str]
            [medley.core :as m]
            [scterm.log :refer [log]]
            [scterm.debug.logger :refer [logger]]
            [scterm.utils :as u :refer [with-js->clj->js]]
            [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
            [re-frame.core :as re-frame :refer
             [reg-event-db reg-event-fx inject-cofx path after dispatch
              reg-cofx inject-cofx reg-fx]]))

(defn db []
  (-> @re-frame.db/app-db))

(defn info []
  (-> (db) :info))

(defn details
  ([]
   (-> (info) :details))
  ([& args]
   (get-in (details) args)))

;; Fetch fake jobinfo data for fast development.
(defonce ^:dynamic *fake-fetch* false)
(defn enable-fake-fetch []
  (log "Using fake fetch!")
  (set! *fake-fetch* true))
(defn disable-fake-fetch []
  (log "No using fake fetch!")
  (set! *fake-fetch* false))

#_(enable-fake-fetch)
#_(disable-fake-fetch)

#_(defonce fake-fetch-init (when ^boolean goog.DEBUG
                           (enable-fake-fetch)))

(def random-details
  (clj->js
   {:logs 100
    :items 30
    :pages 10
    :errors 2
    :spider "foo.com_discovery"
    :scrapystats {:items/scraped 1}
    }))

;; (defn remove-session-keys [stats]
;;   (m/filter-keys #(not (str/includes? % "session")) stats))

(defn random-increase-counter [details]
  (let [kind (rand-nth [:logs :items :pages])]
    ;; (log "kind = %s" kind)
    (update details kind (fnil inc 0))))


(defn gen-fake-info [details]
  (if details
    (random-increase-counter details)
    random-details))

(defn logs
  ([] (logs 10))
  ([n] (take-last n @logger)))

(defn foo []
  (with-js->clj->js foo (inc foo)))
