(ns virtuoso.elements.badge)

(defn badge [{:keys [text]}]
  [:div.flex.justify-center.text-8xl text])

(def themes
  {:neutral "border-neutral"
   :success "border-success"})

(defn round-badge [{:keys [text label theme]}]
  [:div.flex.justify-center.relative
   [:div.rounded-full.border-4.w-44.h-44.flex.items-center.justify-center.flex-col
    {:class (themes (or theme :neutral))}
    [:div.text-7xl.relative.-top-2
     text]
    [:div.opacity-40.absolute.bottom-8 label]]])
