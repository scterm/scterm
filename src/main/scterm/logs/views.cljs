(ns scterm.logs.views
  (:require [reagent.core :as r]
            ["chalk" :as chalk :refer (red)]
            [clojure.string :as str]
            ["moment" :as moment]
            [clojure.core.async :refer [<! go timeout]]
            [re-frame.core :as rf :refer [subscribe dispatch]]
            [oops.core :refer [oget oget+]]
            ["chalk" :as chalk]
            [scterm.utils :as u]
            [scterm.logs.subs]
            [scterm.logs.events]
            [scterm.widgets.spinner :refer [spinner-widget]]
            [scterm.styles :refer [focused-style]]
            [scterm.log :refer [log]]
            [scterm.common.utils :as cu]))

;; Each line is like:
;;
;;      {"time":157484102579,"level":20,"message":"Log opened."}

(defn format-time
  [ts]
  (let [date (.utc ^js moment ts)]
    (when (.isValid date)
      (.format date "YYYY-MM-DD HH:mm:ss"))))

(def level->str {10 "DEBUG"
                 20 "INFO"
                 30 "WARNING"
                 40 "ERROR"})

(defn get-rand-color []
  (let  [color (rand-nth ["bgYellow" "bgCyan" "bgGreen" "bgBlue" "bgRed" "bgWhite"])]
    (oget+ chalk color)))

(defn format-one-line [index entry]
  (let [ts (format-time (:time entry))
        level (level->str (:level entry))]
    (->> (str index
              ": "
              (->> (str ts " " level)
                   (.cyan chalk))
              " "
              (:message entry))
         #_((get-rand-color)))))

(defn format-logs [entries]
  (->> entries
       (map-indexed format-one-line)
       (str/join "\n")))

(defn logs-view-impl [props]
  (r/with-let [el-ref (atom nil)
               init-scrolling (atom false)
               last-scroll-percent (atom -1)
               update-scroll
               (fn []
                 (when-let [el @el-ref]
                   (let [percent (-> (.getScrollPerc el)
                                     (min 100))]
                     (when (and
                            ;; wait until the view scrolls to the
                            ;; saved position (e.g when switched back
                            ;; to the logs tab
                            (not @init-scrolling)
                            ;; Sometimes the scroll event is triggered
                            ;; multiple times in a row. It's hard to
                            ;; de-deduplicate this in events
                            ;; handlers (TODO: Really? The view shall
                            ;; pass the events as-is)
                            (not= @last-scroll-percent percent)
                            (not= (-> @(subscribe [:logs/scroll]) :percent)
                                  percent))
                       (reset! last-scroll-percent percent)
                       (dispatch [:logs/update-scroll
                                 {:base (oget el "childBase")
                                  :percent percent}]
                                )))))]

    (let [{:keys [ready? entries]} @(subscribe [:logs/entries])
          show-loading @(subscribe [:logs/fetching])
          logs-height (if show-loading
                        "100%-3"
                        "100%")
          init-scroll (fn [el]
                        (let [scroll @(subscribe [:logs/scroll])]
                          (when-not (zero? (:base scroll))
                            (reset! init-scrolling true)
                            (.scrollTo el (:base scroll))
                            (reset! init-scrolling false))))]
      (if ready?
        [:box
         [:text#logs (r/merge-props {:width "100%-4"
                                     :height logs-height
                                     :content (format-logs entries)
                                     :scrollable true
                                     :alwaysScroll true
                                     :scrollbar {:style {:bg :white
                                                         :fg :white}
                                                 :track {:bg :blue
                                                         :fg :cyan}}
                                     :vi true
                                     :on-scroll update-scroll
                                     :ref (fn [el]
                                            ;; (log "ref is called")
                                            (dispatch [:logs/ref el])
                                            (reset! el-ref el)
                                            ;; (oget log "foo")
                                            (def vel el)
                                            (when el
                                              (init-scroll el)))}
                                    focused-style
                                    props)]
         (when show-loading
           [:box#logs-loading {:height 3
                               :bottom 0
                               :content "Loading ..."
                               :border :line}])]
       [spinner-widget (r/merge-props props {:title "logs"})]))))

(defn logs-view [props]
  (let [error @(subscribe [:logs/api-error])
        booted @(subscribe [:logs/booted])]
    (if (and error (not booted))
      [u/error-view (merge props {:error error
                                  :id :logs-error})]
      [logs-view-impl props])))

(comment

  (go
    (dotimes [i 3]
      (when-let [el scterm.logs.views/vel]
        (log "scroll to %s" i)
        (.scrollTo el i))
      (<! (timeout 1000))))


  (.scrollTo vel 1)
  (.scrollTo vel 0)

  ())
