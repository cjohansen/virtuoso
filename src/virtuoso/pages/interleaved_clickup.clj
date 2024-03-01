(ns virtuoso.pages.interleaved-clickup
  (:require [virtuoso.elements.brain :as brain]
            [virtuoso.elements.form :as form]
            [virtuoso.elements.layout :as layout]
            [virtuoso.elements.typography :as t]))

(defn render-page [_ctx _page]
  (layout/layout
   [:div.flex.flex-col.min-h-screen.justify-between
    [:header.m-4.flex.justify-between.items-center.gap-4
     [:a.block.right.w-12
      {:href "/"}
      (brain/brain {:text? false})]
     (t/h1 {:class "max-w-2xl grow"} "Interleaved Clicking Up")
     [:span " "]]
    [:main.grow.flex.flex-col.gap-4
     {:class layout/container-classes}
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
                   {:value "32" :text "32"}]}))))

     (form/box
      {}
      (form/h2 "Session settings")

      (form/fields
       (form/control
        {:label "Start at"}
        (form/select
         {:values [{:value "forward" :selected? true :text "the top"}
                   {:value "backward" :text "the end"}]}))

       (form/control
        {:label "Max phrase length"}
        (form/number-input {:value 0}))))

     (form/box
      {}
      (form/h2 "Metronome settings")

      [:div.flex.flex-col.gap-4
       (form/fields
        (form/control
         {:label "Start tempo"}
         (form/number-input {:value 60}))

        (form/control
         {:label "BPM Step"}
         (form/number-input {:value 5}))

        (form/control
         {:label "Drop beats (%)"}
         (form/number-input {:value 0})))

       (form/control
        {:label "Tick beats"}
        (form/pill-select
         {:values [{:text "1" :selected? true}
                   {:text "2" :selected? true}
                   {:text "3" :selected? true}
                   {:text "4" :selected? true}]}))

       (form/control
        {:label "Accentuate beats"}
        (form/pill-select
         {:values [{:text "1" :selected? true}
                   {:text "2"}
                   {:text "3"}
                   {:text "4"}]}))])]
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
