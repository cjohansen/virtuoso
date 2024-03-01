(ns virtuoso.elements.form-scenes
  (:require [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.form :as form]))

(defscene form-box
  (form/box
   {}
   (form/h2 "Exercise details")
   (form/fields
    (form/control
     {:label "Length"}
     (form/number-input {:value 4})
     (form/select
      {:values [{:value "beat" :text "Beats"}
                {:value "bar" :selected? true :text "Bars"}
                {:value "line" :text "Lines"}
                {:value "phrase" :text "Phrases"}]}))

    (form/control
     {:label "Time signature"}
     (form/number-input {:value 4})
     (form/select
      {:values [{:value "4" :selected? true :text "4"}
                {:value "8" :text "8"}
                {:value "16" :text "16"}
                {:value "32" :text "32"}]})))))
