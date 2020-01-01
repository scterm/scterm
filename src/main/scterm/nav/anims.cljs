(ns scterm.nav.anims
  "Provide some animation visual effect by changing the background color
  for a few seconds."
  (:require [medley.core :as m]
            [scterm.config]
            [scterm.utils :refer [deftimer]]
            [re-frame.core :as re-frame :refer [dispatch]]))

;; (def colors-transitions [:red :green :cyan])
(def colors-transitions [:#3DFFF0 :#B4F9FF :#D9F4FF])
;; (def colors-transitions [:ff0000 :00ff00 :0000ff])
(def anim-time-mescs 3000)
(def max-trans (count colors-transitions))

(defn update-anims [anims]
  (let [update-one (fn [old]
                     (let [new (inc old)]
                       (if (> new max-trans)
                         nil
                         new)))]
    (->> anims
         (m/filter-vals some?)
         (m/map-vals update-one))))

(deftimer nav-counters-updates :nav/update-anims (/ anim-time-mescs max-trans))

(comment
  (macroexpand-1 '(deftimer nav-counters-updates :nav/update-anims 100))
  )
