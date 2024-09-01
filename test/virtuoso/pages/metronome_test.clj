(ns virtuoso.pages.metronome-test
  (:require [clojure.test :refer [deftest is testing]]
            [datascript.core :as d]
            [virtuoso.pages.metronome :as sut]
            [virtuoso.test-helper :as helper]))

(deftest prepare-badge-test
  (testing "Displays current tempo"
    (is (= (sut/prepare-badge {:music/tempo 150})
           {:kind :element.kind/round-badge
            :text "150"
            :label "BPM"
            :theme :success})))

  (testing "Dims badge when metronome is paused"
    (is (= (sut/prepare-badge
            {:music/tempo 150
             :activity/paused? true})
           {:kind :element.kind/round-badge
            :text "150"
            :label "BPM"
            :theme :neutral}))))

(deftest prepare-button-panel-test
  (testing "All buttons are enabled by default"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 60
                  :music/bars [{}]})
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
                 {:activity/paused? true
                  :music/tempo 60
                  :music/bars [{}]})
                :buttons
                first
                :actions)
           [[:action/db.add {:activity/paused? true, :music/tempo 60, :music/bars [{}]} :music/tempo 55]])))

  (testing "Lowering by 5 bpm does not start paused metronome"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 60
                  :activity/paused? true
                  :music/bars [{}]})
                :buttons
                first
                :actions)
           [[:action/db.add {:music/tempo 60
                             :activity/paused? true
                             :music/bars [{}]} :music/tempo 55]])))

  (testing "Skips down by preferred step size"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 60
                  :metronome/tempo-step-size 8
                  :music/bars [{}]})
                :buttons
                first)
           {:text "Lower tempo by 8 bpm"
            :icon :phosphor.bold/minus
            :icon-size :tiny
            :icon-after-label "8"
            :kbd "p"
            :actions [[:action/db.add {:music/tempo 60
                                       :metronome/tempo-step-size 8
                                       :music/bars [{}]}
                       :music/tempo 52]
                      [:action/start-metronome {:music/bars [{}]
                                                :metronome/tempo-step-size 8
                                                :music/tempo 52}
                       {:on-click
                        [[:action/transact
                          [{:db/id nil
                            :metronome/current-bar [:metronome/click :bar/idx]
                            :metronome/current-beat [:metronome/click :bar/beat]}]]]}]]})))

  (testing "Lowers tempo by a single bpm"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 120
                  :music/bars [{}]})
                :buttons
                second
                :actions
                first)
           [:action/db.add {:music/tempo 120, :music/bars [{}]} :music/tempo 119])))

  (testing "Bumps tempo by a single bpm"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 120
                  :music/bars [{}]})
                :buttons
                (drop 3)
                first
                :actions
                first)
           [:action/db.add {:music/tempo 120, :music/bars [{}]} :music/tempo 121])))

  (testing "Defaults to skip increasing by 5 bpm"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 60
                  :music/bars [{}]})
                :buttons
                last
                :actions
                first)
           [:action/db.add {:music/tempo 60, :music/bars [{}]} :music/tempo 65])))

  (testing "Skips up by preferred step size"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 60
                  :metronome/tempo-step-size 8
                  :music/bars [{}]})
                :buttons
                last)
           {:text "Bump tempo by 8 bpm"
            :icon :phosphor.bold/plus
            :icon-size :tiny
            :icon-after-label "8"
            :kbd "n"
            :actions [[:action/db.add {:music/tempo 60
                                       :metronome/tempo-step-size 8
                                       :music/bars [{}]}
                       :music/tempo 68]
                      [:action/start-metronome {:music/tempo 68
                                                :metronome/tempo-step-size 8
                                                :music/bars [{}]}
                       {:on-click
                        [[:action/transact
                          [{:db/id nil
                            :metronome/current-bar [:metronome/click :bar/idx]
                            :metronome/current-beat [:metronome/click :bar/beat]}]]]}]]})))

  (testing "Play button starts metronome"
    (is (= (->> (sut/prepare-button-panel
                 {:db/id 7
                  :music/tempo 95
                  :activity/paused? true
                  :music/bars [{}]})
                :buttons
                (drop 2)
                first
                :actions)
           [[:action/db.retract {:db/id 7
                                 :music/tempo 95
                                 :activity/paused? true
                                 :music/bars [{}]} :activity/paused?]
            [:action/start-metronome {:db/id 7
                                      :music/tempo 95
                                      :activity/paused? true
                                      :music/bars [{}]}
             {:on-click
              [[:action/transact
                [{:db/id 7
                  :metronome/current-bar [:metronome/click :bar/idx]
                  :metronome/current-beat [:metronome/click :bar/beat]}]]]}]])))

  (testing "Pause button stops metronome"
    (is (= (->> (sut/prepare-button-panel
                 {:music/tempo 90
                  :music/bars [{}]})
                :buttons
                (drop 2)
                first
                :actions)
           [[:action/db.add {:music/tempo 90
                             :music/bars [{}]}
             :activity/paused? true]
            [:action/stop-metronome]]))))

