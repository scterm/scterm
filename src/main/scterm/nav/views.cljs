(ns scterm.nav.views
  (:require ["chalk" :as chalk :refer (red blue green bgRed bgBlue bgGreen underline)]
            [clojure.string :as str]
            [cuerdas.core :as cstr]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as reagent :refer [merge-props]]
            [scterm.log :refer [log]]
            [scterm.nav.events]
            [scterm.nav.subs]
            [scterm.app.tabs :as tabs]
            [scterm.utils :as u]
            [goog.object]
            [scterm.widgets.list :refer [list-widget]]
            [scterm.widgets.navlist :refer [navlist-widget]]))

(defn make-bg-color [color]
  (let [color (name color)]
    (if (str/starts-with? color "#")
      (.bgHex chalk color)
      (->> color
           (str/capitalize)
           (str "bg")
           (aget chalk)))))

(defn add-counter-color [v info-key]
  (if-let [color @(subscribe [:nav/counter-color info-key])]
    (do #_(log "color for %s is %s" info-key color)
        ((make-bg-color color) v))
    v
    ))

;; TODO: these shall go to subs!
(defn get-counter [info-key]
  (let [[info-key f] (if (keyword? info-key)
                       [info-key]
                       info-key)
        f (or f identity)]
    (when-let [info @(subscribe [:info/details])]
      (-> info
          info-key
          f
          (add-counter-color info-key)
          ))))

(defn make-list-items [tabs]
  (let [make-one-item
        (fn [tab]
          (let [{:keys [name info-key]} tab
                counter (or
                         (and info-key (get-counter info-key))
                         0)]
            (if info-key
              (cstr/format "%s (%s)" name counter)
              name)))]
    ;; Force realize the map because we call subscribe here. Otherwise
    ;; the subscribe would not be registered.
    (doall (map make-one-item tabs))))

(defn navbar-ui [props]
  [navlist-widget
   (merge-props {:items (make-list-items tabs/navtabs)
                 :active @(subscribe [:nav/active-tab-index])}
                props)])

;; (defn make-one-shortcut [index]
;;   #(dispatch [:routing/push-state (:path (navtabs index))]))

;; (defn make-shortcuts []
;;   (let [shortcut->index
;;         (reduce
;;          (fn [accu [index tab]]
;;            (assoc accu (:shortcut tab) index))
;;          {}
;;          (m/indexed navtabs))]
;;     (m/map-vals make-one-shortcut shortcut->index)))
