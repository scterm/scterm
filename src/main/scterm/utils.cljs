(ns scterm.utils
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [reagent.core :as r]
            ["moment" :as moment]
            ["chalk" :as chalk :refer [red]]
            [cuerdas.core :as cstr]
            [cljs-node-io.file :as file]
            [cljs-node-io.fs :as iofs]
            [oops.core :refer [oget]]
            [re-frame.core :refer [reg-event-fx]])
  (:require-macros [scterm.utils :refer [prog1]]))

;; (def url-re #"^(?:(?<scheme>[^:/]+)://)?(?<netloc>[^:/#]+)?(?::(?<port>[0-9]+))?(?<path>/[^#].*)?")
(def url-re #"^(?:([^:/]+)://)?([^:/#]+)?(?::([0-9]+))?(/[^#]*)?")

(defn urlparse [url]
  (->>
   (re-find url-re url)
   (drop 1)
   (zipmap [:scheme :netloc :port :path])))


(comment
  (re-find url-re "foo.com/f1")
  (re-find url-re "foo.com:80")
  (urlparse "http://foo.com:80")
  (urlparse "foo.com/f1")
  )

(def not-blank? (complement str/blank?))

(defn urlunparse [{:keys [scheme netloc port path]}]
  (let [path (if (nil? path)
               ""
               path)]
    (cond->> path
      (and (not-blank? path)
           (not (str/starts-with? path "/")))
      (str "/")

      (not-blank? port)
      (str ":" port)

      (not-blank? netloc)
      (str netloc)

      (not-blank? scheme)
      (str scheme "://"))))

(defn urljoin
  ([url]
   url)
  ([url s]
   (let [{:keys [path] :as parts} (urlparse url)
         path (or path "")
         path (str/replace path #"/+$" "")
         path (if (str/starts-with? s "/") s (str path "/" s))]
     (-> parts
         (assoc :path path)
         urlunparse)))
  ([url s & more]
   (reduce urljoin (urljoin url s) more)))

(defn join-lines [& strs] (str/join "\n" strs))

(defn right-pad
  [s n]
  (let [npad (- n (.-length s))]
    (if (pos? npad) (str s (str/join (repeat npad " "))) s)))

(defn format-http-error
  [resp]
  (select-keys resp [:status :response]))

(defn maybe-cycle
  "Ensure index is inside the bound 0 <= index <= max-index, and cycle
  to first/last if necessary."
  [index max-index]
  (let [fix-underflow #(if (neg? %) max-index %)
        fix-overflow #(if (> % max-index) 0 %)]
    (-> index
        fix-underflow
        fix-overflow)))

(defn cycle-next
  "Go back to the first element when trying to move beyond the last
  one."
  [index max-index]
  (-> index
      inc
      (maybe-cycle max-index)))

(defn cycle-prev
  "Go back to the last element when trying to move beyond the first
  one."
  [index max-index]
  (-> index
      dec
      (maybe-cycle max-index)))

(defn replace-last
  "Replace the last element of the collection"
  [f coll]
  (if (seq coll)
    (let [[front end] [(butlast coll) (last coll)]]
      (concat front [(f end)]))
    coll
    ))

(defn noop [_] nil)

(defn- reg-event-generic
  "A convenient wrapper to return nil for effects handlers that doesn't
  change the world."
  [reg-fn & args]
  (let [args (replace-last #(comp noop %) args)]
    (apply reg-fn args)))

(def reg-event-fx-noop (partial reg-event-generic reg-event-fx))

(defn truncate-str [s n]
  (if (<= (count s) n)
    s
    (str (cstr/slice s 0 n) "...")))

(defn maybe-truncate [m ks n]
  (let [ks (if (keyword? ks) [ks] ks)]
    (if-let [v (some-> (get-in m ks) js->clj str)]
      (assoc-in m ks (truncate-str v n))
      m)))

(defn component-height [element]
  (-> element second :height))

(defn adjust-top
  "In a vertical layout, set the :top offset for each child
  automatically based on their heights"
  [element]
  (let [[front children]
        (split-at 2 element)

        tops
        (reductions + 0 (map component-height (butlast children)))

        update-top
        (fn [x top] (update x 1 assoc :top top))

        new-children
        (map update-top children tops)]
    ;; (log "tops = %s" tops)
    (into [] (concat front new-children))))

(defn take-lastv
  "Like take-last but always return a vector"
  [coll n]
  (if (< (count coll) n)
    coll
    (into [] (take-last n coll))))

(defn keyword->str [kw]
  (-> (str kw)
      (cstr/slice 1)
      ))

(defn read-env
  "Reads an environment variable.
  If undefined, returns nil."
  [var]
  (-> js/process
      (.-env)
      (aget var)))

(defn exit [code]
  (.exit js/process code))

(defn make-re-spec [re-fn pattern]
  (let [conform-fn (fn [s]
                     (if-let [v (re-fn pattern s)]
                       v
                       ::s/invalid
                       ))]
    (s/conformer conform-fn)))

(defn make-re-find-spec [pattern]
  (make-re-spec re-find pattern))

(defn make-re-matches-spec [pattern]
  (make-re-spec re-matches pattern))

(defn canon-str [id]
  (if (keyword? id)
    (name id)
    id))

(defn cljs-fn? [f]
  (let [name (oget f :name)]
    (str/starts-with? name "scterm$")))

(defn rand-string [n]
  (str/join "" (repeatedly n #(rand-nth "abcdefgABCDEFG1234567890"))))

(defn ensure-dir [path]
  (let [d (file/->File path)]
    (when-not (.exists d)
      (.mkdir d))))

(defn make-temp-dir
  "Create a tmp directory and remove it at program exit."
  []
  (let [dname (urljoin "/tmp" (rand-string 10))]
    (prog1 dname
      (ensure-dir <>)
      (.on js/process "exit" #(iofs/rm-r <>)))))

(defn json-loads [x] (.parse js/JSON x))

(defn json-dumps [x] (.stringify js/JSON x))

(defn error-view [{:keys [error] :as props}]
  [:box (r/merge-props
         {:align "center"
          :valign "middle"
          :content (str "Oops! Error: " (red error))}
         props)])

(defn parse-time-delta [delta]
  (second (reduce
           (fn [[remaining accu] [unit unit-secs]]
             [(rem remaining unit-secs)
              (assoc accu unit (quot remaining unit-secs))])
           [delta]
           [[:day 86400]
            [:hour 3600]
            [:minute 60]
            [:second 1]])))

(defn readable-time-delta [delta]
  (let [delta (parse-time-delta delta)]
    (->> (for [unit [:day :hour :minute :second]]
           (let [value (get delta unit)]
             (when (pos? value)
               (str value " " (name unit) (when (> value 1) "s")))))
         (remove nil?)
         (str/join ", ")
         )))
