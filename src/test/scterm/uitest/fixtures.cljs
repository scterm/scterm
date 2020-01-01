(ns scterm.uitest.fixtures
  (:require [scterm.main-impl :refer [setup-app! start-app! before-reload]]
            [scterm.db :refer [screen]]
            [scterm.common.test-utils :as test.common]
            [scterm.test.failfast :refer [setup-fail-fast]]))

(def uitest-global-fixture
  {:before
   (fn []
     ;; Put it here instead of at top level because we want to run
     ;; tests in a live app.
     (setup-fail-fast)
     (setup-app! {:test true}))

   :after
   (fn []
     (.destroy @screen))})

(def uitest-fixture
  {:before
   (fn []
     ;; (setup-fail-fast)
     (start-app! {:api-key test.common/correct-api-key
                  :job-id test.common/running-job-id
                  :test true}))

   :after
   (fn []
     (before-reload))})

(def uitest-error-fixture
  {:before
   (fn []
     ;; (setup-fail-fast)
     (start-app! {:api-key test.common/incorrect-api-key
                  :job-id test.common/running-job-id
                  :test true}))

   :after
   (fn []
     (before-reload))})
