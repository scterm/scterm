(ns scterm.nav.subs
  (:require ["moment" :as moment]
            [blessed :refer (parseTags)]
            [cuerdas.core :as cstr]
            [re-frame.core :refer [reg-sub subscribe]]
            [scterm.nav.anims :as anims]
            [scterm.app.tabs :as tabs]
            [scterm.log :refer [log]]))

(reg-sub
 :nav
 (fn [db _] (:nav db)))

(reg-sub
 :routing
 (fn [db _] (:routing db)))

(reg-sub
 :routing/current-path
 :<- [:routing]
 (fn [routing _] (:current-path routing)))

(reg-sub
 :nav/anims
 :<- [:nav]
 (fn [nav _] (:anims nav)))

(reg-sub
 :nav/active-view
 :<- [:nav]
 (fn [nav _] (:active-view nav)))

(reg-sub
 :nav/active-tab-index
 :<- [:routing/current-path]
 (fn [current-path _]
   (tabs/path->index current-path)))

(reg-sub
 :ui/infobar-text
 (fn [db _]
   (let [routing (:routing db)
         path (-> routing
                  :current-path
                  (cstr/ltrim "/")
                  (cstr/capital))
         title-sub (some-> (get-in db [:nav :active-view])
                           first
                           :title-sub)
         job-name (str (:job-id db)
                       (when-let [spider-name
                                  (some-> db :info :details :spider)]
                         (str " " spider-name)))]
     (parseTags
      (cstr/format
       ;; TODO: add spider name
       (str "{bold}%(job-name)s > {green-fg}%(path)s{/}"
            (when title-sub
              (when-let [title @(subscribe title-sub)]
                (str " > " title))))
       {:job-name job-name
        :path path})))))

(reg-sub
 :nav/counter-color
 :<- [:nav/anims]
 (fn [anims [_ kind]]
   (when-let [step (kind anims)]
     ;; step could be 0 to max-trans
     (get anims/colors-transitions step))))
