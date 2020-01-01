(ns scterm.stats.utils
  (:require [chalk :as chalk :refer [bgWhite]]
            [clojure.string :as str]
            [cuerdas.core :as cstr]
            [cuerdas.regexp :as cregex]
            [medley.core :as m]
            [scterm.log :refer [log]]
            [scterm.utils :as u]
            [scterm.utils.format :refer [format-kvs-as-table max-key-length]]))

(defn highlight-match [line pattern]
  (when (re-find pattern line)
    (str/replace line pattern #(bgWhite %1))))

(defn find-stats [stats pattern]
  (let [pattern' (-> pattern
                     str/lower-case
                     cregex/escape
                     (as-> s (cstr/format "(?i)%s" s))
                     re-pattern)]
    (->> stats
         (keep (fn [[k v]]
                 (when-some [k' (highlight-match k pattern')]
                   [k' v])))
         (into {}))))


(defn format-job-stats
  [stats pattern]
  ;; TODO: allow space in pattern to act as "and"
  ;; (def vstats stats)
  (let [stats (m/map-keys u/keyword->str stats)
        filtered-stats (if (not-empty pattern)
                         (find-stats stats pattern)
                         stats)
        maxklen (max-key-length stats)]
    (-> filtered-stats
        sort
        (format-kvs-as-table maxklen))))

(comment
  (def stats {:foo 1 :bar 2 :barz 3})

  (log (find-stats stats "ba"))

  (def s1 "123")
  (.-length s1)

  )
