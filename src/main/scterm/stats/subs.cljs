(ns scterm.stats.subs
  (:require [cuerdas.core :as cstr]
            [re-frame.core :refer [reg-sub]]
            [scterm.log :refer [log]]
            [scterm.stats.utils :as su]))

(reg-sub
 :stats/filtering-text
 (fn [db _]
   (when (= (get-in db [:routing :current-path]) "/stats")
     (get-in db [:stats :filtering-text]))))

(reg-sub
 :stats/title
 (fn [db _]
   (let [stats (:stats db)]
     (when (:filtering stats)
       (let [filtering-text (:filtering-text stats)]
         (cstr/format "Searching \"%s\"" filtering-text))))))

(reg-sub
 :stats/filtered-stats
 (fn [db _]
   (let [details (get-in db [:info :details])
         filtering-text (get-in db [:stats :filtering-text])]
     (su/format-job-stats (:scrapystats details) filtering-text))))
