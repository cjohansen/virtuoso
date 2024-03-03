(ns virtuoso.elements.colored-boxes)

(def colors
  ["bg-error" "bg-warning" "bg-success" "bg-info"
   "bg-accent" "bg-secondary" "bg-primary"])

(defn colored-boxes [{:keys [boxes footer]}]
  [:div.flex.flex-col.justify-center
   [:div.flex.gap-2.justify-center.mb-8.flex-wrap
    (let [n (count colors)]
      (for [{:keys [text color-idx]} boxes]
        [:div.text-primary-content.rounded.py-2.px-4.font-bold
         {:class (get colors (mod color-idx n))}
         text]))]
   [:p.text-center
    (:text footer)]])
