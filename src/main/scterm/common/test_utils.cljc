(ns scterm.common.test-utils
  (:require [scterm.common.utils :as cu]))

(def correct-api-key "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
(def incorrect-api-key (cu/repeat-chars "y" 32))
(def running-job-id "1/1/1")
(def finished-job-id "1/1/2")
(def error-job-id "1/1/3")
(def nonexisting-job-id "100/100/100")
