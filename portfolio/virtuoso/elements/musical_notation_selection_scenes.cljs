(ns virtuoso.elements.musical-notation-selection-scenes
  (:require [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.musical-notation-selection :as selection]))

(defscene quarter-note-beat-selection
  (selection/musical-notation-selection
   {:items [{:notation [:note/quarter]
             :active? true}
            {:notation [[:notation/beam :note/eighth :note/eighth]]
             :actions []}
            {:notation [[:notation/beam [:notation/dot :note/eighth] :note/sixteenth]]
             :actions []}
            {:notation [[:notation/beam :note/eighth :note/eighth :note/eighth]]
             :actions []}
            {:notation [[:notation/beam :note/sixteenth :note/sixteenth :note/sixteenth :note/sixteenth]]
             :actions []}
            {:notation [[:notation/beam :note/sixteenth :note/sixteenth :note/sixteenth
                         :note/sixteenth :note/sixteenth :note/sixteenth]]
             :actions []}]}))
