(ns virtuoso.pages.not-found
  (:require [virtuoso.elements.brain :as brain]
            [virtuoso.elements.layout :as layout]))

(defn render-page [_ctx _page]
  (layout/layout
   (layout/flex-container
    [:main.grow.flex.flex-col.justify-center
     (brain/brain)
     [:p.my-6.opacity-80
      "Sorry, that page doesn't exist."]]
    [:footer
     [:p.my-4.opacity-80
      "Made by " [:a.link {:href "https://cjohansen.no"} "Christian Johansen"]]])))
