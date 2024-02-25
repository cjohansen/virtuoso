(ns virtuoso.ui.core
  (:require [virtuoso.ui.main :as virtuoso]))

(defonce ^:export kicking-out-the-jams (virtuoso/boot))
