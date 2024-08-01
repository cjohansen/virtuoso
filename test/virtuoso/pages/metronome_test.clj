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

(deftest prepare-bars-test
  (testing "Prepares single default bar"
    (is (= (-> (sut/prepare-bars
                {:db/id 666
                 :music/tempo 90
                 :metronome/bars [{:db/id 1
                                   :music/time-signature [4 4]}]})
               :bars)
           [{:beats {:val 4}
             :subdivision {:val 4}
             :dots [{:actions [[:action/stop-metronome]
                               [:action/db.add 1 :metronome/accentuate-beats 1]]}
                    {:actions [[:action/stop-metronome]
                               [:action/db.add 1 :metronome/accentuate-beats 2]]}
                    {:actions [[:action/stop-metronome]
                               [:action/db.add 1 :metronome/accentuate-beats 3]]}
                    {:actions [[:action/stop-metronome]
                               [:action/db.add 1 :metronome/accentuate-beats 4]]}]}])))

  (testing "Displays the bar time signature"
    (is (= (-> (sut/prepare-bars
                {:db/id 666
                 :music/tempo 90
                 :activity/paused? true
                 :metronome/bars [{:db/id 2
                                   :music/time-signature [6 8]}]})
               :bars
               first)
           {:beats {:val 6}
            :subdivision {:val 8}
            :dots [{:actions [[:action/db.add 2 :metronome/accentuate-beats 1]]}
                   {:actions [[:action/db.add 2 :metronome/accentuate-beats 2]]}
                   {:actions [[:action/db.add 2 :metronome/accentuate-beats 3]]}
                   {:actions [[:action/db.add 2 :metronome/accentuate-beats 4]]}
                   {:actions [[:action/db.add 2 :metronome/accentuate-beats 5]]}
                   {:actions [[:action/db.add 2 :metronome/accentuate-beats 6]]}]})))

  (testing "Displays the currently playing tempo of a bar with explicit tempo"
    (is (= (-> (sut/prepare-bars
                {:db/id 666
                 :metronome/bars [{:music/time-signature [4 4]
                                   :music/tempo 120}
                                  {:music/time-signature [3 4]
                                   :music/tempo 60}]
                 :music/tempo 90 ;; 75% of 120, so the bar @60bpm should play @45bpm
                 })
               :bars
               second
               :tempo)
           {:val 45
            :unit "BPM"})))

  (testing "Does not click all beats"
    (is (= (-> (sut/prepare-bars
                {:db/id 666
                 :music/tempo 120
                 :activity/paused? true
                 :metronome/bars [{:db/id 3
                                   :music/time-signature [4 4]
                                   :metronome/click-beats #{1 3}}]})
               :bars
               first
               :dots)
           [{:actions [[:action/db.add 3 :metronome/accentuate-beats 1]]}
            {:disabled? true
             :actions [[:action/db.add 3 :metronome/click-beats 2]]}
            {:actions [[:action/db.add 3 :metronome/accentuate-beats 3]]}
            {:disabled? true
             :actions [[:action/db.add 3 :metronome/click-beats 4]]}])))

  (testing "Accentuates some beats"
    (is (= (->> (sut/prepare-bars
                 {:db/id 666
                  :music/tempo 120
                  :metronome/bars [{:music/time-signature [4 4]
                                    :metronome/accentuate-beats #{1}}]})
                :bars
                first
                :dots
                (map :highlight?))
           [true nil nil nil])))

  (testing "Does not accentuate ignored beat"
    (is (= (->> (sut/prepare-bars
                 {:db/id 666
                  :music/tempo 120
                  :metronome/bars [{:music/time-signature [4 4]
                                    :metronome/click-beats #{2 4}
                                    :metronome/accentuate-beats #{1}}]})
                :bars
                first
                :dots
                (map :disabled?))
           [true nil true nil])))

  (testing "Repeats bar"
    (is (= (-> (sut/prepare-bars
                {:db/id 666
                 :music/tempo 120
                 :metronome/bars [{:music/time-signature [4 4]
                                   :metronome/reps 2}]})
               :bars
               first
               :reps)
           {:val 2
            :unit "times"})))

  (testing "Includes button to remove bar when there are multiple bars"
    (is (= (->> (sut/prepare-bars
                 {:db/id 666
                  :music/tempo 120
                  :metronome/bars [{:db/id 1
                                    :music/time-signature [4 4]}
                                   {:db/id 2
                                    :music/time-signature [3 4]}]})
                :bars
                first
                :buttons)
           [{:text "Remove bar"
             :icon :phosphor.regular/minus-circle
             :theme :warn
             :actions [[:action/stop-metronome]
                       [:action/transact [[:db/retractEntity 1]]]]}])))

  (testing "Doesn't need to stop metronome when removing bars from paused metronome"
    (is (= (->> (sut/prepare-bars
                 {:db/id 666
                  :music/tempo 120
                  :activity/paused? true
                  :metronome/bars [{:db/id 1
                                    :music/time-signature [4 4]}
                                   {:db/id 2
                                    :music/time-signature [3 4]}]})
                :bars
                first
                :buttons
                first
                :actions)
           [[:action/transact [[:db/retractEntity 1]]]])))

  (testing "Includes button to add another bar"
    (is (= (->> (sut/prepare-bars
                 {:db/id 666
                  :music/tempo 60
                  :metronome/bars [{:music/time-signature [4 4]}]})
                :buttons)
           [{:text "Add bar"
             :icon :phosphor.regular/music-notes-plus
             :icon-size :large
             :actions [[:action/stop-metronome]
                       [:action/transact
                        [{:db/id 666
                          :metronome/bars [{:ordered/idx 1
                                            :music/time-signature [4 4]}]}]]]}])))

  (testing "Deleting bars can leave holes - make sure new bars have idx at the end"
    (is (= (->> (sut/prepare-bars
                 {:db/id 666
                  :music/tempo 60
                  :activity/paused? true
                  :metronome/bars [{:ordered/idx 2
                                    :music/time-signature [4 4]}]})
                :buttons
                first
                :actions)
           [[:action/transact
             [{:db/id 666
               :metronome/bars [{:ordered/idx 3
                                 :music/time-signature [4 4]}]}]]]))))
