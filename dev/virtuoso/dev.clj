(ns virtuoso.dev
  (:require [powerpack.dev :as dev :refer [reset start]]
            [virtuoso.core :as virtuoso]
            [virtuoso.export :as export]))

(defmethod dev/configure! :default []
  (virtuoso/create-app :dev))

(comment
  (set! *print-namespace-maps* false)
  (start)
  (reset)
  (export/export)
)
