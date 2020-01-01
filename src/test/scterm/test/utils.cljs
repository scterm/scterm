(ns scterm.test.utils
  (:require [scterm.db :refer [screen]]
            [scterm.utils :as u]
            [clojure.string :as str]
            [re-frame.db :refer [app-db]]
            [re-frame.core :refer [dispatch dispatch-sync reg-cofx reg-fx reg-event-db subscribe]]
            [day8.re-frame.test :refer [run-test-sync]]
            [oops.core :refer [oget ocall]])
  (:require-macros [scterm.test.utils]))

(def prefix->modifier {"S" :shift
                       "C" :ctrl})

(defn extract-keys [name]
  (let [match (str/split name "-")
        modifiers (->> (butlast match)
                       (reduce (fn [accu mod]
                                 (assoc accu (prefix->modifier mod) true))
                               {}))
        key (last match)]
    [modifiers key]))

#_(extract-keys "S-C-left")
#_(extract-keys "S-left")
#_(extract-keys "S-C-o")

(defn make-one-key-clj
  [name]
  (let [full-name (u/canon-str name)
        [modifiers name] (extract-keys full-name)]
    (merge {:ctrl false
            :full full-name
            :meta false
            :name name
            :sequence name
            :shift false}
           modifiers)))

(def arrow-keys #{"up" "down" "left" "right"})

(defn arrow-key? [k]
  (some (fn [arrow] (or (= arrow k)
                        (str/ends-with? k (str "-" arrow))))
        arrow-keys))

(def make-one-key-js (comp clj->js make-one-key-clj))

(defn get-current-path []
  (-> @app-db :routing :current-path))

(defn do-mock-event-handler [event f]
  (run-test-sync
    (let [called (volatile! false)]
      (reg-event-db
       event
       (fn [db _]
         (vreset! called true)
         db))
      (f called))))

(defn do-mock-effect-handler [effect f]
  (run-test-sync
   (let [called (volatile! false)]
     (reg-fx
      effect
      (fn [_]
        (vreset! called true)))
     (f called))))
