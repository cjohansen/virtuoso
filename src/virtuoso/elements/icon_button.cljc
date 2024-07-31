(ns virtuoso.elements.icon-button
  (:require [phosphor.icons :as icons]))

(def size? #{:small :medium :large})

(def button-icon-size
  {:small {:small ["h-3" "w-3"]
           :medium ["h-4" "w-4"]
           :large ["h-6" "w-6"]}
   :medium {:small ["h-4" "w-4"]
            :medium ["h-6" "w-6"]
            :large ["h-8" "w-8"]}
   :large {:small ["h-5" "w-5"]
           :medium ["h-8" "w-8"]
           :large ["h-12" "w-12"]}})

(def theme-class
  {:info "btn-info"
   :warn "btn-error"
   :success "btn-success"})

(defn icon-button [{:keys [text size theme icon icon-size actions disabled?]}]
  (let [size (if (size? size) size :medium)
        icon-size (if (size? icon-size) icon-size :medium)
        theme (if (theme-class theme) theme :info)]
    [:div.btn.btn-circle
     {:title text
      :class [(when (= :large size) :btn-lg)
              (when (= :small size) :btn-sm)
              (when disabled? :btn-disabled)
              (theme-class theme)]
      :on {:click actions}}
     (icons/render icon
      {:class (get-in button-icon-size [size icon-size])})]))
