(ns virtuoso.elements.layout
  (:require [virtuoso.elements.brain :as brain]
            [virtuoso.elements.typography :as t]))

(defn layout [& content]
  [:html.bg-base-200 {:data-theme "dracula"}
   [:head
    [:link {:rel "mask-icon" :href "/safari-pinned-tab.svg" :color "#ff79c6"}]
    [:meta {:name "theme-color" :content "#232530"}]]
   [:body
    content]])

(defn flex-container [& content]
  [:div.container.max-w-2xl.mx-auto.px-4.flex.flex-col.min-h-screen.justify-between
   content])

(def container-classes
  "container max-w-2xl mx-auto px-2 md:px-4")

(def box-classes "bg-base-100 rounded-box border-base-300 md:border")

(defn box [attrs & content]
  [(if (:href attrs)
     :a.block
     :div)
   (update attrs :class str " p-4 md:p-6 " box-classes)
   content])

(defn header [{:keys [title]}]
  [:header.m-4.flex.justify-between.items-center.gap-4
   [:a.block.right.w-12
    {:href "/"}
    (brain/brain {:text? false})]
   (t/h1 {:class "max-w-2xl grow"} title)
   [:span " "]])
