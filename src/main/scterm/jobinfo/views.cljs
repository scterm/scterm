(ns scterm.jobinfo.views
  (:require [reagent.core :as reagent :refer [merge-props]]
            [re-frame.core :refer [subscribe]]
            [scterm.log :refer [log]]
            ;; for side effects
            [scterm.jobinfo.events]
            [scterm.jobinfo.subs]
            [scterm.utils :as u]
            [scterm.widgets.spinner :refer [spinner-widget]]))

(defn info-view-impl
  [props]
  ;; (def vp props)
  ;; (def vchildren (r/children (r/current-component)))
  (let [booted @(subscribe [:info/booted])]
    (if-not booted
      [spinner-widget (merge-props props {:title "job info"})]
      [:box props @(subscribe [:info/summary])])))

(defn info-view [props]
  (let [error @(subscribe [:info/api-error])
        booted @(subscribe [:info/booted])]
    (if (and error (not booted))
      [u/error-view (merge props {:error error
                                  :id :info-error})]
      [info-view-impl props])))