(deftest prepare-bars-test
  (testing "Prepares single default bar"
    (is (= (-> (sut/prepare-bars
                nil
                {:db/id 666
                 :music/tempo 90
                 :music/bars [{:db/id 1
                               :bar/rhythm [(/ 1 4)]
                               :music/time-signature [4 4]}]})
               :bars
               helper/simplify-db-actions)
           [{:replicant/key [:bar 1]
             :beats {:val 4}
             :subdivision {:val 4}
             :rhythm {:pattern [:note/quarter]}
             :dots [{:actions [[:action/db.add {:db/id 666} :activity/paused? true]
                               [:action/stop-metronome]
                               [:action/db.add 1 :metronome/accentuate-beats 1]]}
                    {:actions [[:action/db.add {:db/id 666} :activity/paused? true]
                               [:action/stop-metronome]
                               [:action/db.add 1 :metronome/accentuate-beats 2]]}
                    {:actions [[:action/db.add {:db/id 666} :activity/paused? true]
                               [:action/stop-metronome]
                               [:action/db.add 1 :metronome/accentuate-beats 3]]}
                    {:actions [[:action/db.add {:db/id 666} :activity/paused? true]
                               [:action/stop-metronome]
                               [:action/db.add 1 :metronome/accentuate-beats 4]]}]}])))

  (testing "Displays the bar time signature"
    (is (= (-> (sut/prepare-bars
                nil
                {:db/id 666
                 :music/tempo 90
                 :activity/paused? true
                 :music/bars [{:db/id 2
                               :music/time-signature [6 8]}]})
               :bars
               first)
           {:replicant/key [:bar 2]
            :beats {:val 6}
            :subdivision {:val 8}
            :dots [{:actions [[:action/db.add 2 :metronome/accentuate-beats 1]]}
                   {:actions [[:action/db.add 2 :metronome/accentuate-beats 2]]}
                   {:actions [[:action/db.add 2 :metronome/accentuate-beats 3]]}
                   {:actions [[:action/db.add 2 :metronome/accentuate-beats 4]]}
                   {:actions [[:action/db.add 2 :metronome/accentuate-beats 5]]}
                   {:actions [[:action/db.add 2 :metronome/accentuate-beats 6]]}]})))

  (testing "Displays the currently playing tempo of a bar with explicit tempo"
    (is (= (-> (sut/prepare-bars
                nil
                {:db/id 666
                 :music/bars [{:music/time-signature [4 4]
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
                nil
                {:db/id 666
                 :music/tempo 120
                 :activity/paused? true
                 :music/bars [{:db/id 3
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

  (testing "Highlights clicking beat"
    (is (= (-> (sut/prepare-bars
                nil
                {:db/id 666
                 :music/tempo 120
                 :metronome/current-bar 1
                 :metronome/current-beat 1
                 :music/bars [{:db/id 3
                               :music/time-signature [4 4]
                               :metronome/click-beats #{1 3}}]})
               :bars
               first
               :dots
               first
               helper/simplify-db-actions)
           {:actions [[:action/db.add {:db/id 666} :activity/paused? true]
                      [:action/stop-metronome]
                      [:action/db.add 3 :metronome/accentuate-beats 1]]
            :current? true})))

  (testing "Highlights clicking even disabled beats"
    (is (= (-> (sut/prepare-bars
                nil
                {:db/id 666
                 :music/tempo 120
                 :metronome/current-bar 1
                 :metronome/current-beat 2
                 :music/bars [{:db/id 3
                               :music/time-signature [4 4]
                               :metronome/click-beats #{1 3}}]})
               :bars
               first
               :dots
               second
               helper/simplify-db-actions)
           {:disabled? true
            :current? true
            :actions [[:action/db.add {:db/id 666} :activity/paused? true]
                      [:action/stop-metronome]
                      [:action/db.add 3 :metronome/click-beats 2]]})))

  (testing "Accentuates some beats"
    (is (= (->> (sut/prepare-bars
                 nil
                 {:db/id 666
                  :music/tempo 120
                  :music/bars [{:music/time-signature [4 4]
                                :metronome/accentuate-beats #{1}}]})
                :bars
                first
                :dots
                (map :highlight?))
           [true nil nil nil])))

  (testing "Clicking accentuated beat silences it"
    (is (= (->> (sut/prepare-bars
                 nil
                 {:db/id 666
                  :music/tempo 120
                  :music/bars [{:db/id 2
                                :music/time-signature [4 4]
                                :metronome/accentuate-beats #{1}}]})
                :bars
                first
                :dots
                first
                :actions
                helper/simplify-db-actions)
           [[:action/db.add {:db/id 666} :activity/paused? true]
            [:action/stop-metronome]
            [:action/db.retract 2 :metronome/accentuate-beats 1]
            [:action/transact [{:db/id 2 :metronome/click-beats #{2 3 4}}]]])))

  (testing "Clicking accentuated beat a second time also silences it"
    (is (= (->> (sut/prepare-bars
                 nil
                 {:db/id 666
                  :music/tempo 120
                  :music/bars [{:db/id 2
                                :music/time-signature [4 4]
                                :metronome/click-beats #{1 2 3 4}
                                :metronome/accentuate-beats #{1}}]})
                :bars
                first
                :dots
                first
                :actions
                helper/simplify-db-actions)
           [[:action/db.add {:db/id 666} :activity/paused? true]
            [:action/stop-metronome]
            [:action/db.retract 2 :metronome/accentuate-beats 1]
            [:action/db.retract 2 :metronome/click-beats 1]])))

  (testing "Does not accentuate ignored beat"
    (is (= (->> (sut/prepare-bars
                 nil
                 {:db/id 666
                  :music/tempo 120
                  :music/bars [{:music/time-signature [4 4]
                                :metronome/click-beats #{2 4}
                                :metronome/accentuate-beats #{1}}]})
                :bars
                first
                :dots
                (map :disabled?))
           [true nil true nil])))

  (testing "Repeats bar"
    (is (= (-> (sut/prepare-bars
                nil
                {:db/id 666
                 :music/tempo 120
                 :music/bars [{:music/time-signature [4 4]
                               :metronome/reps 2}]})
               :bars
               first
               :reps)
           {:val 2
            :unit "times"})))

  (testing "Includes button to remove bar when there are multiple bars"
    (is (= (->> (sut/prepare-bars
                 nil
                 {:db/id 666
                  :music/tempo 120
                  :music/bars [{:db/id 1
                                :music/time-signature [4 4]}
                               {:db/id 2
                                :music/time-signature [3 4]}]})
                :bars
                first
                :buttons
                helper/simplify-db-actions)
           [{:text "Remove bar"
             :icon :phosphor.regular/minus-circle
             :theme :warn
             :actions [[:action/db.add {:db/id 666} :activity/paused? true]
                       [:action/stop-metronome]
                       [:action/transact [[:db/retractEntity 1]]]]}])))

  (testing "Doesn't need to stop metronome when removing bars from paused metronome"
    (is (= (->> (sut/prepare-bars
                 nil
                 {:db/id 666
                  :music/tempo 120
                  :activity/paused? true
                  :music/bars [{:db/id 1
                                :music/time-signature [4 4]}
                               {:db/id 2
                                :music/time-signature [3 4]}]})
                :bars
                first
                :buttons
                first
                :actions)
           [[:action/transact [[:db/retractEntity 1]]]])))

  (testing "Rounds tempos on bars"
    (is (= (->> (sut/prepare-bars
                 nil
                 {:db/id 666
                  :music/tempo 60
                  :activity/paused? true
                  :music/bars [{:ordered/idx 2
                                :music/time-signature [4 4]}
                               {:ordered/idx 2
                                :music/tempo 75.83333333333
                                :music/time-signature [4 4]}]})
                :bars
                (map :tempo))
           [nil {:val 76 :unit "BPM"}])))

  (testing "Includes button to add another bar"
    (is (= (->> (sut/prepare-bars
                 nil
                 {:db/id 666
                  :music/tempo 60
                  :music/bars [{:music/time-signature [4 4]}]})
                :buttons
                helper/simplify-db-actions)
           [{:text "Add bar"
             :icon :phosphor.regular/music-notes-plus
             :icon-size :large
             :actions [[:action/db.add {:db/id 666} :activity/paused? true]
                       [:action/stop-metronome]
                       [:action/transact
                        [{:db/id 666
                          :music/bars [{:ordered/idx 1
                                        :bar/rhythm [1/4]
                                        :music/time-signature [4 4]}]}]]]}])))

  (testing "Deleting bars can leave holes - make sure new bars have idx at the end"
    (is (= (->> (sut/prepare-bars
                 nil
                 {:db/id 666
                  :music/tempo 60
                  :activity/paused? true
                  :music/bars [{:ordered/idx 2
                                :music/time-signature [4 4]}]})
                :buttons
                first
                :actions)
           [[:action/transact
             [{:db/id 666
               :music/bars [{:ordered/idx 3
                             :bar/rhythm [1/4]
                             :music/time-signature [4 4]}]}]]]))))

(deftest prepare-form-test
  (testing "Defaults drop percent and step size"
    (is (= (->> (helper/with-conn [conn]
                  (helper/execute-actions conn (sut/get-boot-actions (d/db conn)))
                  (sut/prepare-ui-data (d/db conn)))
                :sections
                last
                helper/simplify-db-actions)
           {:kind :element.kind/boxed-form
            :boxes [{:title "Settings"
                     :fields [{:controls
                               [{:label "Drop % of beats"
                                 :inputs [{:input/kind :input.kind/number
                                           :on
                                           {:input
                                            [[:action/db.add
                                              {:db/id 2}
                                              :metronome/drop-pct
                                              :event/target-value-num]]}
                                           :value 0}]}
                                {:label "Skip interval"
                                 :inputs [{:input/kind :input.kind/number
                                           :on
                                           {:input
                                            [[:action/db.add
                                              {:db/id 2}
                                              :metronome/tempo-step-size
                                              :event/target-value-num]]}
                                           :value 5}]}]}]}]})))

  (testing "Changing the drop % stops running metronome"
    (is (= (->> (sut/prepare-form {:db/id 4})
                :boxes
                first
                :fields
                first
                :controls
                first
                :inputs
                first
                :on
                :input)
           [[:action/db.add {:db/id 4} :metronome/drop-pct :event/target-value-num]
            [:action/db.add {:db/id 4} :activity/paused? true]
            [:action/stop-metronome]]))))

(deftest prepare-modal-data-test
  (testing "Prepares for editing new bar in modal"
    (is (= (-> (sut/prepare-new-bar-modal
                {:db/id 567
                 :music/tempo 60
                 :music/bars [{:db/id 1
                               :ordered/idx 0
                               :music/time-signature [4 4]}
                              {:db/id 9
                               :bar/rhythm [1/4]
                               :ordered/idx 1}]}
                {:modal/kind ::sut/edit-new-bar-modal
                 :modal/params {:idx 1}})
               helper/simplify-db-actions
               (update :sections #(take 1 %)))
           {:title "Configure bar"
            :classes ["max-w-64"]
            :sections
            [{:kind :element.kind/bars
              :bars [{:beats {:val 4
                              :left-button {:icon :phosphor.regular/minus-circle
                                            :actions [[:action/db.add {:db/id 9} :music/time-signature [3 4]]]}
                              :right-button {:icon :phosphor.regular/plus-circle
                                             :actions [[:action/db.add {:db/id 9} :music/time-signature [5 4]]]}}
                      :subdivision {:val 4
                                    :left-button {:icon :phosphor.regular/minus-circle}
                                    :right-button {:icon :phosphor.regular/plus-circle
                                                   :actions [[:action/db.add {:db/id 9} :music/time-signature [4 8]]]}}
                      :reps {:val 1
                             :unit "time"
                             :button-above {:icon :phosphor.regular/minus-circle}
                             :button-below {:icon :phosphor.regular/plus-circle
                                            :actions [[:action/db.add {:db/id 9} :metronome/reps 2]]}}
                      :tempo {:val 60
                              :unit "BPM"
                              :actions [[:action/db.add {:db/id 1} :music/tempo 60]
                                        [:action/db.add {:db/id 9} :music/tempo :event/target-value-num]]
                              :subtle? true}
                      :dots [{:actions
                              [[:action/db.add {:db/id 567} :activity/paused? true]
                               [:action/stop-metronome]
                               [:action/db.add 9 :metronome/accentuate-beats 1]]}
                             {:actions
                              [[:action/db.add {:db/id 567} :activity/paused? true]
                               [:action/stop-metronome]
                               [:action/db.add 9 :metronome/accentuate-beats 2]]}
                             {:actions
                              [[:action/db.add {:db/id 567} :activity/paused? true]
                               [:action/stop-metronome]
                               [:action/db.add 9 :metronome/accentuate-beats 3]]}
                             {:actions
                              [[:action/db.add {:db/id 567} :activity/paused? true]
                               [:action/stop-metronome]
                               [:action/db.add 9 :metronome/accentuate-beats 4]]}]
                      :size :large}]}]})))

  (testing "Initializes time signature in new bar from the previous bar"
    (is (= (-> (sut/prepare-new-bar-modal
                {:music/tempo 60
                 :music/bars [{:db/id 1
                               :ordered/idx 0
                               :music/time-signature [3 4]}
                              {:db/id 9
                               :ordered/idx 1}]}
                {:modal/kind ::sut/edit-new-bar-modal
                 :modal/params {:idx 1}})
               helper/simplify-db-actions
               :sections
               first
               :bars
               first
               (select-keys [:beats :subdivision])
               (helper/strip-keys #{:icon}))
           {:beats {:val 3
                    :left-button {:actions [[:action/db.add {:db/id 9} :music/time-signature [2 4]]]}
                    :right-button {:actions [[:action/db.add {:db/id 9} :music/time-signature [4 4]]]}}
            :subdivision {:val 4
                          :left-button {}
                          :right-button {:actions [[:action/db.add {:db/id 9} :music/time-signature [3 8]]]}}})))

  (testing "Cannot have less than 1 beat in a bar"
    (is (= (-> (sut/prepare-new-bar-modal
                {:music/tempo 60
                 :music/bars [{:db/id 1
                               :ordered/idx 0
                               :music/time-signature [4 4]}
                              {:db/id 9
                               :ordered/idx 1
                               :music/time-signature [1 4]}]}
                {:modal/kind ::sut/edit-new-bar-modal
                 :modal/params {:idx 1}})
               helper/simplify-db-actions
               :sections
               first
               :bars
               first
               :beats)
           {:val 1
            :left-button {:icon :phosphor.regular/minus-circle}
            :right-button
            {:icon :phosphor.regular/plus-circle
             :actions [[:action/db.add {:db/id 9} :music/time-signature [2 4]]]}})))

  (testing "Can change subdivisions up and down"
    (is (= (-> (sut/prepare-new-bar-modal
                {:music/tempo 60
                 :music/bars [{:db/id 1
                               :ordered/idx 0
                               :music/time-signature [6 8]}
                              {:db/id 9
                               :ordered/idx 1}]}
                {:modal/kind ::sut/edit-new-bar-modal
                 :modal/params {:idx 1}})
               helper/simplify-db-actions
               :sections
               first
               :bars
               first
               :subdivision
               (helper/strip-keys #{:icon}))
           {:val 8
            :left-button
            {:actions [[:action/db.add {:db/id 9} :music/time-signature [6 4]]]}
            :right-button
            {:actions [[:action/db.add {:db/id 9} :music/time-signature [6 16]]]}})))

  (testing "64ths is the end of the line"
    (is (= (-> (sut/prepare-new-bar-modal
                {:music/tempo 60
                 :music/bars [{:db/id 1
                               :ordered/idx 0
                               :music/time-signature [32 64]}
                              {:db/id 9
                               :ordered/idx 1}]}
                {:modal/kind ::sut/edit-new-bar-modal
                 :modal/params {:idx 1}})
               helper/simplify-db-actions
               :sections
               first
               :bars
               first
               :subdivision
               :right-button)
           {:icon :phosphor.regular/plus-circle})))

  (testing "Does not initialize new bar with reps from previous bar"
    (is (= (-> (sut/prepare-new-bar-modal
                {:music/tempo 60
                 :music/bars [{:db/id 1
                               :ordered/idx 0
                               :metronome/reps 2
                               :music/time-signature [3 4]}
                              {:db/id 9
                               :ordered/idx 1}]}
                {:modal/kind ::sut/edit-new-bar-modal
                 :modal/params {:idx 1}})
               helper/simplify-db-actions
               :sections
               first
               :bars
               first
               :reps
               :val)
           1)))

  (testing "Can decrease reps when more than 1"
    (is (= (-> (sut/prepare-new-bar-modal
                {:music/tempo 60
                 :music/bars [{:db/id 1
                               :ordered/idx 0
                               :music/time-signature [4 4]}
                              {:db/id 8
                               :ordered/idx 1
                               :metronome/reps 2}]}
                {:modal/kind ::sut/edit-new-bar-modal
                 :modal/params {:idx 1}})
               helper/simplify-db-actions
               :sections
               first
               :bars
               first
               :reps
               helper/simplify-db-actions)
           {:val 2
            :unit "times"
            :button-above {:icon :phosphor.regular/minus-circle
                           :actions [[:action/db.add {:db/id 8} :metronome/reps 1]]}
            :button-below {:icon :phosphor.regular/plus-circle
                           :actions [[:action/db.add {:db/id 8} :metronome/reps 3]]}})))

  (testing "Displays metronome tempo as default tempo"
    (is (= (-> (sut/prepare-new-bar-modal
                {:music/tempo 80
                 :music/bars [{:db/id 1
                               :ordered/idx 0
                               :music/time-signature [4 4]}
                              {:db/id 8
                               :ordered/idx 1}]}
                {:modal/kind ::sut/edit-new-bar-modal
                 :modal/params {:idx 1}})
               helper/simplify-db-actions
               :sections
               first
               :bars
               first
               :tempo
               (dissoc :actions))
           {:val 80
            :unit "BPM"
            :subtle? true})))

  (testing "Setting the tempo reifies any existing default tempos"
    (is (= (-> (sut/prepare-new-bar-modal
                {:music/tempo 80
                 :music/bars [{:db/id 1
                               :ordered/idx 0
                               :music/time-signature [4 4]}
                              {:db/id 8
                               :ordered/idx 1}]}
                {:modal/kind ::sut/edit-new-bar-modal
                 :modal/params {:idx 1}})
               helper/simplify-db-actions
               :sections
               first
               :bars
               first
               :tempo
               :actions)
           [[:action/db.add {:db/id 1} :music/tempo 80]
            [:action/db.add {:db/id 8} :music/tempo :event/target-value-num]])))

  (testing "Displays explicitly set tempo as edited"
    (is (= (-> (sut/prepare-new-bar-modal
                {:music/tempo 80
                 :music/bars [{:db/id 1
                               :ordered/idx 0
                               :music/time-signature [4 4]}
                              {:db/id 8
                               :music/tempo 120
                               :ordered/idx 1}]}
                {:modal/kind ::sut/edit-new-bar-modal
                 :modal/params {:idx 1}})
               helper/simplify-db-actions
               :sections
               first
               :bars
               first
               :tempo)
           {:val 120
            :unit "BPM"
            :actions [[:action/db.add {:db/id 1} :music/tempo 80]
                      [:action/db.add {:db/id 8} :music/tempo :event/target-value-num]]})))

  (testing "Prepares for editing existing bar in modal"
    (is (= (-> (sut/prepare-existing-bar-edit-modal
                {:db/id 567
                 :music/tempo 60
                 :music/bars [{:db/id 1
                               :ordered/idx 0
                               :music/time-signature [4 4]}
                              {:db/id 9
                               :ordered/idx 1}]}
                {:modal/kind ::sut/edit-bar-modal
                 :modal/params {:idx 0}})
               helper/simplify-db-actions
               (update :sections #(take 1 %)))
           {:title "Configure bar"
            :classes ["max-w-64"]
            :sections
            [{:kind :element.kind/bars
              :bars
              [{:beats {:val 4
                        :left-button {:icon :phosphor.regular/minus-circle
                                      :actions [[:action/db.add {:db/id 1} :music/time-signature [3 4]]]}
                        :right-button {:icon :phosphor.regular/plus-circle
                                       :actions [[:action/db.add {:db/id 1} :music/time-signature [5 4]]]}}
                :subdivision {:val 4
                              :left-button {:icon :phosphor.regular/minus-circle}
                              :right-button {:icon :phosphor.regular/plus-circle
                                             :actions [[:action/db.add {:db/id 1} :music/time-signature [4 8]]]}}
                :reps {:val 1
                       :unit "time"
                       :button-above {:icon :phosphor.regular/minus-circle}
                       :button-below {:icon :phosphor.regular/plus-circle
                                      :actions [[:action/db.add {:db/id 1} :metronome/reps 2]]}}
                :tempo {:val 60
                        :unit "BPM"
                        :actions [[:action/db.add {:db/id 9} :music/tempo 60]
                                  [:action/db.add {:db/id 1} :music/tempo :event/target-value-num]]
                        :subtle? true}
                :dots [{:actions [[:action/db.add {:db/id 567} :activity/paused? true]
                                  [:action/stop-metronome]
                                  [:action/db.add 1 :metronome/accentuate-beats 1]]}
                       {:actions [[:action/db.add {:db/id 567} :activity/paused? true]
                                  [:action/stop-metronome]
                                  [:action/db.add 1 :metronome/accentuate-beats 2]]}
                       {:actions [[:action/db.add {:db/id 567} :activity/paused? true]
                                  [:action/stop-metronome]
                                  [:action/db.add 1 :metronome/accentuate-beats 3]]}
                       {:actions [[:action/db.add {:db/id 567} :activity/paused? true]
                                  [:action/stop-metronome]
                                  [:action/db.add 1 :metronome/accentuate-beats 4]]}]
                :size :large}]}]})))

  (testing "Can't set tempo or repeats on only bar"
    (is (= (-> (sut/prepare-existing-bar-edit-modal
                {:db/id 567
                 :music/tempo 60
                 :music/bars [{:db/id 1
                               :ordered/idx 0
                               :music/time-signature [4 4]}]}
                {:modal/kind ::sut/edit-bar-modal
                 :modal/params {:idx 0}})
               helper/simplify-db-actions
               :sections
               first
               :bars
               first
               (select-keys [:tempo :reps]))
           {})))

  (testing "Prepares rhythm selection menu"
    (is (= (-> (sut/prepare-existing-bar-edit-modal
                {:db/id 567
                 :music/tempo 60
                 :music/bars [{:db/id 9
                               :ordered/idx 0
                               :bar/rhythm [(/ 1 4)]
                               :music/time-signature [4 4]}]}
                {:modal/kind ::sut/edit-bar-modal
                 :modal/params {:idx 0}})
               helper/simplify-db-actions
               :sections
               second)
           {:kind :element.kind/musical-notation-selection
            :items [{:notation [:note/quarter]
                     :active? true}
                    {:notation [[:notation/beam :note/eighth :note/eighth]]
                     :actions [[:action/db.add {:db/id 9} :bar/rhythm [(/ 1 8) (/ 1 8)]]]}
                    {:notation [[:notation/beam [:notation/dot :note/eighth] :note/sixteenth]]
                     :actions [[:action/db.add {:db/id 9} :bar/rhythm [(* (/ 3 2) (/ 1 8)) (/ 1 16)]]]}
                    {:notation [[:notation/beam :note/eighth :note/eighth :note/eighth]]
                     :actions [[:action/db.add {:db/id 9} :bar/rhythm [(* (/ 2 3) (/ 1 8)) (* (/ 2 3) (/ 1 8)) (* (/ 2 3) (/ 1 8))]]]}
                    {:notation [[:notation/beam :note/sixteenth :note/sixteenth :note/sixteenth :note/sixteenth]]
                     :actions [[:action/db.add {:db/id 9} :bar/rhythm [(/ 1 16) (/ 1 16) (/ 1 16) (/ 1 16)]]]}
                    {:notation [[:notation/beam :note/sixteenth :note/sixteenth :note/sixteenth
                                 :note/sixteenth :note/sixteenth :note/sixteenth]]
                     :actions [[:action/db.add {:db/id 9} :bar/rhythm [(* (/ 2 3) (/ 1 16))
                                                                       (* (/ 2 3) (/ 1 16))
                                                                       (* (/ 2 3) (/ 1 16))
                                                                       (* (/ 2 3) (/ 1 16))
                                                                       (* (/ 2 3) (/ 1 16))
                                                                       (* (/ 2 3) (/ 1 16))]]]}]}))))

(deftest symbolize-rhythm-test
  (testing "Recognizes main note values"
    (is (= (sut/symbolize-rhythm [1 (/ 1 2) (/ 1 4) (/ 1 8)])
           [:note/whole
            :note/half
            :note/quarter
            :note/eighth])))

  (testing "Dots notes"
    (is (= (sut/symbolize-rhythm (map #(* (/ 3 2) %) [1 (/ 1 2) (/ 1 4) (/ 1 8) (/ 1 16)]))
           [[:notation/dot :note/whole]
            [:notation/dot :note/half]
            [:notation/dot :note/quarter]
            [:notation/dot :note/eighth]
            [:notation/dot :note/sixteenth]])))

  (testing "Beams eighths in pairs"
    (is (= (sut/symbolize-rhythm [(/ 1 8) (/ 1 8) (/ 1 8) (/ 1 8)])
           [[:notation/beam :note/eighth :note/eighth]
            [:notation/beam :note/eighth :note/eighth]])))

  (testing "Beams eighths in pairs, as far as it goes"
    (is (= (sut/symbolize-rhythm [(/ 1 8) (/ 1 8) (/ 1 8)])
           [[:notation/beam :note/eighth :note/eighth]
            :note/eighth])))

  (testing "Beams eighths and sixteenths"
    (is (= (sut/symbolize-rhythm [(/ 1 8) (/ 1 16) (/ 1 16)])
           [[:notation/beam :note/eighth :note/sixteenth :note/sixteenth]])))

  (testing "Beams dotted eighth and sixteenth"
    (is (= (sut/symbolize-rhythm [(* (/ 3 2) (/ 1 8)) (/ 1 16)
                                  (* (/ 3 2) (/ 1 8)) (/ 1 16)])
           [[:notation/beam [:notation/dot :note/eighth] :note/sixteenth]
            [:notation/beam [:notation/dot :note/eighth] :note/sixteenth]])))

  (testing "Beams eighth note triplets in triplets"
    (is (= (sut/symbolize-rhythm [(* (/ 2 3) (/ 1 8)) (* (/ 2 3) (/ 1 8)) (* (/ 2 3) (/ 1 8))])
           [[:notation/beam :note/eighth :note/eighth :note/eighth]]))))
