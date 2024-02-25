(ns virtuoso.export
  (:require [powerpack.export :as export]
            [virtuoso.core :as virtuoso]))

(defn ^:export export [& _args]
  (set! *print-namespace-maps* false)
  (export/export! (virtuoso/create-app :prod)))
