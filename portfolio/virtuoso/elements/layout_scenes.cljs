(ns virtuoso.elements.layout-scenes
  (:require [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.layout :as layout]
            [virtuoso.elements.typography :as t]))

(defscene box
  (layout/box {}
   (t/h2 "A box")
   (t/p "This here is a real nice box")))
