(ns virtuoso.scenes
  (:require [portfolio.ui :as ui]
            [virtuoso.elements.brain-scenes]
            [virtuoso.elements.form-scenes]
            [virtuoso.elements.layout-scenes]
            [virtuoso.elements.typography-scenes]))

:virtuoso.elements.brain-scenes/keep
:virtuoso.elements.form-scenes/keep
:virtuoso.elements.layout-scenes/keep
:virtuoso.elements.typography-scenes/keep

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