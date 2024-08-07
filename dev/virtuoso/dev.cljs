(ns virtuoso.dev
  (:require [clojure.string :as str]
            [gadget.inspector :as inspector]
            [virtuoso.ui.main :as virtuoso]))

(inspector/inspect "Application state" virtuoso/store)

(def labels
  {::virtuoso/ui-layer "Page data"
   ::virtuoso/modal-layer "Modal data"})

(set! virtuoso/*on-render* #(inspector/inspect (labels %1) %2))

(defn print-error [e]
  (js/console.error (.-message e))
  (prn (ex-data e))
  (println (.-stack e))
  (when-let [cause (.-cause e)]
    (println "Caused by:")
    (print-error cause)))

(defonce ^:export kicking-out-the-jams
  (do
    (set! *print-namespace-maps* false)
    (js/window.addEventListener
     "error"
     (fn [e]
       (.preventDefault e)
       (let [file (or (some-> (.-filename e)
                              (str/split #"js/compiled")
                              second)
                      (.-filename e))]
         (js/console.error "Uncaught exception in " (str file ":" (.-lineno e) ":" (.-colno e))))
       (print-error (.-error e))))
    (virtuoso/boot)))

(comment
  (set! *print-namespace-maps* false)
)
