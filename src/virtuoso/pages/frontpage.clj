(ns virtuoso.pages.frontpage
  (:require [phosphor.icons :as icons]
            [virtuoso.elements.brain :as brain]
            [virtuoso.elements.layout :as layout]))

(defn box-title [icon text]
  [:h2.mb-2.flex.gap-4.items-center
   (icons/render icon {:size "2rem"})
   [:strong.font-bold text]])

(defn render-page [_ctx _page]
  (layout/layout
   (layout/flex-container
    [:main.grow.flex.flex-col.justify-center
     (brain/brain)
     [:p.my-6.opacity-80
      "Tools for musicians to practice more efficiently."]
     (layout/box {:href "/interleaved-clickup/"}
       (box-title :phosphor.regular/speedometer "Interleaved Clicking Up")
       [:p.opacity-80 "Solidify a piece of music and bring it up to speed using
       random practice while clicking up with the metronome."])
     (layout/box {:href "/metronome/"
                  :class ["mt-4"]}
       (box-title :phosphor.regular/metronome "Metronome")
       [:p.opacity-80 "A metronome that supports randomly dropping
                 beats, accenting beats, changing time signatures and tempos and
                 more."])]
    [:footer
     [:p.my-4.opacity-80
      "Made by " [:a.link {:href "https://cjohansen.no"} "Christian Johansen"]]])))
