(ns scterm.routing-test
  (:require [cljs.test :refer [deftest is]]
            [day8.re-frame.test :refer [run-test-sync]]
            [re-frame.core :refer [dispatch reg-sub subscribe]]
            [scterm.log :refer [log]]
            [scterm.routing :refer [path->view remove-trailing-slash]]
            scterm.test.env
            [scterm.test.utils :as tu]))

(deftest test-remove-trailing-slash
  (let [pairs {"/abc" "/abc"
               "/"    "/"
               "/abc/" "/abc"}]
    (doseq [[in out] pairs]
      (is (= (remove-trailing-slash in) out)))))

(defn test-fixtures []
  (reg-sub
   :test-routing
   (fn [db _] (:routing db))))

(deftest test-path->view []
  (let [view1 (fn [])
        view2 (fn [])
        view0 (fn [])
        routes [["/"   view0]
                ["/p1" view1]
                ["/p2" view2]]]
    (is (= (path->view "/" routes)   [{:ui view0}]))
    (is (= (path->view "/p2" routes) [{:ui view2}]))
    (is (= (path->view "/p3" routes) [nil]))))

(deftest test-push-pop
  (run-test-sync
   (test-fixtures)
   (let [routing    (subscribe [:test-routing])
         routes     [["/"      (fn [_] [])]
                     ["/stats" (fn [_] [])]]
         push-state #(dispatch [:routing/push-state %])
         pop-state  #(dispatch [:routing/pop-state])]
     (dispatch [:routing/init routes])

     ;; TODO: in the future we might need to mock out the call
     ;; to "dispatch [:nav/set-active-view]"
     (push-state "/stats")
     (is (= (:current-path @routing) "/stats"))
     (is (= (:stack @routing) ["/"]))

     ;; nothing shall change if push to the same path
     (push-state "/stats")
     (is (= (:current-path @routing) "/stats"))
     (is (= (:stack @routing) ["/"]))

     (pop-state)
     (is (= (:current-path @routing) "/"))
     (is (= (:stack @routing) []))

     ;; pop state isn't allowed when no history is recorded
     (pop-state)
     (is (= (:current-path @routing) "/"))
     (is (= (:stack @routing) []))
     )))
