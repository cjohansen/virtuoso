(ns virtuoso.ui.elements)

(defn layout [& content]
  [:html.bg-base-200 {:data-theme "dracula"}
   [:body
    content]])

(defn flex-container [& content]
  [:div.container.max-w-2xl.mx-auto.px-4.flex.flex-col.min-h-screen.justify-between
   content])

(def container-classes
  "container max-w-2xl mx-auto px-2 md:px-4")

(def box-classes "bg-base-100 rounded-box border-base-300 md:border")

(defn box [attrs & content]
  [(if (:href attrs)
     :a.block
     :div)
   (update attrs :class str " p-4 md:p-6 " box-classes)
   content])

(defn form-box [attrs & content]
  [(if (:href attrs)
     :a.block
     :div)
   (update attrs :class str " p-3 md:p-5 " box-classes)
   content])

(defn preformatted [text]
  [:pre.whitespace-pre.font-mono.my-8.text-xs.md:text-sm
   text])

(defn h1
  ([text] (h1 nil text))
  ([props text]
   [:h1.text-2xl.md:text-3xl
    props
    text]))

(defn h2
  ([text] (h2 nil text))
  ([_props text]
   [:h2.mb-4
    text]))

(defn form-h2
  ([text] (form-h2 nil text))
  ([_props text]
   [:h2.text-sm.mb-2.ml-1.font-bold
    text]))

(defn p
  ([text] (p nil text))
  ([props & texts]
   [:p.opacity-80.my-4.text-sm props texts]))

(defn form-fields [& fields]
  [:div.flex.gap-4 fields])

(defn number-input [{:keys [value]}]
  [:input.input.input-bordered.w-12.input-sm
   {:type "text" :value (str value)}])

(defn select [{:keys [values]}]
  [:select.select.select-bordered.w-26.select-sm
   (for [{:keys [value selected? text]} values]
     [:option (cond-> {:value value}
                selected? (assoc :selected "selected"))
      text])])

(defn pill-select [{:keys [values]}]
  [:div.flex.gap-2
   (for [{:keys [text selected?]} values]
     [:button.badge.badge-neutral
      {:class (when-not selected?
                "badge-outline")}
      text])])

(defn form-control [{:keys [label]} & fields]
  [:label.form-control.w-40
   [:div.label
    [:span.label-text label]]
   [:div.flex.gap-2
    fields]])
