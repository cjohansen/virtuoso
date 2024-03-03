(ns virtuoso.elements.button-panel
  (:require [phosphor.icons :as icons]))

(defn icon-button [{:keys [text size icon actions disabled?]}]
  [:div.btn.btn-circle.btn-info.btn
   {:title text
    :class [(when (= :large size)
              :btn-lg)
            (when disabled? :btn-disabled)]
    :on {:click actions}}
   (icons/render icon {:class (if (= :large size)
                                "h-6 w-6"
                                "h-4 w-4")})])

(defn button-panel [{:keys [buttons]}]
  [:nav
   [:div.flex.items-center.gap-4.justify-center
    (map icon-button buttons)]
   (when-let [kbds (seq (keep :kbd buttons))]
     [:div.flex.items-center.gap-8.justify-center.mt-4
      (for [k kbds]
        [:kbd.kbd.kbd-sm k])])])
