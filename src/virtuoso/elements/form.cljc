(ns virtuoso.elements.form
  (:require [virtuoso.elements.layout :as layout]))

(defn box [attrs & content]
  [(if (:href attrs)
     :a.block
     :div)
   (update attrs :class str " p-3 md:p-5 " layout/box-classes)
   content])

(defn h2
  ([text] (h2 nil text))
  ([_props text]
   [:h2.text-sm.mb-2.ml-1.font-bold
    text]))

(defn fields [& fields]
  [:div.flex.gap-4 fields])

(defn control [{:keys [label]} & fields]
  [:label.form-control.w-40
   [:div.label
    [:span.label-text label]]
   [:div.flex.gap-2
    fields]])

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
