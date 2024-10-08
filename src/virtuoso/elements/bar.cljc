(ns virtuoso.elements.bar
  (:require [phosphor.icons :as icons]
            [virtuoso.elements.form :as form]
            [virtuoso.elements.icon-button :as icon-button]
            [virtuoso.elements.musical-notation :as mn]))

(def sizes
  {:medium 2.5
   :large 8})

(def bar-line-thin
  {:medium "w-px"
   :large "w-0.5"})

(def bar-line-thick
  {:medium "w-0.5"
   :large "w-1"})

(def rep-dot
  {:medium ["w-1" "h-1"]
   :large ["w-2" "h-2"]})

(def rep-text-size
  {:medium "text-sm"
   :large "text-lg"})

(def tempo-label-size
  {:medium "text-xs"
   :large "text-sm"})

(def text-class
  {:medium "text-lg"
   :large "text-6xl"})

(def icon-size
  {:medium "1rem"
   :large "2rem"})

(def dot-padding
  {:medium "pt-2"
   :large "pt-4"})

(def dot-size
  {:medium ["w-3" "h-3"]
   :large ["w-5" "h-5"]})

(def label-width
  {:medium "min-w-4"
   :large "min-w-20"})

(def note-size
  {:medium "text-4xl"
   :large "text-8xl"})

(def note-padding
  {:medium "px-2"
   :large "px-8"})

(defn icon-button [{:keys [icon actions]} {:keys [size]}]
  (when icon
    [:button {:on {:click actions}
              :class (when-not actions
                       "text-neutral")
              :disabled (not actions)}
     (icons/render icon {:size (icon-size size)})]))

(defn render-time-signature-buttons [xs k {:keys [height size]}]
  (when (some k xs)
    [:div.text-center.relative.leading-none.flex.flex-col.justify-around.pr-2
     {:style {:height height}}
     (for [x xs]
       (let [button (k x)]
         [:div {:style {:min-height (icon-size size)}}
          (icon-button button {:size size})]))]))

(defn bar [{:keys [beats rhythm subdivision tempo reps dots size buttons actions] :as bar}]
  (let [size (if (sizes size) size :medium)
        rem-size (sizes size)
        height (str rem-size "rem")]
    [:div (when-let [k (:replicant/key bar)]
            {:replicant/key k})
     [:div.relative.pl-4.pr-4.min-w-12.border-l-2.border-r-2.border-neutral.flex
      {:style {:height height}}
      (for [i (range 5)]
        [:div.bg-neutral.h-px.absolute.left-0.right-0 {:style {:top (str (* i (/ rem-size 4)) "rem")}}])
      ;; Time signature
      (render-time-signature-buttons [beats subdivision] :left-button {:height height :size size})
      [:div.text-center.relative.leading-none.flex.flex-col.justify-around.pr-2
       (cond-> {:style {:height height}
                :class [(label-width size) (text-class size)]}
         actions (assoc :on {:click actions})
         actions (update :class conj "cursor-pointer"))
       [:div (:val beats)]
       [:div (:val subdivision)]]
      (render-time-signature-buttons [beats subdivision] :right-button {:height height :size size})
      ;; Rhythm
      (when (:pattern rhythm)
        (let [actions (or actions (:actions rhythm))]
          (mn/render (cond-> {:class [(note-size size) (note-padding size) "relative"]}
                       actions (assoc :on {:click actions})
                       actions (update :class conj "cursor-pointer"))
            (:pattern rhythm))))
      ;; Tempo
      (when tempo
        (let [subtle? (= :subtle (:style tempo))]
          [:div.pl-2.relative.flex.flex-col.text-center.justify-center.text-neutral-content
           (cond-> {}
             (and actions (not (:actions tempo)))
             (merge {:on {:click actions}
                     :class "cursor-pointer"}))
           (if (:actions tempo)
             (form/number-input
              {:on {:input (:actions tempo)}
               :value (:val tempo)
               :class (cond-> ["mb-1" (rep-text-size size) "w-14"]
                        subtle? (conj "text-neutral"))})
             [:div {:class (rep-text-size size)} (:val tempo)])
           [:div {:class (cond-> [(tempo-label-size size)]
                           subtle? (conj "text-neutral"))}
            (:unit tempo)]]))
      ;; Reps
      (when reps
        [:div.relative.flex.ml-4
         [:div.flex.flex-col.justify-center.mr-1
          [:div.rounded-full.bg-neutral-content.mb-1
           {:class (rep-dot size)}]
          [:div.rounded-full.bg-neutral-content
           {:class (rep-dot size)}]]
         [:div.bg-neutral-content.mr-1 {:class (bar-line-thin size)}]
         [:div.bg-neutral-content {:class (bar-line-thick size)}]])
      (when reps
        [:div.pl-2.relative.flex.flex-col.justify-around.items-start
         (icon-button (:button-above reps) {:size size})
         [:span {:class [(label-width size) (rep-text-size size)]}
          (:val reps) " " (:unit reps)]
         (icon-button (:button-below reps) {:size size})])
      ;; Buttons
      (when (seq buttons)
        [:div.relative.flex.ml-4.gap-4.items-center
         (for [button buttons]
           (icon-button/bare-icon-button button))])]
     ;; Dots
     (when dots
       [:div.flex.justify-between.gap-1
        {:class (dot-padding size)}
        (for [dot dots]
          [:div.rounded-full.transition.duration-300.border
           {:on {:click (:actions dot)}
            :class (concat
                    (dot-size size)
                    (when (:actions dot)
                      ["cursor-pointer"])
                    (cond
                      (:current? dot)
                      ["bg-success" "border-success"]

                      (:disabled? dot)
                      ["border-neutral"]

                      (:highlight? dot)
                      ["bg-neutral" "border-info"]

                      :else
                      ["bg-neutral" "border-neutral"]))}])])]))

(defn bars [{:keys [bars buttons]}]
  [:div.flex.gap-4.justify-center
   (map bar bars)
   (let [size (:size (first bars))
         size (if (sizes size) size :medium)]
     (for [button buttons]
       [:div.flex.items-center {:style {:height (str (sizes size) "rem")}}
        (icon-button/bare-icon-button
         (-> button
             (assoc :size :small)
             (update :theme #(or % :success))))]))])
