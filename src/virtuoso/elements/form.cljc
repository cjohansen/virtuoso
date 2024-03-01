(ns virtuoso.elements.form
  (:require [virtuoso.elements.layout :as layout]))

(defn box [attrs & content]
  [(if (:href attrs)
     :a.block
     :div)
   (update attrs :class str " p-3 md:p-5 " layout/box-classes)
   content])

(defn H2
  ([text] (H2 nil text))
  ([_props text]
   [:h2.text-sm.mb-2.ml-1.font-bold
    text]))

(defn Fields [& fields]
  [:div.flex.gap-4 fields])

(defn Control [{:keys [label]} & fields]
  [:label.form-control.w-40
   [:div.label
    [:span.label-text label]]
   [:div.flex.gap-2
    fields]])

(defn NumberInput [{:keys [value]}]
  [:input.input.input-bordered.w-12.input-sm
   {:type "text" :value (str value)}])

(defn Select [{:keys [values]}]
  [:select.select.select-bordered.w-26.select-sm
   (for [{:keys [value selected? text]} values]
     [:option (cond-> {:value value}
                selected? (assoc :selected "selected"))
      text])])

(defn PillSelect [{:keys [values]}]
  [:div.flex.gap-2
   (for [{:keys [text selected?]} values]
     [:button.badge.badge-neutral
      {:class (when-not selected?
                "badge-outline")}
      text])])

(defn render-input [input]
  (case (:input/kind input)
    :input.kind/number (NumberInput input)
    :input.kind/select (Select input)
    :input.kind/pill-select (PillSelect input)))

(defn BoxForm [form]
  (box
   {}
   (some-> form :title H2)
   (let [rows (for [{:keys [controls]} (:fields form)]
                (let [fs (for [{:keys [label inputs]} controls]
                           (Control {:label label}
                                    (map render-input inputs)))]
                  (if (= 1 (count fs))
                    fs
                    (Fields fs))))]
     (if (= 1 (count rows))
       rows
       [:div.flex.flex-col.gap-4 rows]))))

(defn BoxedForm [{:keys [boxes]}]
  [:div.flex.flex-col.gap-4
   (map BoxForm boxes)])
