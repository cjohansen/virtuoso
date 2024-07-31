(ns virtuoso.elements.icon-button-scenes
  (:require [phosphor.icons :as icons]
            [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.icon-button :as icon-button]))

(defscene default-button
  (icon-button/icon-button
   {:text "Click"
    :icon (icons/icon :phosphor.regular/music-notes-plus)}))

(defscene small-buttons
  [:div.flex.gap-8
   (icon-button/icon-button
    {:text "Click"
     :icon (icons/icon :phosphor.regular/music-notes-plus)
     :icon-size :small
     :size :small})
   (icon-button/icon-button
    {:text "Click"
     :icon (icons/icon :phosphor.regular/music-notes-plus)
     :icon-size :medium
     :size :small})
   (icon-button/icon-button
    {:text "Click"
     :icon (icons/icon :phosphor.regular/music-notes-plus)
     :icon-size :large
     :size :small})])

(defscene medium-buttons
  [:div.flex.gap-8
   (icon-button/icon-button
    {:text "Click"
     :icon (icons/icon :phosphor.regular/play)
     :icon-size :small
     :size :medium})
   (icon-button/icon-button
    {:text "Click"
     :icon (icons/icon :phosphor.regular/play)
     :icon-size :medium
     :size :medium})
   (icon-button/icon-button
    {:text "Click"
     :icon (icons/icon :phosphor.regular/play)
     :icon-size :large
     :size :medium})])

(defscene large-buttons
  [:div.flex.gap-8
   (icon-button/icon-button
    {:text "Click"
     :icon (icons/icon :phosphor.regular/play)
     :icon-size :small
     :size :large})
   (icon-button/icon-button
    {:text "Click"
     :icon (icons/icon :phosphor.regular/play)
     :icon-size :medium
     :size :large})
   (icon-button/icon-button
    {:text "Click"
     :icon (icons/icon :phosphor.regular/play)
     :icon-size :large
     :size :large})])

(defscene themes
  [:div.flex.gap-8
   (icon-button/icon-button
    {:text "Click"
     :icon (icons/icon :phosphor.regular/music-notes-simple)
     :theme :info})
   (icon-button/icon-button
    {:text "Click"
     :icon (icons/icon :phosphor.regular/music-notes-minus)
     :theme :warn})
   (icon-button/icon-button
    {:text "Click"
     :icon (icons/icon :phosphor.regular/music-notes-plus)
     :theme :success})])
