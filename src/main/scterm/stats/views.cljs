(ns scterm.stats.views
  (:require [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :refer [merge-props]]
            ;; for side effects
            [scterm.stats.events]
            [scterm.stats.subs]
            [scterm.screen :refer [demo-focus-on]]
            [scterm.styles :refer [focused-style]]
            [scterm.widgets.spinner :refer [spinner-widget]]))

(defn stats-ui [props]
  (let [booted @(subscribe [:info/booted])]
    (if-not booted
      [spinner-widget (merge-props props {:title "job stats"})]
      [:box#stats
       (merge props
              (when @demo-focus-on focused-style)
              {:scrollable true
               ;; :left 2
               :alwaysScroll true
               :on-keypress (fn [ch keyobj]
                              (def vch ch)
                              (def vkeyobj keyobj)
                              ;; TODO: clean the keyobj before
                              ;; dispatching it, instead of doing the
                              ;; cleaning in the events handler
                              (dispatch [:stats/keypress
                                         (js->clj keyobj :keywordize-keys true)]))
               :scrollbar true
               ;; TODO: if filter result is empty, show a "no mactch result found text."
               :content @(subscribe [:stats/filtered-stats])})])))

(def stats-view {:ui #'stats-ui
                 :title-sub [:stats/title]})
