(ns virtuoso.elements.icon-button
  (:require [phosphor.icons :as icons]))

(def button-icon-size
  {:small {:tiny ["h-2" "w-2"]
           :small ["h-3" "w-3"]
           :medium ["h-4" "w-4"]
           :large ["h-6" "w-6"]}
   :medium {:tiny ["h-2.5" "w-2.5"]
            :small ["h-4" "w-4"]
            :medium ["h-6" "w-6"]
            :large ["h-8" "w-8"]
            :xlarge ["h-10" "w-10"]}
   :large {:tiny ["h-3" "w-3"]
           :small ["h-5" "w-5"]
           :medium ["h-8" "w-8"]
           :large ["h-12" "w-12"]}})

(def theme-class
  {:info "btn-info"
   :warn "btn-error"
   :success "btn-success"})

(defn icon-button [{:keys [text size theme icon icon-after-label icon-size actions disabled?]}]
  (let [size (if (button-icon-size size) size :medium)
        theme (if (theme-class theme) theme :info)
        icon-el (icons/render icon
                              {:class (or (get-in button-icon-size [size icon-size])
                                          (get-in button-icon-size [size :medium]))})]
    [:div.btn.btn-circle
     {:title text
      :class [(when (= :large size) :btn-lg)
              (when (= :small size) :btn-sm)
              (when (= :xsmall size) :btn-xs)
              (when disabled? :btn-disabled)
              (theme-class theme)]
      :on {:click actions}}
     (if icon-after-label
       [:div.flex.items-center.text-lg {:class "gap-0.5"}
        icon-el
        icon-after-label]
       icon-el)]))

(def bare-theme-class
  {:info "text-info"
   :warn "text-error"
   :success "text-success"
   :neutral "text-neutral-content"})

(defn bare-icon-button [{:keys [text size theme icon actions]}]
  (let [theme (if (bare-theme-class theme) theme :neutral)]
    [:div.cursor-pointer
     {:title text
      :class (bare-theme-class theme)
      :on {:click actions}}
     (icons/render icon
                   {:class (or (get-in button-icon-size [:medium size])
                               (get-in button-icon-size [:medium :medium]))})]))
