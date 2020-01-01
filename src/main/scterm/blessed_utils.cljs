(ns scterm.blessed-utils
  (:require [clojure.string :as str]
            [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
            [medley.core :as m]
            [scterm.log :refer [log]]
            [scterm.utils :as u]
            [scterm.db :refer [screen]]))

(defn make-seq [x]
  (if (or (array? x)
          (sequential? x))
    x
    [x]))

(defn get-matched-fns [fns]
  (->> fns
       make-seq
       #_(filter cljs-fn?)))

(defn unbind-one-event-type
  ([^js el type listeners]
   #_(log "xxx: unbind-one-event-type called with type %s" type)
   (when-let [listeners (seq (get-matched-fns listeners))]
     #_(log "unbind: type = %s, found %s listeners" type (count listeners))
     (run! #(.removeListener el type %) listeners)))
  ([^js el type]
   (let [listeners (-> (.listeners el type)
                       js->clj)]
     (unbind-one-event-type el type listeners))))

(comment
  (def el @scterm.db/screen)
  (def events (-> (.-_events el) js->clj))
  (def listeners (-> (m/filter-keys #(str/starts-with? % "key ") events) first second))
  (->> listeners
      make-seq
      (filter cljs-fn?))
  )

(defn unbind-keys
  "Unbind all keyboard events listeners for the given element."
  [el]
  (let [events (-> (.-_events el) js->clj)]
    (->> events
         (m/filter-keys #(str/starts-with? % "key "))
         ;; (#(log "%s" (str %)))
         (run! (fn [[type listeners]] (unbind-one-event-type el type listeners))))))

(defn all-elements
  ([]
   (all-elements @screen))
  ([node]
   {:pre [some? node]}
   (let [children (seq (.-children node))]
     (if (and children (not-empty children))
       (cons node (mapcat all-elements children))
       [node]))))

(defn select [id]
  ;; TODO: use reduce/reduced for early stop instead of enumberating
  ;; all the elements.
  (let [id (u/canon-str id)]
    (m/find-first #(= id (oget % "options.?id")) (all-elements))))

(comment

  (all-elements)
  (def all (all-elements))
  (-> (first all)
      (oget "options.?id"))

  (require '["blessed" :as blessed :refer [Message]])
  (require '["chalk" :as chalk :refer [red green blue bgRed bgGreen]])
  (let [msg (Message.)]
    (.display msg (bgGreen "Hi!") 10000))
  
  ())
