(ns virtuoso.elements.page
  (:require [virtuoso.elements.badge :as badge]
            [virtuoso.elements.bar :as bar]
            [virtuoso.elements.button :as button]
            [virtuoso.elements.button-panel :as panel]
            [virtuoso.elements.colored-boxes :as colored-boxes]
            [virtuoso.elements.form :as form]
            [virtuoso.elements.typography :as t]))

(defn footer [{:keys [heading text button]}]
  [:div.justify-center.px-4.md:px-0
   (some-> heading t/h2)
   (some-> text t/p)
   (some-> button button/button)])

(defn page [{:keys [sections spacing]}]
  [:div.flex.flex-col
   {:class (if (= :wide spacing)
             ["gap-16" "pt-8"]
             ["gap-8"])}
   (for [section sections]
     (case (:kind section)
       :element.kind/bars (bar/bars section)
       :element.kind/boxed-form (form/boxed-form section)
       :element.kind/button-panel (panel/button-panel section)
       :element.kind/colored-boxes (colored-boxes/colored-boxes section)
       :element.kind/footer (footer section)
       :element.kind/round-badge (badge/round-badge section)
       (prn "Unknown section kind" section)))])

(defn modal [props]
  [:modal
   [:h1 "Whatchagonna do"]])
