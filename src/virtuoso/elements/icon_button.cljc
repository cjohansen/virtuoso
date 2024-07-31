(ns virtuoso.elements.icon-button
  (:require [phosphor.icons :as icons]))

(defn icon-button [{:keys [text size icon actions disabled?]}]
  [:div.btn.btn-circle.btn-info.btn
   {:title text
    :class [(when (= :large size) :btn-lg)
            (when disabled? :btn-disabled)]
    :on {:click actions}}
   (icons/render icon {:class (if (= :large size)
                                "h-6 w-6"
                                "h-4 w-4")})])
