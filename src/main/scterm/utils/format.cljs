(ns scterm.utils.format
  (:require [clojure.string :as str]
            [re-frame.db :refer [app-db]]
            ["strip-ansi" :as strip-ansi]
            [scterm.utils :as u]))

(def default-line-length 60)

(defn available-line-length
  "Get the line length of the stats box, and ensures a mininum value of
  `default-line-length`.
  "
  []
  (let [main-view-width-ratio 0.8
        box-margin 10
        line-width (if-some [screen-width (get-in @app-db [:screen :width])]
                     (-> screen-width
                         (* main-view-width-ratio)
                         int
                         (- box-margin))
                     default-line-length)]
    (max default-line-length line-width)))


(defn format-one-line
  "   |<- line-length  ->|
      |<k>+<kpadding>|<v>|
  "
  [maxklen [k v]]
  (let [klen (count (strip-ansi k))
        kpadding (- maxklen klen)]
    (str k
         ":"
         (str/join "" (repeat kpadding " "))
         v)))

(defn max-key-length [m]
  (reduce (fn [accu [k _]]
            (max accu (count (strip-ansi (str k)))))
          0
          m))

(defn format-kvs-as-table
  "Format a map (or an associative collection) as a two column table.

  The width of the key column is decided by:
  - the current screen size
  - the longest key
  "
  ([m]
   (let [maxklen (max 50 (+ 5 (max-key-length m)))]
     (format-kvs-as-table m maxklen)))
  ([m maxklen]
   (def vm m)
   (->> m
        (map (partial format-one-line maxklen))
        (str/join "\n"))))
