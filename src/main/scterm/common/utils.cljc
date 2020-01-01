(ns scterm.common.utils
  (:require [clojure.string :as str]
            [medley.core :as m]
            [cognitect.transit :as t]
            #?(:cljs [cljs-node-io.core :as io :refer [slurp]]
               :clj [clojure.java.io :as io])))

#?(:cljs (def reader (t/reader :json)))

(defn repeat-chars
  [ch n]
  (str/join "" (repeat n ch)))

(defn load-logs-file-transit [path]
  #?(:cljs (->> path
                slurp
                str/split-lines
                (map #(t/read reader %)))
     :clj (with-open [in (io/input-stream (java.io.File. path))]
            (let [reader (t/reader in :json)
                  logs (transient [])
                  read-one (fn []
                             (try
                               (conj! logs (t/read reader))
                               true
                               (catch java.io.EOFException _ false)
                               (catch java.lang.RuntimeException _ false)))]
              (loop []
                (when (read-one)
                  (recur)))
              (persistent! logs)))))

(defn load-logs-file [path]
  (->> (load-logs-file-transit path)
       (map #(m/map-keys keyword %))))


(comment
  (def s0 (lazy-seq [1 2 3]))

  (defn fib
    [x]
     (if (<= x 1)
       1
       (+ (fib (- x 1))
          (fib (- x 2)))))

  (fib 1)

  (println s0)

  (def is1 (io/input-stream (java.io.File. "/tmp/3.txt")))

  (def it (org.apache.commons.io.IOUtils/lineIterator is1 "utf-8"))

  (def s1 (seq it))


  ()


(defn fib
  ([] (fib 1 1))
  ([a b]
   (println (str "a = " a ", b = " b))
   (lazy-seq (cons a
                   (fib b (+ a b))))))


; a = 1, b = 1
; a = 1, b = 2
; a = 2, b = 3
; a = 3, b = 5
; a = 5, b = 8
; a = 8, b = 13

)
