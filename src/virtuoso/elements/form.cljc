(ns virtuoso.elements.form
  (:require [virtuoso.elements.layout :as layout]
            [virtuoso.elements.button :as button]))

(defn prepare-select [state path options & [default]]
  (let [current (or (get-in state path) default (ffirst options))]
    {:input/kind :input.kind/select
     :on {:input [[:action/assoc-in path (cond
                                           (keyword? current)
                                           :event/target-value-kw

                                           (number? current)
                                           :event/target-value-num

                                           :else
                                           :event/target-value)]]}
     :options (for [[v t] options]
                (cond-> {:value (cond
                                  (keyword? v)
                                  (str (when-let [ns (namespace v)]
                                         (str ns "/"))
                                       (name v))

                                  :else (str v))
                         :text t}
                  (= v current) (assoc :selected? true)))}))

(defn prepare-multi-select [state path options & [default]]
  (let [current (set (or (get-in state path) default))]
    {:input/kind :input.kind/pill-select
     :options
     (for [v options]
       (cond-> {:text (str v)
                :on {:click [[:action/assoc-in path
                              (if (current v)
                                (disj current v)
                                (conj current v))]]}}
         (current v) (assoc :selected? true)))}))

(defn prepare-number-input [state path & [default]]
  {:input/kind :input.kind/number
   :on {:input [[:action/assoc-in path :event/target-value-num]]}
   :value (or (get-in state path) default)})

;; Rendering

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

(defn control [{:keys [label size]} & fields]
  [:label.form-control
   {:class (if (= :md size) "w-24" "w-40")}
   [:div.label
    [:span.label-text label]]
   [:div.flex.gap-2
    fields]])

(defn number-input [option]
  [:input.input.input-bordered.w-12.input-sm
   (merge option {:type "text"})])

(defn select [params]
  [:select.select.select-bordered.w-26.select-sm
   (dissoc params :options)
   (for [option (:options params)]
     [:option (cond-> (dissoc option :selected? :text)
                (:selected? option) (assoc :selected "selected"))
      (:text option)])])

(defn pill-select [{:keys [options]}]
  [:div.flex.gap-2
   (for [option options]
     [:button.badge.badge-neutral
      (merge
       (dissoc option :text :selected?)
       {:class (when-not (:selected? option)
                 "badge-outline")})
      (:text option)])])

(defn render-input [input]
  (case (:input/kind input)
    :input.kind/number (number-input input)
    :input.kind/select (select input)
    :input.kind/pill-select (pill-select input)))

(defn form-box [form]
  (box
   {}
   (some-> form :title h2)
   (let [rows (for [{:keys [controls size]} (:fields form)]
                (let [fields (for [{:keys [label inputs]} controls]
                               (control {:label label
                                         :size size}
                                        (map render-input inputs)))]
                  (if (= 1 (count fields))
                    fields
                    (fieldset fields))))]
     (if (= 1 (count rows))
       rows
       [:div.flex.flex-col.gap-4 rows]))))

(defn boxed-form [{:keys [boxes button]}]
  [:div.flex.flex-col.gap-4
   (map form-box boxes)
   (when button
     [:div.my-4
      (button/button button)])])
