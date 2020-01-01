(ns scterm.screen
  (:require ["blessed" :as blessed]
            [re-frame.core :refer [dispatch]]
            [reagent.core :as r :refer [merge-props]]
            [clojure.string :as str]
            [medley.core :as m]
            [scterm.log :refer [log]]
            [scterm.utils :as u]
            [scterm.blessed-utils :as bu]
            [scterm.db :refer [screen]]
            [oops.core :refer [oget oset!]]
            ["react-blessed" :as react-blessed]))

(defonce render (.createBlessedRenderer react-blessed blessed))

(declare reset-screen)

(defonce lastkey (volatile! nil))

(defn scterm-fn? [f]
  (str/starts-with? (oget f :name) "scterm$"))

(defn debug-update-last-key [ch key]
  (vreset! lastkey
           (->> {:ch ch :key key}
                (m/map-vals #(js->clj % :keywordize-keys true)))))

(defn get-program []
  (oget @screen "program"))

(defn get-program-keypress-events []
  (->> (.listeners (get-program) "keypress")
       (filter u/cljs-fn?)))

;; (def program (get-program))
(defn ^:dev/after-load debug-bind-program-keypress []
  (let [program (get-program)]
    (bu/unbind-one-event-type program "keypress" (get-program-keypress-events))
    (.on program "keypress" debug-update-last-key)
    ))

(defn exit-screen
  ([]
   (exit-screen 0))
  ([code]
   ;; Calling `screen.destroy` would restore the terminal to normal
   ;; state. Otherwise the logs dumped by the debug logger (on process
   ;; exit) would not be displayed.
   (.destroy @screen)
   (.exit js/process code)))

(defonce demo-focus-on (r/atom true))

(def key-bindings
  {"C-c" #(exit-screen 0)
   ;; "/" #(dispatch [:nav/focus-search-box])
   ;; "S-/" #(dispatch [:nav/focus-main])
   ;; "left" #(dispatch [:routing/pop-state])
   "C-t" #(dispatch [:debug/toggle-fullscreen])
   "C-x" #(swap! demo-focus-on not)
   ["S-left"] #(dispatch [:nav/prev-tab])
   ["S-right"] #(dispatch [:nav/next-tab])
   })

(declare update-screen-size)
(declare on-screen-rendered)

(def events-bindings
  {"resize" update-screen-size
   "render" on-screen-rendered})

(defn ^:dev/after-load bind-events []
  (bu/unbind-one-event-type @screen "resize")
  (.on @screen "resize" update-screen-size))

(defn ^:dev/after-load bind-keys []
  ;; (log "rebinding keys")
  (bu/unbind-keys @screen)
  (doseq [[key handler] key-bindings]
    (let [keys (if (vector? key) key [key])]
      (.key @screen (clj->js keys) handler))))

(defn update-screen-size []
  ;; (log "update-screen-size called")
  (let [w (.-width @screen)
        h (.-height @screen)]
    (dispatch [:screen/resized w h])))

(defn on-screen-rendered []
  (dispatch [:screen/rendered]))

(defn init-screen []
  (let [ascreen (.screen blessed
                         #js { ;; :autoPadding false
                              ;; :smartCSR true
                              ;; :fullUnicode true
                              ;; :log "/tmp/scterm.log"
                              :title "SCTerm"})]
    (reset! screen ascreen)
    (bind-keys)
    (bind-events)
    (debug-bind-program-keypress)
    (update-screen-size)
    ascreen))

(declare destroy-node-recursive)

(defn reset-screen [screen]
  (destroy-node-recursive screen false))

(defn destroy-node-recursive
  "The event listeners doesn't see to be completely removed by `(render
  nil screen)` after a hot-reloading. So we manually call destroy and
  removeAllListeners on all nodes."
  ([^js node] (destroy-node-recursive node true))
  ([^js node include-self]
   #_(log (str
         "destroy-node-recursive called on "
         (pr-str node)
         ", with include-self = " include-self))
   (when-let [children (.-children node)]
     (dorun (map destroy-node-recursive children))
     (dorun (map (.bind (.-remove node) node) children))
     )
   (when include-self
     (when (.-removeAllListeners node)
       (.removeAllListeners node))
     (when (.-destroy node)
       (.destroy node)))))
