(ns scterm.routing
  "Routing mimics the BOM history api and a router like reitit and bidi.
  It provides two key features:
  (1) an api to go backward and forward
  (2) a mapping betwen paths and views (or view dispatching).
  "
  (:require [clojure.spec.alpha :as s]
            [cuerdas.core :as cstr]
            [re-frame.core :refer [dispatch path reg-event-db]]
            [scterm.log :refer [log]]
            [scterm.utils :as u]
            [scterm.specs :as sp :refer-macros [self-or-var]]))

(s/def ::path (s/and string? not-empty #(cstr/starts-with? % "/")))
(s/def ::fn-view ::sp/callable)

(s/def ::ui ::sp/callable)
(s/def ::title-sub (s/cat :sub keyword? :args (s/* any?)))
(s/def ::map-view (s/keys ::req-un [::ui ::title-sub]))
(s/def ::view (s/or :fn ::fn-view :map ::map-view))

(s/def ::route (s/tuple ::path ::view))
(s/def ::routes_ (s/coll-of ::route))
(s/def ::routes (self-or-var ::routes_))

(comment
  (def x (s/conform ::routes scterm.views.app/routes))
  (s/explain-str ::routes scterm.views.app/routes)
  )

(def routing-path [(path :routing)])
(declare new-router
         path->view
         remove-trailing-slash
         pop-state
         push-state
         goto-path)

(reg-event-db
 :routing/init
 (fn [db [_ routes]]
   (assoc db :routing (new-router routes))))

(reg-event-db
 :routing/push-state
 routing-path
 (fn [routing [_ path]]
   #_(log "routing to %s" path)
   (push-state routing path)))

(defn- push-state [routing path]
  (let [oldpath (:current-path routing)
        path (remove-trailing-slash path)]
    (if (= oldpath path)
      (do #_(log "current path is already %s, nothing to do" path)
          routing)
      (goto-path routing path oldpath))))

(defn- update-stack [stack entry]
  (-> stack
      (conj entry)
      (u/take-lastv 10)))

(defn- goto-path [routing path oldpath]
  (let [[view args] (path->view path (:routes routing))]
    (if view
      (dispatch [:nav/set-active-view [view args]])
      (log "warning: no view matched for path %s" path)))
    (-> routing
        (assoc :current-path path)
        (update :stack update-stack oldpath)))

(reg-event-db
 :routing/pop-state
 routing-path
 (fn [routing _]
   (if (empty? (:stack routing))
     (do #_(log "warning: trying to pop when stack is empty")
         routing)
     (pop-state routing))))

(defn- pop-state [routing]
  (let [oldpath (:current-path routing)
        path (last (:stack routing))
        [view args] (path->view path (:routes routing))]
    ;; (log "popping state, %s => %s" oldpath path)
    (if view
      (do (dispatch [:nav/set-active-view [view args]])
          (-> routing
              (assoc :current-path path)
              (update :stack pop)))
      (do (log "warning: no view matched for path %s" path)
          routing))))

(defn new-router [routes]
  (s/assert ::routes routes)
  {:routes routes
   :stack []
   :current-path "/"
   })

(defn remove-trailing-slash [path]
  (str "/" (cstr/trim path "/")))

;; TODO: use the returned value of s/conform instead of doing the
;; if-check again here.?
(defn wrap-view [view]
  (if (fn? view)
    {:ui view}
    view))

(defn path->view [path routes]
  ;; TODO: add support for params
  (let [routes (cond-> routes
                 (instance? cljs.core/Var routes)
                 deref)
        path (remove-trailing-slash path)
        view (some (fn [[rpath view]]
                     (when (= rpath path)
                       view))
                   routes)]
    [(some-> view wrap-view)]))

(s/check-asserts true)
