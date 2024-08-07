(ns virtuoso.pages.frontpage
  (:require [virtuoso.elements.brain :as brain]
            [virtuoso.elements.layout :as layout]))

(defn render-page [_ctx _page]
  (layout/layout
   (layout/flex-container
    [:main.grow.flex.flex-col.justify-center
     (brain/brain)
     [:p.my-6.opacity-80
      "Tools for musicians to practice more efficiently."]
     (layout/box {:href "/interleaved-clickup/"}
       [:h2.mb-2 [:strong.font-bold "Interleaved Clicking Up"]]
       [:p.opacity-80 "Solidify and bring a piece of music up to speed by leveraging random
    practice while clicking up with the metronome."])
     (layout/box {:href "/metronome/"
                  :class ["mt-4"]}
       [:h2.mb-2 [:strong.font-bold "Metronome"]]
       [:p.opacity-80 "A metronome that supports randomly dropping
                 beats, accenting beats, changing time signatures and tempos and
                 more."])]
    [:footer
     [:p.my-4.opacity-80
      "Made by " [:a.link {:href "https://cjohansen.no"} "Christian Johansen"]]])))
