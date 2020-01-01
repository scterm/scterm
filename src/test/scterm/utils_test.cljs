(ns scterm.utils-test
  (:require [cljs.test :refer [is testing]]
            [scterm.log :refer [log]]
            [scterm.test.utils :as tu]
            [scterm.utils :as u]))

(tu/deftest test-urljoin
  (is (= (u/urljoin "http://foo.com/ab/" "/cd") "http://foo.com/cd"))
  (is (= (u/urljoin "http://foo.com/ab/" "/cd/") "http://foo.com/cd/"))
  (is (= (u/urljoin "http://foo.com/ab" "/cd") "http://foo.com/cd"))
  (is (= (u/urljoin "http://foo.com/ab" "cd") "http://foo.com/ab/cd"))

  (testing "with port in url"
    (is (= (u/urljoin "http://foo.com:80/ab" "cd") "http://foo.com:80/ab/cd"))
    (is (=
         (u/urljoin "https://storage.scrapinghub.com" "jobs" "1/2/3")
         "https://storage.scrapinghub.com/jobs/1/2/3"))
    (is (=
         (u/urljoin "http://127.0.0.1:18000/" "/jobs/" "1/2/3")
         "http://127.0.0.1:18000/jobs/1/2/3"))
    )

  (testing "urls without scheme"
    (is (= (u/urljoin "/dir1" "f1") "/dir1/f1"))
    (is (= (u/urljoin "foo.com/dir1" "f1") "foo.com/dir1/f1"))
    (is (= (u/urljoin "foo.com/dir1" "/f1") "foo.com/f1"))
    (is (= (u/urljoin "foo.com/dir1" "/f1" "f2") "foo.com/f1/f2"))))

(def url-parts
  (array-map "http://foo.com/cd"
             {:scheme "http" :netloc "foo.com" :port nil :path "/cd"}

             "http://foo.com"
             {:scheme "http" :netloc "foo.com" :port nil :path nil}

             "foo.com/cd"
             {:scheme nil :netloc "foo.com" :port nil :path "/cd"}

             "/a/b/c"
             {:scheme nil :netloc nil :port nil :path "/a/b/c"}
             
             "/"
             {:scheme nil :netloc nil :port nil :path "/"}

             "http://foo.com:80"
             {:scheme "http" :netloc "foo.com" :port "80" :path nil}))

(tu/deftest test-urlparse
  (run! (fn [[url parts]]
          (is (= (u/urlparse url) parts)))
        url-parts))

(tu/deftest test-urlunparse
  (run! (fn [[url parts]]
          (is (= (u/urlunparse parts) url)))
        url-parts))
