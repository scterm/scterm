(ns scterm.uitest.utils
  (:require [clojure.string :as str]
            [scterm.db :refer [screen]]
            [scterm.utils :as u]
            [scterm.blessed-utils :as bu]
            [scterm.test.utils :as tu]
            ["chalk" :as chalk]
            ["ansicolor" :as ansicolor :refer [parse]]
            #_["robotjs" :as robotjs]
            [oops.core :refer [oget ocall]])
  (:require-macros [scterm.uitest.utils]))

(defn send-one-key-with-blessed [name]
  (let [prog (oget @screen "program")
        keyobj (tu/make-one-key-js name)]
    (ocall prog "emit" "keypress" nil keyobj)))

#_(defn send-one-key-with-robotjs [name]
  (.keyTap robotjs name))

;; robotjs is not very stable at this moment. Sometimes robotjs.keyTap doesn't work.
#_(def send-one-key send-one-key-with-robotjs)

(def send-one-key send-one-key-with-blessed)

#_(send-one-key "down")
#_(send-one-key "right")

(defn send-key
  ([name]
   (send-key name 1))
  ([name ntimes]
   (dotimes [_ ntimes] (send-one-key name))))

(defn send-string
  [s]
  (run! send-key s))

#_(send-key "down" 3)

(defn has-text [sel s]
  (when-let [el (bu/select sel)]
    (str/includes? (oget el "content") s)))

(defn get-text [sel]
  (let [el (bu/select sel)]
    (oget el "content")))

(defn has-content-bg-color [sel]
  (when-let [text (get-text sel)]
    (-> text parse (.-spans) (aget 0) (.-bgColor))))

(defn has-bg-color [sel]
  (let [el (bu/select sel)]
    (.. el -style -bg)))


(comment

(def vkeyobj #js {:sequence "OB", :name "down", :ctrl false, :meta false, :shift false, :code "OB", :full "down"})
(def vch nil)

(def prog (oget @screen "program"))
(.emit prog "keypress" vch vkeyobj)

(require '[scterm.stats.events :refer [get-char]])

(get-char (tu/make-one-key "down"))

(scterm.uitest.utils/print-ns-name)

(tu/make-one-key-js "g")
(tu/make-one-key-js "down")

(get-text :logs)

)
