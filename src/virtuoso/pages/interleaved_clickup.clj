(ns virtuoso.pages.interleaved-clickup
  (:require [virtuoso.elements.brain :as brain]
            [virtuoso.ui.elements :as e]))

(defn render-page [_ctx _page]
  (e/layout
   [:div.flex.flex-col.min-h-screen.justify-between
    [:header.m-4.flex.justify-between.items-center.gap-4
     [:a.block.right.w-12
      {:href "/"}
      (brain/brain {:text? false})]
     (e/h1 {:class "max-w-2xl grow"} "Interleaved Clicking Up")
     [:span " "]]
    [:main.grow.flex.flex-col.gap-4
     {:class e/container-classes}
     (e/form-box
      {}
      (e/form-h2 "Exercise details")
      (e/form-fields
       (e/form-control
        {:label "Length"}
        (e/number-input {:value 4})
        (e/select
         {:values [{:value "beat" :text "Beats"}
                   {:value "bar" :selected? true :text "Bars"}
                   {:value "line" :text "Lines"}
                   {:value "phrase" :text "Phrases"}]}))

       (e/form-control
        {:label "Time signature"}
        (e/number-input {:value 4})
        (e/select
         {:values [{:value "4" :selected? true :text "4"}
                   {:value "8" :text "8"}
                   {:value "16" :text "16"}
                   {:value "32" :text "32"}]}))))

     (e/form-box
      {}
      (e/form-h2 "Session settings")

      (e/form-fields
       (e/form-control
        {:label "Start at"}
        (e/select
         {:values [{:value "forward" :selected? true :text "the top"}
                   {:value "backward" :text "the end"}]}))

       (e/form-control
        {:label "Max phrase length"}
        (e/number-input {:value 0}))))

     (e/form-box
      {}
      (e/form-h2 "Metronome settings")

      [:div.flex.flex-col.gap-4
       (e/form-fields
        (e/form-control
         {:label "Start tempo"}
         (e/number-input {:value 60}))

        (e/form-control
         {:label "BPM Step"}
         (e/number-input {:value 5}))

        (e/form-control
         {:label "Drop beats (%)"}
         (e/number-input {:value 0})))

       (e/form-control
        {:label "Tick beats"}
        (e/pill-select
         {:values [{:text "1" :selected? true}
                   {:text "2" :selected? true}
                   {:text "3" :selected? true}
                   {:text "4" :selected? true}]}))

       (e/form-control
        {:label "Accentuate beats"}
        (e/pill-select
         {:values [{:text "1" :selected? true}
                   {:text "2"}
                   {:text "3"}
                   {:text "4"}]}))])]
    [:footer.my-4 {:class e/container-classes}
     [:div.px-4.md:px-0.mt-4
      (e/h2 "What's this?")
      (e/p
       "Interleaved clicking up helps you solidify and bring a piece of music up
       to speed by breaking it into chunks and clicking them up in a rotating
       pattern. You start with just a single chunk and click it up. You then add
       a unit, and will then be asked to play sections of varying lengths every
       time you increase the speed.")
      (e/p {}
       "This exercise was designed by "
       [:a.link {:href "https://www.mollygebrian.com/"} "Molly Gebrian"] ". She
      has a very good explanation and a demonstration of the process "
       [:a.link {:href "https://www.youtube.com/watch?v=it89AswI2dw"} "on YouTube"]
       ".")]]]))
