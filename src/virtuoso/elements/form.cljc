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

(defn fieldset [& fields]
  [:fieldset.flex.gap-4 fields])

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

(defn render-input [input]
  (case (:input/kind input)
    :input.kind/number (number-input input)
    :input.kind/select (select input)
    :input.kind/pill-select (pill-select input)))

(defn box-form [form]
  (box
   {}
   (some-> form :title h2)
   (let [rows (for [{:keys [controls]} (:fields form)]
                (let [fields (for [{:keys [label inputs]} controls]
                               (control {:label label}
                                        (map render-input inputs)))]
                  (if (= 1 (count fields))
                    fields
                    (fieldset fields))))]
     (if (= 1 (count rows))
       rows
       [:div.flex.flex-col.gap-4 rows]))))

(defn boxed-form [{:keys [boxes]}]
  [:div.flex.flex-col.gap-4
   (map box-form boxes)])
