(ns virtuoso.dev
  (:require [gadget.inspector :as inspector]
            [virtuoso.ui.main :as virtuoso]))

(inspector/inspect "Application state" virtuoso/store)

(def labels
  {::virtuoso/ui-layer "Page data"
   ::virtuoso/modal-layer "Modal data"})

(set! virtuoso/*on-render* #(inspector/inspect (labels %1) %2))

(defonce ^:export kicking-out-the-jams (virtuoso/boot))

(set! js/window.-onerror
      (fn [message source line-no col-no ex]
        (prn "!!!!" message source line-no col-no ex)))
