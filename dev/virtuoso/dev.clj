(ns virtuoso.dev
  (:require [powerpack.dev :as dev]
            [virtuoso.core :as virtuoso]
            [virtuoso.export :as export]))

(defmethod dev/configure! :default []
  (virtuoso/create-app :dev))

(comment
  (set! *print-namespace-maps* false)
  (export/export)
)

(comment ;; s-:
  (dev/start)
  (dev/reset)
  )
