(ns scterm.react-utils
  (:require [reagent.core :as r]
            ["react" :as react]
            [reagent.impl.component :as comp]
            [reagent.impl.util :as util]
            [scterm.log :refer [log]]
            [goog.object :as gobj]))

(def error-boundary
  "Wrapper component for recovering from exceptions in downstream
  render fns. Creates an error boundary that prevents exceptions from corrupting
  the React component hierarchy.
  Use this component to wrap a single reagent (root) component. Any exception
  thrown in downstream render fns will be caught and logged. The component's
  child and its children will not be rendered.
  This is useful in a reloading-based development workflow.
  Example usage:
  (ns my-ns
    (:require [error-boundary.error-boundary :refer [error-boundary]]))
  (r/render-component (fn [] [error-boundary [root]])
                      (.. js/document (querySelector \"#container\")))
  Note that this relies on the undocumented unstable_handleError API introduced
  in React 15.
  This componenet may have performance implications, so it is recommended to
  enable it only during development."
  (r/adapt-react-class
   (comp/create-class
    {:getInitialState
     (fn []
       #js {:hasError false})

     :getDerivedStateFromError
     (fn []
       #js {:hasError true})

     :componentDidCatch
     (fn [this error errorInfo]
       (log "Error Boundary: error=%s errorInfo=%s" (.-stack error) errorInfo)
       (def einfo (.-stack error)))

     :render
     (fn []
       (this-as this
         (let [children (.. react
                            -Children
                            (toArray (.. ^js this -props -children)))]
           (when (not= 1 (count children))
             (js/console.warn "Component error-boundary requires a single child component. Additional children are ignored."))
           (if (gobj/get (.. this -state) "hasError")
             (do
               (js/console.warn "An error occurred downstream (see errors above). The element subtree will not be rendered.")
               ;; TODO: Draw something even if there is error, instead of showing an empty screen.
               nil)
             (first children)))))})))
