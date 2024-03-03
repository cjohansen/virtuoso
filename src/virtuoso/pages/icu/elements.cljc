(ns virtuoso.pages.icu.elements
  (:require [virtuoso.elements.form :as form]))

(defn render [sections]
  [:div
   (for [section sections]
     (case (:kind section)
       :element.kind/boxed-form (form/boxed-form section)
       (prn "Unknown section kind" section)))])
