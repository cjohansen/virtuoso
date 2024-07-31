(ns virtuoso.elements.icon-button-scenes
  (:require [phosphor.icons :as icons]
            [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.icon-button :as icon-button]))

(defscene default-button
  (icon-button/icon-button
   {:text "Click"
    :icon (icons/icon :phosphor.regular/music-notes-plus)}))

(defscene large-icon
  (icon-button/icon-button
   {:text "Click"
    :icon (icons/icon :phosphor.regular/music-notes-plus)
    :icon-size :large}))

(defscene large-button
  (icon-button/icon-button
   {:text "Click"
    :icon (icons/icon :phosphor.regular/music-notes-plus)
    :size :large}))

(defscene large-button-large-icon
  (icon-button/icon-button
   {:text "Click"
    :icon (icons/icon :phosphor.regular/music-notes-plus)
    :size :large
    :icon-size :large}))

(defscene warn-theme
  (icon-button/icon-button
   {:text "Click"
    :icon (icons/icon :phosphor.regular/music-notes-minus)
    :icon-size :large
    :theme :warn}))
