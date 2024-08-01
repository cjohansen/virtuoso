(ns virtuoso.scenes
  (:require [portfolio.ui :as ui]
            [replicant.dom :as replicant]
            [virtuoso.elements.bar-scenes]
            [virtuoso.elements.brain-scenes]
            [virtuoso.elements.button-panel-scenes]
            [virtuoso.elements.button-scenes]
            [virtuoso.elements.colored-boxes-scenes]
            [virtuoso.elements.form-scenes]
            [virtuoso.elements.icon-button-scenes]
            [virtuoso.elements.layout-scenes]
            [virtuoso.elements.metronome-scenes]
            [virtuoso.elements.typography-scenes]))

:virtuoso.elements.bar-scenes/keep
:virtuoso.elements.brain-scenes/keep
:virtuoso.elements.button-panel-scenes/keep
:virtuoso.elements.button-scenes/keep
:virtuoso.elements.colored-boxes-scenes/keep
:virtuoso.elements.form-scenes/keep
:virtuoso.elements.icon-button-scenes/keep
:virtuoso.elements.layout-scenes/keep
:virtuoso.elements.metronome-scenes/keep
:virtuoso.elements.typography-scenes/keep

(replicant/set-dispatch! #(prn %3))

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
