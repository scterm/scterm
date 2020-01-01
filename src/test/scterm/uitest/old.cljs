(ns scterm.uitest.old
  "Archived old code here. Maybe useful in the future")

#_(deftest foo-test
  (cljs.test/async done
    (let [foo (atom false)
          _ (go
              (<! (timeout 500))
              (reset! foo true))
          wait-chan (go-loop [total-wait 0]
                      (let [interval 100]
                        (if (or #_(bu/select :infobar)
                                @foo
                                (>= total-wait 1000))
                         (do
                           (log "current wait = %s" total-wait)
                           :ok)
                         (do
                           (<! (timeout interval))
                           (recur (+ total-wait interval))))))]
      (go
        (let [result (<! wait-chan)]
          (log "result = %s" (str result))
          (is (bu/select :infobar))
          (done))))))

#_(defmacro defuitest [name & body]
  (let [impl (-> name (str "-impl") symbol)]
    `(do
       ;; Add a layer of function wrapper so that cljs.test reporter
       ;; could print the correct call site.
       (defn ~impl []
         ~@body)
       (cljs.test/deftest ~name
         (async ~'done
           (js/setTimeout
            (fn []
              (try
                (run-test-sync
                 (~impl))
                ;; The async block is not protected by
                ;; cljs.test/test-var-block* because it is, well, async.
                (catch :default e#
                  (cljs.test/do-report
                   {:type :error
                    :message "Uncaught exception, not in assertion."
                    :expected nil
                    :actual e#}))
                (finally
                  (~'done)
                  )))
            100))))))
