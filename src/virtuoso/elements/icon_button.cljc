(ns virtuoso.elements.icon-button
  (:require [phosphor.icons :as icons]))

(def size? #{:medium :large})

(def button-icon-size
  {:medium {:medium ["h-4" "w-4"]
            :large ["h-8" "w-8"]}
   :large {:medium ["h-6" "w-6"]
           :large ["h-12" "w-12"]}})

(def theme-class
  {:normal "btn-info"
   :warn "btn-error"})

(defn icon-button [{:keys [text size theme icon icon-size actions disabled?]}]
  (let [size (if (size? size) size :medium)
        icon-size (if (size? icon-size) icon-size :medium)
        theme (if (theme-class theme) theme :normal)]
    [:div.btn.btn-circle
     {:title text
      :class [(when (= :large size) :btn-lg)
              (when disabled? :btn-disabled)
              (theme-class theme)]
      :on {:click actions}}
     (icons/render icon
      {:class (get-in button-icon-size [size icon-size])})]))
