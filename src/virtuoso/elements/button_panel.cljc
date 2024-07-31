(ns virtuoso.elements.button-panel
  (:require [virtuoso.elements.icon-button :as icon-button]))

(defn button-panel [{:keys [buttons]}]
  [:nav
   [:div.flex.items-center.gap-4.justify-center
    (map icon-button/icon-button buttons)]
   (when-let [kbds (seq (keep :kbd buttons))]
     [:div.flex.items-center.gap-8.justify-center.mt-4.max-md:hidden
      (for [k kbds]
        [:kbd.kbd.kbd-sm k])])])
