(ns virtuoso.elements.button-panel-scenes
  (:require [phosphor.icons :as icons]
            [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.button-panel :as panel]))

(defscene button-panel
  (panel/button-panel
   {:buttons [{:text "Lower BPM"
               :icon (icons/icon :phosphor.bold/minus)
               :kbd "-"}
              {:text "Previous phrase"
               :icon (icons/icon :phosphor.fill/skip-back)
               :kbd "p"}
              {:text "Play"
               :icon (icons/icon :phosphor.fill/play)
               :size :large
               :kbd "space"}
              {:text "Next phrase"
               :icon (icons/icon :phosphor.fill/skip-forward)
               :kbd "n"}
              {:text "Bump BPM"
               :icon (icons/icon :phosphor.bold/plus)
               :kbd "+"}]}))

(defscene button-panel-pause-button
  (panel/button-panel
   {:buttons [{:text "Lower BPM"
               :icon (icons/icon :phosphor.bold/minus)
               :disabled? true
               :kbd "-"}
              {:text "Previous phrase"
               :icon (icons/icon :phosphor.fill/skip-back)
               :disabled? true
               :kbd "p"}
              {:text "Pause"
               :icon (icons/icon :phosphor.fill/pause)
               :size :large
               :kbd "space"}
              {:text "Next phrase"
               :icon (icons/icon :phosphor.fill/skip-forward)
               :kbd "n"}
              {:text "Bump BPM"
               :icon (icons/icon :phosphor.bold/plus)
               :kbd "+"}]}))

(defscene button-panel-disabled-buttons
  (panel/button-panel
   {:buttons [{:text "Lower BPM"
               :icon (icons/icon :phosphor.bold/minus)
               :disabled? true
               :kbd "-"}
              {:text "Previous phrase"
               :icon (icons/icon :phosphor.fill/skip-back)
               :disabled? true
               :kbd "p"}
              {:text "Play"
               :icon (icons/icon :phosphor.fill/play)
               :size :large
               :kbd "space"}
              {:text "Next phrase"
               :disabled? true
               :icon (icons/icon :phosphor.fill/skip-forward)
               :kbd "n"}
              {:text "Bump BPM"
               :disabled? true
               :icon (icons/icon :phosphor.bold/plus)
               :kbd "+"}]}))
