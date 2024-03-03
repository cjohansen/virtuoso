(ns virtuoso.elements.button
  (:require [phosphor.icons :as icons]))

(defn button [{:keys [text spinner? left-icon right-icon] :as btn}]
  [:button.btn.btn-primary.max-sm:btn-block
   (dissoc btn :text :spinner? :left-icon :right-icon)
   (when spinner?
     [:span.loading.loading-spinner])
   (when left-icon
     (icons/render left-icon {:class "h-6 w-6"}))
   text
   (when right-icon
     (icons/render right-icon {:class "h-6 w-6"}))])
