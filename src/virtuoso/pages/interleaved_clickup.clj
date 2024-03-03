(ns virtuoso.pages.interleaved-clickup
  (:require [virtuoso.elements.layout :as layout]
            [virtuoso.elements.typography :as t]))

(defn render-page [_ctx _page]
  (layout/layout
   [:div.flex.flex-col.min-h-screen.justify-between
    (layout/header {:title "Interleaved Clicking Up"})
    [:main.grow.flex.flex-col.gap-4.replicant-root.justify-center
     {:class layout/container-classes
      :data-view "interleaved-clickup"}]
    [:footer.my-4 {:class layout/container-classes}
     [:div.px-4.md:px-0.mt-4
      (t/h2 "What's this?")
      (t/p
       "Interleaved clicking up helps you solidify and bring a piece of music up
       to speed by breaking it into chunks and clicking them up in a rotating
       pattern. You start with just a single chunk and click it up. You then add
       a unit, and will then be asked to play sections of varying lengths every
       time you increase the speed.")
      (t/p {}
       "This exercise was designed by "
       [:a.link {:href "https://www.mollygebrian.com/"} "Molly Gebrian"] ". She
      has a very good explanation and a demonstration of the process "
       [:a.link {:href "https://www.youtube.com/watch?v=it89AswI2dw"} "on YouTube"]
       ".")]]]))
