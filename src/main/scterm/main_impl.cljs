(ns scterm.main-impl
  "The entry of the app. Refactored out as a standalone module so it
  could be shared beteween main app and the ui tests."
  (:require [re-frisk-remote.core :refer [enable-re-frisk-remote!]]
            [devtools.core :as devtools]
            [reagent.core :as reagent]
            [re-frame.core :refer [dispatch-sync]]
            ;; for registering :http-xhrio fx
            [day8.re-frame.http-fx]
            ["react-blessed" :as react-blessed :refer [getBlessedReconciler]]
            ;; project deps
            [scterm.fixes]
            [scterm.react-devtools]
            [user]
            [scterm.routing]
            [scterm.debug.logger :as debug-logger]
            [scterm.db :refer [screen]]
            [scterm.screen :refer [init-screen render reset-screen]]
            [scterm.log :refer [log]]
            [scterm.app.views :refer [scterm-app-ui routes]]
            ;; [dirac.runtime]
            ))

(defn show []
  (let [el (-> scterm-app-ui
               reagent/reactify-component
               (reagent/create-element #js {}))]
    (render el @screen)))

(defn before-reload []
  (reset-screen @screen)
  (render nil @screen))

(defn after-reload [_]
  (show))

(defn start-app! [{:keys [job-id api-key test]}]
  ;; The logger setup shall be called here instead of the main
  ;; function, otherwise the errors printed by cli-matic would not be
  ;; visible.
  (dispatch-sync [:initialize {:api-key api-key
                               :job-id job-id
                               :test test
                               :routes #'routes}])
  (show)

  ;; (.injectIntoDevTools (getBlessedReconciler))
  (reset! scterm.react-devtools/app-ready? true))

(defn setup-app!
  [{:keys [test]}]
  ;; The logger setup shall be called here instead of the main
  ;; function, otherwise the errors printed by cli-matic would not be
  ;; visible.
  (debug-logger/setup! test)
  (when test
    (log "Running in test mode"))
  (when ^boolean goog.DEBUG
    ;; TODO: use preloads instead of requiring & enabling here
    (enable-re-frisk-remote! {:enable-re-frame-10x? true})
    (devtools/install!)
    ;; (dirac.runtime/install! :all)
    )
  (init-screen))
