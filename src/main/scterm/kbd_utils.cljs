(ns scterm.kbd-utils
  (:require [cuerdas.core :as cstr]))

(defn text? [key]
  (and (= (count key) 1)
       #_(or (cstr/alnum? key))))

(defn get-char
  "
  p          => {:sequence 'p', :name 'p', :ctrl false, :meta false, :shift false, :full 'p'}
  shift-p    => {:sequence 'P', :name 'p', :ctrl false, :meta false, :shift true, :full 'S-p'}
  backspace  => {:sequence '', :name 'backspace', :ctrl false, :meta false, :shift false, :full 'backspace'}
  1          => {:ch '1', :full '1'}
  "
  [keyobj]
  ;; (log keyobj)
  (if (and (:sequence keyobj) (not (:ctrl keyobj)))
    (let [name (:name keyobj)]
      (if (and (text? name) (:shift keyobj))
        (cstr/upper name)
        name))
    (:ch keyobj)))
