(ns virtuoso.pages.metronome-test
  (:require [clojure.test :refer [deftest is testing]]
            [virtuoso.pages.metronome :as sut]))

(deftest prepare-badge-test
  (testing "Displays current tempo"
    (is (= (sut/prepare-badge {:music/tempo 150})
           {:kind :element.kind/round-badge
            :text "150"
            :label "BPM"
            :theme :success}))))

(deftest prepare-button-panel-test
  (testing "All buttons are enabled by default"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 60
                  :metronome/bars [{}]})
                :buttons
                (map #(select-keys % [:text :icon :icon-after-label :kbd :disabled?])))
           [{:text "Lower tempo by 5 bpm"
             :icon :phosphor.bold/minus
             :icon-after-label "5"
             :kbd "p"}
            {:text "Lower tempo"
             :icon :phosphor.bold/minus
             :kbd "-"}
            {:text "Pause"
             :icon :phosphor.fill/pause
             :kbd "space"}
            {:text "Bump tempo"
             :icon :phosphor.bold/plus
             :kbd "+"}
            {:text "Bump tempo by 5 bpm"
             :icon :phosphor.bold/plus
             :icon-after-label "5"
             :kbd "n"}])))

  (testing "Defaults to skip-lowering by 5 bpm"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 60
                  :metronome/bars [{}]})
                :buttons
                first
                :actions)
           [[:action/db.add {:music/tempo 60, :metronome/bars [{}]} :music/tempo 55]
            [:action/start-metronome [{}] 55]])))

  (testing "Skips down by preferred step size"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 60
                  :metronome/tempo-step-size 8
                  :metronome/bars [{}]})
                :buttons
                first)
           {:text "Lower tempo by 8 bpm"
            :icon :phosphor.bold/minus
            :icon-after-label "8"
            :kbd "p"
            :actions [[:action/db.add {:music/tempo 60
                                       :metronome/tempo-step-size 8
                                       :metronome/bars [{}]}
                       :music/tempo 52]
                      [:action/start-metronome [{}] 52]]})))

  (testing "Lowers tempo by a single bpm"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 120
                  :metronome/bars [{}]})
                :buttons
                second
                :actions)
           [[:action/db.add {:music/tempo 120, :metronome/bars [{}]} :music/tempo 119]
            [:action/start-metronome [{}] 119]])))

  (testing "Bumps tempo by a single bpm"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 120
                  :metronome/bars [{}]})
                :buttons
                (drop 3)
                first
                :actions)
           [[:action/db.add {:music/tempo 120, :metronome/bars [{}]} :music/tempo 121]
            [:action/start-metronome [{}] 121]])))

  (testing "Defaults to skip increasing by 5 bpm"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 60
                  :metronome/bars [{}]})
                :buttons
                last
                :actions)
           [[:action/db.add {:music/tempo 60, :metronome/bars [{}]} :music/tempo 65]
            [:action/start-metronome [{}] 65]])))

  (testing "Skips up by preferred step size"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 60
                  :metronome/tempo-step-size 8
                  :metronome/bars [{}]})
                :buttons
                last)
           {:text "Bump tempo by 8 bpm"
            :icon :phosphor.bold/plus
            :icon-after-label "8"
            :kbd "n"
            :actions [[:action/db.add {:music/tempo 60
                                       :metronome/tempo-step-size 8
                                       :metronome/bars [{}]}
                       :music/tempo 68]
                      [:action/start-metronome [{}] 68]]})))

  (testing "Play button starts metronome"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 95
                  :activity/paused? true
                  :metronome/bars [{}]})
                :buttons
                (drop 2)
                first
                :actions)
           [[:action/start-metronome [{}] 95]])))

  (testing "Pause button stops metronome"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 90
                  :metronome/bars [{}]})
                :buttons
                (drop 2)
                first
                :actions)
           [[:action/stop-metronome]]))))
