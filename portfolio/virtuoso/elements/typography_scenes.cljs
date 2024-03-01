(ns virtuoso.elements.typography-scenes
  (:require [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.typography :as typo]))

(defscene h1
  (typo/h1 "A top level heading"))

(defscene h2
  (typo/h2 "A second level heading"))

(defscene p
  (typo/p "A paragraph of text"))

(defscene typography
  [:div
   (typo/h1 "Some example text")
   (typo/p "This is a demonstration of the typography with multiple elements.")
   (typo/h2 "Important!")
   (typo/p "This is a further demonstration of the typography with multiple elements.")])
