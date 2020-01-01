(ns scterm.db
  #_(:require [reagent.core :as r]))

(defonce screen (atom nil))

;; TODO: merge defaults db from each module
(def default-db
  {:debug {:fullscreen false}
   :nav  {:active-view nil
          :active-tab-index 0
          :anims {}
          }
   :focus :search
   :info {:booted false
          ;; could be one of :init/:loading/:loaded/:failed
          :fetch-status :init
          :details nil}
   :logs {:booted false
          :fetching false
          :stats {:fetching false
                  :details nil}
          :scroll {:base 0
                   :percent 0}}})
