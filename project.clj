;; The whole point of this file is to
;; 1. be able to run "lein re-frisk" command to start the
;;    re-frisk-remote server.
;; 2. clj-refactor require this to identify the project root.
(defproject scterm "0.1.0-SNAPSHOT"
  :plugins [[lein-re-frisk "0.5.8"]])
