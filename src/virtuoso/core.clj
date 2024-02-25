(ns virtuoso.core
  (:require [virtuoso.pages.frontpage :as frontpage]
            [virtuoso.pages.interleaved-clickup :as icu-page]))

(defn render-page [context page]
  (if-let [f (case (:page/kind page)
               :page.kind/frontpage frontpage/render-page
               :page.kind/interleaved-clickup icu-page/render-page
               nil)]
    (f context page)
    [:h1 "Page not found 🤷‍♂️"]))

(defn create-app [env]
  (cond-> {:site/title "Virtuoso"
           :powerpack/render-page #'render-page
           :powerpack/port 4848
           :powerpack/log-level :debug
           :powerpack/content-file-suffixes ["md" "edn"]

           :powerpack/dev-assets-root-path "public"

           :optimus/bundles {"/styles.css"
                             {:public-dir "public"
                              :paths ["/tailwind.css"]}

                             "/app.js"
                             {:public-dir "public"
                              :paths ["/js/compiled/app.js"]}}}

    (= :build env)
    (assoc :site/base-url "https://virtuoso.tools")

    (= :dev env) ;; serve figwheel compiled js
    (assoc :powerpack/dev-assets-root-path "public")))
