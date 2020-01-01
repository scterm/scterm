(ns scterm.old
  "Archived old code here. Maybe useful in the future")

#_(defn simple-component []
  [:box {:top :center
         :left :center
         :width "100%" :height "100%"
         :border {:type :line}
         :style {:fg :white,
                 :bg :magenta,
                 :border {:fg "#f0f0f0"}
                 :hover {:bg :green}
                 }
         :on-click (fn [_] (swap! app-data update :count inc))
         }
   [:> Table {:keys true
              :fg :white
              :selectedFg :white
              :selectedBg :blue
              :interactive true
              ;; :label "Active Processes"
              :width "80%"
              :height "100%"
              :boder {:type :line :fg :cyan}
              :columnSpacing 10
              :columnWidth [10 20]
              :data {:headers ["Name" "Value"]
                     :data (seq info)}}]
   #_(str "You have clicked " (:count @app-data) " times, Yay!")
   #_[:> Line {:ref "line"
               :data line-data
               :style {:style {:text "blue" :baseline "black"}}
               :showLegend true
               :height "50%"
               :top "0%"
               :label "Fruits"
               }]
   #_[:> Bar {:label (str "Server utilization (%" (:count @app-data) ")")
              :ref "bar"
              :barWidth 4
              :barSpacing 6
              :xOffset 3
              :maxHeight 9
              :data {"titles" #js ["bar1" "bar2"]
                     "data" #js [5 10]}
              }]

   #_[:> Carousel {:interval 3000 :controlKeys true :screen screen}
      [:textbox "hello"]
      [:textbox "world"]]
   ])


#_(a/go
  (dotimes [_ 10]
      (a/<! (a/timeout 1000))
      (swap! app-data update :count inc)))

#_(a/go
  (<! (a/timeout 2000))
  (swap! app-data assoc :box-text "Hi3!")
  )

#_(defn mytest []
  (a/go-loop []
    (a/<! (a/timeout 2000))
    (log "toggle!")
    (swap! app-data update :show not)
    (when (:mytest @app-data true) (recur))))

#_[:> Table {:keys true
               :fg :white
               ;; :selectedFg :white
               ;; :selectedBg :blue
               ;; :focused true
               :interactive true
               ;; :label "Active Processes"
               :top topbar-height
               :left "20%"
               :width "80%"
               :border {:type :line :fg :cyan}
               :columnSpacing 10
               :columnWidth [10 20]
               :style {
                       :border {:type :line :fg :green}
                       :selected {:fg :while
                                  :bg :blue}
                       }
               :data {:headers ["Name" "Value"]
                      :data (seq info-data)}}]

;; drafts
(comment
  (.readFile fs "/tmp/spider.png"
             (fn [err data] (swap! app-data assoc :img data :err err)))

  (count (:img @app-data))
  )

#_[taoensso.timbre :as timbre :rename {log tlog}]
#_[taoensso.timbre.appenders.core :refer [split-appender]]
#_(defn init-log! []
  (timbre/merge-config!
   {:appenders
    {:spit (spit-appender {:fname "/tmp/scterm.log"})}}))
#_(defn log [msg]
  (tlog msg))

#_(defn searchbar-ui []
  ;; TODO: wrap this ref logic in a custom component
  (let [el (reagent/atom nil)]
    (fn [props]
      (log "searchbar-ui is called with focus = %s"
           @(subscribe [:nav/search-focused?]))
      [:textarea
       (merge-props {:border {:type :line}
                     ;; :content @(subscribe [:nav/search-text])
                     :ref #(reset! el %)
                     :input true
                     :on-keypress
                     (fn [ch keyobj]
                       (def vargs [ch keyobj])
                       (def vel el)
                       (let [key (.-name keyobj)
                             search-text (.-value @el)]
                         (log "searchbar keypress with %s, val = %s" key search-text)
                         (dispatch [:stats/update-search-text search-text])
                         #_(if (= key "return")
                           (dispatch [:nav/unfocus-search-box])
                           (dispatch [:stats/update-search-text search-text]))
                         ))
                     }
                    props
                    (when @(subscribe [:nav/search-focused?])
                      {:focused true
                       :keys true
                       :style {:bg :cyan :border {:fg :green}}
                       }))])))

;; (comment
;; (do
;;   (def c1 (Confirm. #js {:name "question" :message "Did you like me"}))
;;   (-> c1 (.run) (.then #(println "answer is" %)))
;;   )
;; )
