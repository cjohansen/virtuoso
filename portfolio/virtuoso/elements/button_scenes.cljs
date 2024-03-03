(ns virtuoso.elements.button-scenes
  (:require [phosphor.icons :as icons]
            [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.button :as button]))

(defscene button
  (button/button {:text "Click it"}))

(defscene button-spinner
  (button/button {:text "Click it"
                  :spinner? true}))

(defscene button-left-icon
  (button/button {:text "Click it"
                  :left-icon (icons/icon :phosphor.regular/metronome)}))

(defscene button-right-icon
  (button/button {:text "Click it"
                  :right-icon (icons/icon :phosphor.regular/metronome)}))
