(ns virtuoso.elements.form-scenes
  (:require [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.form :as form]))

(defscene form
  (form/boxed-form
   {:boxes
    [{:title "Exercise details"
      :fields
      [{:controls
        [{:label "Length"
          :inputs [{:input/kind :input.kind/number
                    :value 4}
                   {:input/kind :input.kind/select
                    :values [{:value "beat" :text "Beats"}
                             {:value "bar" :selected? true :text "Bars"}
                             {:value "line" :text "Lines"}
                             {:value "phrase" :text "Phrases"}]}]}
         {:label "Time signature"
          :inputs [{:input/kind :input.kind/number
                    :value 4}
                   {:input/kind :input.kind/select
                    :values [{:value "4" :selected? true :text "4"}
                             {:value "8" :text "8"}
                             {:value "16" :text "16"}
                             {:value "32" :text "32"}]}]}]}]}

     {:title "Session settings"
      :fields
      [{:controls
        [{:label "Start at"
          :inputs [{:input/kind :input.kind/select
                    :values [{:value "forward" :selected? true :text "the top"}
                             {:value "backward" :text "the end"}]}]}
         {:label "Max phrase length"
          :inputs [{:input/kind :input.kind/number
                    :value 0}]}]}]}

     {:title "Metronome settings"
      :fields
      [{:controls
        [{:label "Start tempo"
          :inputs [{:input/kind :input.kind/number
                    :value 60}]}
         {:label "BPM step"
          :inputs [{:input/kind :input.kind/number
                    :value 5}]}
         {:label "Drop beats (%)"
          :inputs [{:input/kind :input.kind/number
                    :value 0}]}]}
       {:controls
        [{:label "Tick beats"
          :inputs [{:input/kind :input.kind/pill-select
                    :values [{:text "1" :selected? true}
                             {:text "2" :selected? true}
                             {:text "3" :selected? true}
                             {:text "4" :selected? true}]}]}]}

       {:controls
        [{:label "Accentuate beats"
          :inputs [{:input/kind :input.kind/pill-select
                    :values [{:text "1" :selected? true}
                             {:text "2"}
                             {:text "3"}
                             {:text "4"}]}]}]}]}]}))
