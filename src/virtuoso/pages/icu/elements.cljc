(ns virtuoso.pages.icu.elements
  (:require [virtuoso.elements.form :as form]
            [virtuoso.elements.button-panel :as panel]))

(def colors
  ["bg-error" "bg-warning" "bg-success" "bg-info"
   "bg-accent" "bg-secondary" "bg-primary"])

(defn render-clickup [{:keys [boxes text]}]
  [:div.flex.flex-col.justify-center
   (let [n (count colors)]
     (for [{:keys [text color-idx]} boxes]
       [:div
        {:class (get colors (mod color-idx n))}
        text]))
   [:p text]])

(defn render [sections]
  [:div.flex.flex-col.gap-8
   (for [section sections]
     (case (:kind section)
       :element.kind/boxed-form (form/boxed-form section)
       :element.kind/button-panel (panel/button-panel section)
       :element.kind/clickup (render-clickup section)
       (prn "Unknown section kind" section)))])
