(ns virtuoso.elements.layout)

(defn layout [& content]
  [:html.bg-base-200 {:data-theme "dracula"}
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
