(ns virtuoso.elements.colored-boxes-scenes
  (:require [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.colored-boxes :as colored-boxes]))

(defscene single-box
  (colored-boxes/colored-boxes
   {:boxes [{:text "Bar 1"
             :color-idx 0}]
    :footer {:text "Look at the box"}}))

(defscene a-few-boxes
  (colored-boxes/colored-boxes
   {:boxes [{:text "Bar 1"
             :color-idx 0}
            {:text "Bar 2"
             :color-idx 1}]
    :footer {:text "Two boxes"}}))

(defscene lots-of-boxes
  (colored-boxes/colored-boxes
   {:boxes [{:text "Bar 1"
             :color-idx 0}
            {:text "Bar 2"
             :color-idx 1}
            {:text "Bar 3"
             :color-idx 2}
            {:text "Bar 4"
             :color-idx 3}
            {:text "Bar 5"
             :color-idx 4}
            {:text "Bar 6"
             :color-idx 5}
            {:text "Bar 7"
             :color-idx 6}
            {:text "Bar 8"
             :color-idx 7}]
    :footer {:text "Wow, that's a lot of boxes"}}))
