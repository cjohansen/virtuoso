(ns virtuoso.elements.metronome-scenes
  (:require [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.page :as page]
            [virtuoso.pages.metronome :as metronome]))

(defscene paused
  (-> {:activity/paused? true
       :music/tempo 60
       :music/bars [{:music/time-signature [4 4]}]}
      metronome/prepare-metronome
      page/page))

(defscene higher-tempo
  (-> {:activity/paused? true
       :music/tempo 195
       :music/bars [{:music/time-signature [4 4]}]}
      metronome/prepare-metronome
      page/page))

(defscene playing
  (-> {:music/tempo 195
       :music/bars [{:music/time-signature [4 4]}]}
      metronome/prepare-metronome
      page/page))
