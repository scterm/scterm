(ns scterm.app.views
  (:require ["chalk" :as chalk :refer (red)]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r :refer [merge-props]]
            [medley.core :as m]
            ;; for side effects
            [scterm.app.events]
            [scterm.app.subs]
            [goog.object]
            [scterm.log :refer [log]]
            [scterm.debug.views :refer [debug-box]]
            [scterm.jobinfo.views :refer [info-view]]
            [scterm.react-utils :refer [error-boundary]]
            [scterm.stats.views :refer [stats-view]]
            [scterm.logs.views :refer [logs-view #_logs-filter-view]]
            [scterm.items.views :refer [items-view]]
            [scterm.requests.views :refer [requests-view]]
            [scterm.nav.views :refer [navbar-ui]]
            [scterm.screen :refer [demo-focus-on]]
            [scterm.widgets.navlist :refer [navlist-widget]]
            [scterm.styles :refer [focused-style]]))

(def help-tips
  ["Move between tabs:\n  => Shift + <left>/<right>\n"
   "Scroll:\n  => <up>/<down>\n"
   "Filtering:\n  => /"
   ])

(defn infobar-ui [props]
  [:box#infobar (merge-props
         props
         {:content @(subscribe [:ui/infobar-text])})])

(defn main-panel-ui [props]
  [:layout (merge-props {:layout :grid} props)
   [infobar-ui {:width "100%"
                :height 3
                :border :line}]
   [navbar-ui (merge {:width 100
                      :height 2
                      :left 2})]
   [:box {:width "100%"
          :height "100%-5"
          :border :line
          :ref (fn [el]
                 (def vel el))}
    (let [[view args] @(subscribe [:nav/active-view])
          ui (:ui view info-view)
          view-style {:left 1}]
      (def vui ui)
      [ui (merge view-style args)])]])

(defn keyboard-tips-ui [props]
  [:layout (merge-props {:border :line
                         :style {:border {:fg :cyan}}}
                        props)
   [:text {:label "Keyboard Shortcuts"
           :width "100%-2"
           }]
   [:box {:border :line
          :width "100%-2"
          :height 10
          :style {:border {:fg :cyan}}
          :content (clojure.string/join "\n" help-tips)}]
   [:box#demo1 (r/merge-props (when @demo-focus-on
                                {:content (str "box#demo1: "
                                               (if @demo-focus-on
                                                 "focus ON"
                                                 "focus OFF"))})
                              (when @demo-focus-on
                               {:on-keypress
                                (fn [_]
                                  (log "keypress called for demo1"))})
                              {:width "100%-2"
                               ;; :height 10
                               :border :line
                               :ref (fn [el]
                                      (set! js/demo-el el))
                               :style {:bg (when @demo-focus-on
                                             :green)}
                               }
                              ;; focused-style
                              (if @demo-focus-on
                                focused-style
                                #_(m/map-vals not focused-style)))]
   ])

(defn error-ui [{:keys [error] :as props}]
  [:box#error (merge
                   {:align "center"
                    :valign "middle"
                    :border :line
                    :content (str "Oops! Error: " (red error))
                    }
                   props)
   ])

(def debug-box-height 10)

(def app-height
  (if ^boolean goog.DEBUG
    (str "100%-" debug-box-height)
    "100%"))

(defn scterm-app-ui
  "Even though Blessed provides a DOM-like interface its rendering is
  quiet different from DOM because there is no way to link an external
  CSS. The rules we use here are:
  1. Each component could has their own style attributes (width height etc.)
  2. But it must take the value passed in props with higher priority.
  "
  []
  (if @(subscribe [:debug/fullscreen])
    [debug-box {:width "100%"
                :height "100%"
                :lines @(subscribe [:screen/height])
                }]
    [:element
     [error-boundary
      [:layout {:width "100%" :height app-height}
       [keyboard-tips-ui {:width "20%" :height "100%"}]
       [main-panel-ui {:width "80%"
                       :height "100%"}]]]
     (when ^boolean goog.DEBUG
       [debug-box {:width "100%"
                   :lines debug-box-height
                   :top app-height
                   }])]))


(def routes
   [
    ;; The first element is the (virtual) path, and the second item is
    ;; the event to dispatch, where a keyword can be used a shortcut
    ;; to omit the call to dispatch.
    ;; TODO: do we need to add another layer of indirection path => keywords => view function?
    ["/" #'info-view]
    ["/stats" stats-view]
    ["/items" #'items-view]
    ["/requests" #'requests-view]
    ["/logs" #'logs-view]
    ;; ["/logs/filter" #'logs-filter-view]
    ;; ["/abc"]
    ;; ["/items/" :items]
    ;; ["/requests/" :requests]
    ;; ["/items/:item" :job-item]
    ;; ["/logs/" :logs]
    ;; ["/logs/:line" :log-line]
    ])
