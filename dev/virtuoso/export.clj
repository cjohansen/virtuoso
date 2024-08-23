(ns virtuoso.export
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [optimus.assets :as assets]
            [optimus.export :as optimus-export]
            [optimus.optimizations :as optimizations]
            [powerpack.export :as export]
            [virtuoso.core :as virtuoso]))

(def optimize #(optimizations/all % {}))

(defn export-portfolio []
  (let [js-path "/js/compiled/app-portfolio.js"
        assets (->> [["portfolio.js" [js-path]]
                     ["portfolio/styles/portfolio.css"]
                     ["portfolio/prism.js"]
                     ["tailwind.css"]]
                    (mapcat (fn [[bundle paths]]
                              (assets/load-bundle "public" bundle (or paths [(str "/" bundle)]))))
                    optimize)
        asset-map (into {} (map (juxt :original-path identity) assets))
        js-bundle (asset-map "/bundles/portfolio.js")
        portfolio-css (asset-map "/bundles/portfolio/styles/portfolio.css")
        prism-js (asset-map "/bundles/portfolio/prism.js")
        app-css (asset-map "/bundles/tailwind.css")]
    (-> [(assoc portfolio-css :path "/portfolio/styles/portfolio.css")
         (assoc prism-js :path "/portfolio/prism.js")
         (assoc app-css :path "/tailwind.css")
         js-bundle]
        (concat (->> assets
                     (remove :bundled)
                     (remove :outdated)
                     (remove (comp #{(:path js-bundle)
                                     (:path portfolio-css)
                                     (:path prism-js)
                                     (:path app-css)} :path))))
        (optimus-export/save-assets "target/site"))
    (spit "target/site/portfolio/index.html"
          (str/replace (slurp (io/resource "public/index.html"))
                       (re-pattern js-path) (:path js-bundle)))
    (spit "target/site/portfolio/canvas.html" (slurp (io/resource "public/canvas.html")))))

(defn ^:export export [& _args]
  (set! *print-namespace-maps* false)
  (export/export! (virtuoso/create-app :prod))
  (export-portfolio))
