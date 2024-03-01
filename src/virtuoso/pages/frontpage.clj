(ns virtuoso.pages.frontpage
  (:require [virtuoso.ui.brain :as brain]
            [virtuoso.ui.elements :as e]))

(defn render-page [_ctx _page]
  (e/layout
   (e/flex-container
    [:main.grow.flex.flex-col.justify-center
     (brain/brain)
     [:p.my-6.opacity-60
      "Tools for musicians to practice more efficiently."]
     (e/box {:href "/interleaved-clickup/"}
      [:h2.mb-2 [:strong.font-bold "Interleaved Clicking Up"]]
      [:p.opacity-80 "Solidify and bring a piece of music up to speed by leveraging random
    practice while clicking up with the metronome."])]
    [:footer
     [:p.my-4.opacity-60
      "Made by " [:a.link {:href "https://cjohansen.no"} "Christian Johansen"]]])))
