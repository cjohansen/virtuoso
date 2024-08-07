(ns virtuoso.elements.button
  (:require [phosphor.icons :as icons]))

(defn button [{:keys [text actions spinner? left-icon right-icon subtle?] :as btn}]
  [:button.btn.max-sm:btn-block
   (cond-> (dissoc btn :text :spinner? :left-icon :right-icon :actions :subtle?)
     actions (assoc-in [:on :click] actions)
     subtle? (assoc :class "btn-neutral")
     (not subtle?) (assoc :class "btn-primary"))
   (when spinner?
     [:span.loading.loading-spinner])
   (when left-icon
     (icons/render left-icon {:class ["h-6" "w-6"]}))
   text
   (when right-icon
     (icons/render right-icon {:class ["h-6" "w-6"]}))])
