(ns virtuoso.elements.musical-notation-selection
  (:require [virtuoso.elements.musical-notation :as mn]))

(defn musical-notation-selection [{:keys [items]}]
  [:ul.menu.menu-horizontal.rounded-box.bg-base-100.gap-2
   (for [{:keys [notation active? actions]} items]
     [:li
      [:a.text-2xl
       {:on {:click actions}
        :class (when active? [:active])}
       (mn/render notation)]])])
