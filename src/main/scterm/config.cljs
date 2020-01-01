(ns scterm.config)

(def ^:dynamic *use-cache*
  "Whether to cache job data or not."
  true)

(def ^:dynamic *cache-dir*
  "The directory to create a scterm-cache dir in."
  "/tmp/")

(def ^:dynamic *unit-tests*
  "A flag to disable some modules (e.g. times when running unit tests"
  false)

(def ^:dynamic *hs-api-url* "https://storage.scrapinghub.com")
(def fake-api-url "http://127.0.0.1:18000")

(def ^:dynamic *in-async-test* false)

(defn use-fake-server! []
  (set! *hs-api-url* fake-api-url))

(def ^:dynamic *api-key* nil)
(def ^:dynamic *job-id* nil)

(def ^:dynamic *debug-test* false)
