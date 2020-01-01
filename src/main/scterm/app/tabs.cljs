(ns scterm.app.tabs
  "Tabs management. Note tabs is just a view-layer concept. It extracts
  some information from current routing path (which is the single
  source of truth) to tell the user currently displayed view."
  (:require [scterm.utils :as u]
            [clojure.string :as str]
            [scterm.log :refer [log]]
            [medley.core :as m]))

(def navtabs
  [{:name "Job Info" :path "/" :shortcut "j"}
   {:name "Items" :path "/items" :shortcut "i" :info-key :items}
   {:name "Requests" :path "/requests" :shortcut "r" :info-key :pages}
   {:name "Logs" :path "/logs" :shortcut "l" :info-key :logs}
   {:name "Stats" :path "/stats" :shortcut "s" :info-key [:scrapystats goog.object.getCount]}])

(def max-index (dec (count navtabs)))

(defn prev-index [current-index]
  (-> current-index
      dec
      (u/maybe-cycle max-index)))

(defn next-index [current-index]
  (-> current-index
      inc
      (u/maybe-cycle max-index)))

(defn index->path [index]
  (-> (nth navtabs index)
      :path
      ))

;; TODO: this doesn't work for paths with queries. The `urlparse`
;; function need to support that first.
(defn path->index [current-path]
  (if (str/blank? current-path)
    0
    (let [{:keys [path]}  (u/urlparse current-path)]
      (->> navtabs
           (keep-indexed (fn [index tab]
                           (when (= path (:path tab))
                             index)))
           first))))

#_(path->index "/")
#_(path->index "/info")
#_(path->index "/info?a=1")
