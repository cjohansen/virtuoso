(ns virtuoso.dev
  (:require [gadget.inspector :as inspector]
            [virtuoso.ui.main :as virtuoso]))

(inspector/inspect "Application state" virtuoso/store)
(set! virtuoso/*on-render* #(inspector/inspect "Page data" %))

(defonce ^:export kicking-out-the-jams (virtuoso/boot))
