(ns virtuoso.elements.musical-notation-scenes
  (:require [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.musical-notation :as mn]))

(defscene long-notes
  (mn/render [:note/whole
              :note/dotted-whole
              :note/half
              :note/dotted-half
              :note/quarter
              :note/dotted-quarter]))

(defscene eighth-note
  [:div
   [:div.flex.gap-4
    (mn/render [:note/eighth])
    (mn/render [[:notation/dot :note/eighth]])
    (mn/render [[:notation/beam :note/eighth :note/eighth]])]
   [:div.flex.gap-4
    (mn/render [[:notation/beam [:notation/dot :note/eighth] :note/eighth]])
    (mn/render [[:notation/beam :note/eighth [:notation/dot :note/eighth]]])
    (mn/render [[:notation/beam [:notation/dot :note/eighth] [:notation/dot :note/eighth]]])]
   [:div.flex.gap-4
    (mn/render [[:notation/beam :note/eighth :note/eighth :note/eighth]])
    (mn/render [[:notation/beam [:notation/dot :note/eighth] :note/eighth :note/eighth]])
    (mn/render [[:notation/beam :note/eighth [:notation/dot :note/eighth] :note/eighth]])
    (mn/render [[:notation/beam :note/eighth :note/eighth [:notation/dot :note/eighth]]])
    (mn/render [[:notation/beam [:notation/dot :note/eighth] [:notation/dot :note/eighth] [:notation/dot :note/eighth]]])]])

(defscene eighth-note
  [:div
   [:div.flex.gap-4
    (mn/render [:note/eighth])
    (mn/render [[:notation/dot :note/eighth]])
    (mn/render [[:notation/beam :note/eighth :note/eighth]])]
   [:div.flex.gap-4
    (mn/render [[:notation/beam [:notation/dot :note/eighth] :note/eighth]])
    (mn/render [[:notation/beam :note/eighth [:notation/dot :note/eighth]]])
    (mn/render [[:notation/beam [:notation/dot :note/eighth] [:notation/dot :note/eighth]]])]
   [:div.flex.gap-4
    (mn/render [[:notation/beam :note/eighth :note/eighth :note/eighth]])
    (mn/render [[:notation/beam [:notation/dot :note/eighth] :note/eighth :note/eighth]])
    (mn/render [[:notation/beam :note/eighth [:notation/dot :note/eighth] :note/eighth]])
    (mn/render [[:notation/beam :note/eighth :note/eighth [:notation/dot :note/eighth]]])
    (mn/render [[:notation/beam [:notation/dot :note/eighth] [:notation/dot :note/eighth] [:notation/dot :note/eighth]]])]])

(defscene sixteenth-note
  [:div
   [:div.flex.gap-4
    (mn/render [:note/sixteenth])
    (mn/render [[:notation/dot :note/sixteenth]])
    (mn/render [[:notation/beam :note/sixteenth :note/sixteenth]])
    (mn/render [[:notation/beam :note/sixteenth :note/sixteenth :note/sixteenth :note/sixteenth]])]
   [:div.flex.gap-4
    (mn/render [[:notation/beam :note/eighth :note/sixteenth :note/sixteenth]])
    (mn/render [[:notation/beam :note/eighth [:notation/dot :note/sixteenth]]])
    (mn/render [[:notation/beam [:notation/dot :note/eighth] :note/sixteenth]])]])

(defscene note-positioning
  [:div.relative
   [:div.absolute.bottom-1.left-0.right-0.border-b-2]
   [:div.flex.gap-4
    (mn/render [:note/eighth])
    (mn/render [[:notation/dot :note/eighth]])
    (mn/render [[:notation/beam :note/eighth :note/eighth]])
    (mn/render [[:notation/beam :note/sixteenth :note/sixteenth :note/sixteenth :note/sixteenth]])]])
