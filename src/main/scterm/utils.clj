(ns scterm.utils
  (:require [medley.core :as m]
            [re-frame.core :as re-frame :refer [dispatch]]))


(defmacro prog1
  "Execute the `expr` and use it as the final return value. But provides
  a chance to execute the `body` before returning, with the symbol
  `<>` bound to the return value. It's like the prog1 found in emacs
  lisp.

    (prog1 (..)
        (.. do something ...))

  "
  {:style/indent 1}
  [expr & body]
  `(let [~'<> ~expr]
     ~@body
     ~'<>))

(defmacro with-js->clj->js
  "Process js object like a clj, and convert it back to js at the end.

  (def foo #js {:a 0})
  (with-js->clj->js foo
    (assoc some-js-object :a 1))
  "
  [sym & body]
  `(let [~sym (~'js->clj ~sym)
         retval# (do ~@body)]
     (~'clj->js retval#)))


(defmacro deftimer
  "Creates a js timer with a callback to dispatch the given `event`, and
  a function `reset-timer` that is called after each reload to delete
  and re-create the timer, to accomodate callback/interval changes."
  [name event interval]
  (let [current-ns (-> &env :ns :name)
        dispatch-fn-name (-> (str "dispatch-" name) symbol)
        timer-name (-> (str name "-timer") symbol)]
    `(do
       (defn ~dispatch-fn-name
         []
         (dispatch [~event]))
       (when-not scterm.config/*unit-tests*

         (defn ~'create-timer []
           (defonce ~timer-name (js/setInterval ~dispatch-fn-name ~interval)))

         (~'create-timer)

         (def ~(vary-meta 'reset-timer assoc :dev/after-load true)
           (fn []
             (js/clearInterval ~timer-name)
             (ns-unmap (quote ~current-ns) (quote ~timer-name))
             (~'create-timer)))

         ))))

(comment

(require '[clojure.core.async :as async])
(defn square [x] (* x x))

(def xform
  (comp
    (filter even?)
    (filter #(< % 10))
    (map square)
    (map inc)))

(def c (async/chan 1 xform))

(async/go
  (async/onto-chan c [5 6 8 12 15]))

(loop [n (async/<!! c)]
  (when n
    (println n)
    (recur (async/<!! c))))


  )
