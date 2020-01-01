(ns scterm.widgets.list-test
  (:require [scterm.widgets.list :refer [list-widget]]
            [cljs.test :refer [deftest is]]))

#_(deftest list-widget-test
  (let [data (atom nil)
        widget ((list-widget) {:items ["item 0"
                                       "item 1"
                                       "item 2"]
                               :on-select (fn [item index]
                                            (reset! data {:item item :index index}))
                               })
        on-keypress (:on-keypress (second widget))
        down #(on-keypress "" (clj->js {"name" "down"}))
        up   #(on-keypress "" (clj->js {"name" "up"}))]

    (down)
    (is (= @data {:index 1 :item "item 1"}))

    (down)
    (is (= @data {:index 2 :item "item 2"}))

    (down)
    (is (= @data {:index 0 :item "item 0"}))

    (up)
    (is (= @data {:index 2 :item "item 2"}))

    (up)
    (is (= @data {:index 1 :item "item 1"}))

    (up)
    (is (= @data {:index 0 :item "item 0"}))
    ))
