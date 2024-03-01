(ns virtuoso.elements.brain-scenes
  (:require [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.brain :as brain]))

(defscene brain
  (brain/brain))

(defscene brain-only
  (brain/brain {:text? false}))
