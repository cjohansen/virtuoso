(ns virtuoso.scenes
  (:require [portfolio.ui :as ui]
            [virtuoso.elements.brain-scenes]))

:virtuoso.elements.brain-scenes/keep

(defonce app
  (ui/start!
   {:config
    {:css-paths ["/tailwind.css"]
     :canvas-path "canvas.html"
     :background/options
     [{:id :dracula
       :title "Dracula"
       :value {:background/background-color "#212227"
               :background/document-class "dracula"}}]}}))
