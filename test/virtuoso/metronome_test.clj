(ns virtuoso.metronome-test
  (:require [clojure.test :refer [deftest is testing]]
            [virtuoso.metronome :as sut]))

(defn simplify-click [click]
  (select-keys click [:beat/n :metronome/click-at :metronome/accentuate?]))

(deftest generate-clicks-test
  (testing "Generates clicks for 4/4 quarter notes @60BPM"
    (is (= (-> [{:music/time-signature [4 4]
                 :music/tempo 60
                 :bar/rhythm [(/ 1 4)]}]
               sut/generate-clicks
               :clicks)
           [{:bar/idx 1 :bar/n 1 :bar/beat 1 :beat/n 1 :rhythm/n 1 :metronome/click-at 0000}
            {:bar/idx 1 :bar/n 1 :bar/beat 2 :beat/n 2 :rhythm/n 1 :metronome/click-at 1000}
            {:bar/idx 1 :bar/n 1 :bar/beat 3 :beat/n 3 :rhythm/n 1 :metronome/click-at 2000}
            {:bar/idx 1 :bar/n 1 :bar/beat 4 :beat/n 4 :rhythm/n 1 :metronome/click-at 3000}])))

  (testing "Generates clicks for 4/4 eight notes @60BPM"
    (is (= (-> [{:music/time-signature [4 4]
                 :music/tempo 60
                 :bar/rhythm [(/ 1 8) (/ 1 8)]}]
               sut/generate-clicks
               :clicks)
           [{:bar/idx 1 :bar/n 1 :bar/beat 1 :beat/n 1 :rhythm/n 1 :metronome/click-at    0}
            {:bar/idx 1 :bar/n 1 :bar/beat 1 :beat/n 1 :rhythm/n 2 :metronome/click-at  500}
            {:bar/idx 1 :bar/n 1 :bar/beat 2 :beat/n 2 :rhythm/n 1 :metronome/click-at 1000}
            {:bar/idx 1 :bar/n 1 :bar/beat 2 :beat/n 2 :rhythm/n 2 :metronome/click-at 1500}
            {:bar/idx 1 :bar/n 1 :bar/beat 3 :beat/n 3 :rhythm/n 1 :metronome/click-at 2000}
            {:bar/idx 1 :bar/n 1 :bar/beat 3 :beat/n 3 :rhythm/n 2 :metronome/click-at 2500}
            {:bar/idx 1 :bar/n 1 :bar/beat 4 :beat/n 4 :rhythm/n 1 :metronome/click-at 3000}
            {:bar/idx 1 :bar/n 1 :bar/beat 4 :beat/n 4 :rhythm/n 2 :metronome/click-at 3500}])))

  (testing "Generates quarter note clicks in a 9/16 bar"
    (is (= (->> [{:music/time-signature [9 16]
                  :music/tempo 60
                  :bar/rhythm [(/ 1 4)]}]
                sut/generate-clicks
                :clicks
                (map simplify-click))
           [{:beat/n 1 :metronome/click-at 0}
            {:beat/n 5 :metronome/click-at 1000N}
            {:beat/n 9 :metronome/click-at 2000N}])))

  (testing "Clicks the beginning of the bar even if it means cutting the rhythm short"
    ;; Quarter notes don't add up in a 9/16 bar.
    ;; When this happens, the metronome should reset at the beginning of the bar.
    (is (= (->> [{:music/time-signature [9 16]
                  :music/tempo 60
                  :bar/reps 2
                  :bar/rhythm [(/ 1 4)]}]
                sut/generate-clicks
                :clicks
                (map simplify-click))
           [{:beat/n 1 :metronome/click-at 0}
            {:beat/n 5 :metronome/click-at 1000N}
            {:beat/n 9 :metronome/click-at 2000N}
            {:beat/n 10 :metronome/click-at 2250}
            {:beat/n 14 :metronome/click-at 3250N}
            {:beat/n 18 :metronome/click-at 4250N}])))

  (testing "Generates clicks for 3/4 @60BPM"
    (is (= (->> [{:music/time-signature [3 4]
                  :music/tempo 60}]
                sut/generate-clicks
                :clicks
                (map simplify-click))
           [{:beat/n 1 :metronome/click-at 0000}
            {:beat/n 2 :metronome/click-at 1000}
            {:beat/n 3 :metronome/click-at 2000}])))

  (testing "Generates triplet clicks in 2/4 @60BPM"
    (is (= (->> [{:music/time-signature [2 4]
                  :music/tempo 60
                  :bar/rhythm [(* (/ 1 8) (/ 2 3))
                               (* (/ 1 8) (/ 2 3))
                               (* (/ 1 8) (/ 2 3))]}]
                sut/generate-clicks
                :clicks
                (map simplify-click))
           [{:beat/n 1 :metronome/click-at 0}
            {:beat/n 1 :metronome/click-at 1000/3}
            {:beat/n 1 :metronome/click-at 2000/3}
            {:beat/n 2 :metronome/click-at 1000N}
            {:beat/n 2 :metronome/click-at 4000/3}
            {:beat/n 2 :metronome/click-at 5000/3}])))

  (testing "Generates clicks against a quarter note beat"
    (is (= (->> [{:music/time-signature [6 8]
                  :music/tempo 60}]
                sut/generate-clicks
                :clicks
                (map simplify-click))
           [{:beat/n 1 :metronome/click-at 0}
            {:beat/n 2 :metronome/click-at 500}
            {:beat/n 3 :metronome/click-at 1000}
            {:beat/n 4 :metronome/click-at 1500}
            {:beat/n 5 :metronome/click-at 2000}
            {:beat/n 6 :metronome/click-at 2500}])))

  (testing "Repeats a bar"
    (is (= (->> [{:music/time-signature [2 4]
                  :music/tempo 60
                  :bar/reps 2}]
                sut/generate-clicks
                :clicks
                (map simplify-click))
           [{:beat/n 1 :metronome/click-at 0000}
            {:beat/n 2 :metronome/click-at 1000}
            {:beat/n 3 :metronome/click-at 2000}
            {:beat/n 4 :metronome/click-at 3000}])))

  (testing "Accentuates the first beat of the bar"
    (is (= (->> [{:music/time-signature [4 4]
                  :music/tempo 60
                  :bar/reps 2
                  :accentuate? (comp #{1} :bar/beat)}]
                sut/generate-clicks
                :clicks
                (filter :metronome/accentuate?)
                (map simplify-click))
           [{:beat/n 1 :metronome/click-at 0, :metronome/accentuate? true}
            {:beat/n 5 :metronome/click-at 4000, :metronome/accentuate? true}])))

  (testing "Accentuates the first beat of the bar with data"
    (is (= (->> [{:music/time-signature [4 4]
                  :music/tempo 60
                  :bar/reps 2
                  :metronome/accentuate-beats #{1}}]
                sut/accentuate-beats
                sut/generate-clicks
                :clicks
                (filter :metronome/accentuate?)
                (map simplify-click))
           [{:beat/n 1 :metronome/click-at 0, :metronome/accentuate? true}
            {:beat/n 5 :metronome/click-at 4000, :metronome/accentuate? true}])))

  (testing "Drops some clicks"
    (is (= (->> [{:music/time-signature [4 4]
                  :music/tempo 60
                  :bar/reps 2
                  :click? (comp #{1 4} :bar/beat)}]
                sut/generate-clicks
                :clicks
                (map simplify-click))
           [{:beat/n 1 :metronome/click-at 0}
            {:beat/n 4 :metronome/click-at 3000}
            {:beat/n 5 :metronome/click-at 4000}
            {:beat/n 8 :metronome/click-at 7000}])))

  (testing "Drops some clicks with data"
    (is (= (->> [{:music/time-signature [4 4]
                  :music/tempo 60
                  :bar/reps 2
                  :metronome/click-beats #{1 4}}]
                sut/click-beats
                sut/generate-clicks
                :clicks
                (map simplify-click))
           [{:beat/n 1 :metronome/click-at 0}
            {:beat/n 4 :metronome/click-at 3000}
            {:beat/n 5 :metronome/click-at 4000}
            {:beat/n 8 :metronome/click-at 7000}])))

  (testing "Clicks only on specified beats while dropping random beats"
    (is (= (->> [{:music/time-signature [4 4]
                  :music/tempo 60
                  :bar/reps 10
                  :metronome/click-beats #{1 4}
                  :metronome/drop-pct 50}]
                sut/click-beats
                sut/generate-clicks
                :clicks
                (map :bar/beat)
                set)
           #{1 4})))

  (testing "Drops random beats while clicking specific beats"
    (is (< (->> [{:music/time-signature [4 4]
                  :music/tempo 60
                  :bar/reps 10
                  :metronome/click-beats #{1 4}
                  :metronome/drop-pct 75}]
                sut/click-beats
                sut/generate-clicks
                :clicks
                (map :bar/beat)
                count)
           20)))

  (testing "Drops some clicks randomly with data"
    (is (< (->> [{:music/time-signature [4 4]
                  :music/tempo 60
                  :bar/reps 2
                  :metronome/drop-pct 75}]
                sut/click-beats
                sut/generate-clicks
                :clicks
                count)
           8)))

  (testing "Uses individual bar tempo"
    (is (= (->> [{:music/time-signature [4 4]
                  :music/tempo 60}
                 {:music/time-signature [4 4]
                  :music/tempo 120}]
                sut/generate-clicks
                :clicks
                (map simplify-click))
           [{:beat/n 1 :metronome/click-at 0}
            {:beat/n 2 :metronome/click-at 1000}
            {:beat/n 3 :metronome/click-at 2000}
            {:beat/n 4 :metronome/click-at 3000}
            {:beat/n 5 :metronome/click-at 4000}
            {:beat/n 6 :metronome/click-at 4500}
            {:beat/n 7 :metronome/click-at 5000}
            {:beat/n 8 :metronome/click-at 5500}])))

  (testing "Calculates beat numbers in eigths"
    (is (= (->> [{:music/time-signature [6 8]
                  :music/tempo 90
                  :bar/reps 2
                  :accentuate? (comp #{1} :bar/beat)}]
                sut/generate-clicks
                :clicks
                (map #(select-keys % [:bar/n :bar/beat :beat/n])))
           [{:bar/n 1 :bar/beat 1 :beat/n 1}
            {:bar/n 1 :bar/beat 2 :beat/n 2}
            {:bar/n 1 :bar/beat 3 :beat/n 3}
            {:bar/n 1 :bar/beat 4 :beat/n 4}
            {:bar/n 1 :bar/beat 5 :beat/n 5}
            {:bar/n 1 :bar/beat 6 :beat/n 6}

            {:bar/n 2 :bar/beat 1 :beat/n 7}
            {:bar/n 2 :bar/beat 2 :beat/n 8}
            {:bar/n 2 :bar/beat 3 :beat/n 9}
            {:bar/n 2 :bar/beat 4 :beat/n 10}
            {:bar/n 2 :bar/beat 5 :beat/n 11}
            {:bar/n 2 :bar/beat 6 :beat/n 12}])))

  (testing "Generates longer sequence of clicks"
    (is (= (->> [{:music/time-signature [4 4]
                  :music/tempo 120
                  :bar/reps 2
                  :accentuate? (comp #{1} :bar/beat)}
                 {:music/time-signature [6 8]
                  :music/tempo 90
                  :bar/reps 2
                  :accentuate? (comp #{1} :bar/beat)}]
                sut/generate-clicks)
           {:clicks [{:rhythm/n 1, :bar/idx 1, :bar/n 1, :bar/beat 1, :beat/n 1, :metronome/click-at 0, :metronome/accentuate? true}
                     {:rhythm/n 1, :bar/idx 1, :bar/n 1, :bar/beat 2, :beat/n 2, :metronome/click-at 500}
                     {:rhythm/n 1, :bar/idx 1, :bar/n 1, :bar/beat 3, :beat/n 3, :metronome/click-at 1000}
                     {:rhythm/n 1, :bar/idx 1, :bar/n 1, :bar/beat 4, :beat/n 4, :metronome/click-at 1500}
                     {:rhythm/n 1, :bar/idx 1, :bar/n 2, :bar/beat 1, :beat/n 5, :metronome/click-at 2000, :metronome/accentuate? true}
                     {:rhythm/n 1, :bar/idx 1, :bar/n 2, :bar/beat 2, :beat/n 6, :metronome/click-at 2500}
                     {:rhythm/n 1, :bar/idx 1, :bar/n 2, :bar/beat 3, :beat/n 7, :metronome/click-at 3000}
                     {:rhythm/n 1, :bar/idx 1, :bar/n 2, :bar/beat 4, :beat/n 8, :metronome/click-at 3500}
                     {:rhythm/n 1, :bar/idx 2, :bar/n 3, :bar/beat 1, :beat/n 9, :metronome/click-at 4000N, :metronome/accentuate? true}
                     {:rhythm/n 1, :bar/idx 2, :bar/n 3, :bar/beat 2, :beat/n 10, :metronome/click-at 13000/3}
                     {:rhythm/n 1, :bar/idx 2, :bar/n 3, :bar/beat 3, :beat/n 11, :metronome/click-at 14000/3}
                     {:rhythm/n 1, :bar/idx 2, :bar/n 3, :bar/beat 4, :beat/n 12, :metronome/click-at 5000N}
                     {:rhythm/n 1, :bar/idx 2, :bar/n 3, :bar/beat 5, :beat/n 13, :metronome/click-at 16000/3}
                     {:rhythm/n 1, :bar/idx 2, :bar/n 3, :bar/beat 6, :beat/n 14, :metronome/click-at 17000/3}
                     {:rhythm/n 1, :bar/idx 2, :bar/n 4, :bar/beat 1, :beat/n 15, :metronome/click-at 6000N, :metronome/accentuate? true}
                     {:rhythm/n 1, :bar/idx 2, :bar/n 4, :bar/beat 2, :beat/n 16, :metronome/click-at 19000/3}
                     {:rhythm/n 1, :bar/idx 2, :bar/n 4, :bar/beat 3, :beat/n 17, :metronome/click-at 20000/3}
                     {:rhythm/n 1, :bar/idx 2, :bar/n 4, :bar/beat 4, :beat/n 18, :metronome/click-at 7000N}
                     {:rhythm/n 1, :bar/idx 2, :bar/n 4, :bar/beat 5, :beat/n 19, :metronome/click-at 22000/3}
                     {:rhythm/n 1, :bar/idx 2, :bar/n 4, :bar/beat 6, :beat/n 20, :metronome/click-at 23000/3}]
            :bar-count 4
            :beat-count 20
            :time 8000N
            :duration 8000N}))))

(deftest set-tempo-test
  (testing "Changes tempo of bar"
    (is (= (sut/set-tempo 40 [{:music/tempo 60
                               :music/time-signature [4 4]}])
           [{:music/tempo 40
             :music/time-signature [4 4]}])))

  (testing "Uses exact specified tempo"
    (is (= (sut/set-tempo 50 [{:music/tempo 60}])
           [{:music/tempo 50}])))

  (testing "Scales tempo linearly across bars"
    (is (= (sut/set-tempo 50 [{:music/tempo 60}
                              {:music/tempo 120}
                              {:music/tempo 90}])
           [{:music/tempo 50}
            {:music/tempo 100}
            {:music/tempo 75}])))

  (testing "Sets tempo when not specified"
    (is (= (sut/set-tempo 60 [{:music/time-signature [4 4]}
                              {:music/time-signature [6 8]}])
           [{:music/time-signature [4 4] :music/tempo 60}
            {:music/time-signature [6 8] :music/tempo 60}])))

  (testing "Defaults same tempo across all bars"
    (is (= (sut/set-tempo 80 [{:music/tempo 120
                               :music/time-signature [4 4]}
                              {:music/time-signature [6 8]}])
           [{:music/time-signature [4 4] :music/tempo 80}
            {:music/time-signature [6 8] :music/tempo 80}]))))

(deftest set-default-test
  (testing "Applies metronome-wide settings on bars"
    (is (= (sut/set-default :metronome/drop-pct 0.3 [{:music/time-signature [4 4]}
                                                     {:music/time-signature [6 8]}])
           [{:music/time-signature [4 4]
             :metronome/drop-pct 0.3}
            {:music/time-signature [6 8]
             :metronome/drop-pct 0.3}])))

  (testing "Keeps bar-specific overrides"
    (is (= (sut/set-default :metronome/drop-pct 0.3 [{:music/time-signature [4 4]}
                                                     {:music/time-signature [6 8]
                                                      :metronome/drop-pct 0.25}])
           [{:music/time-signature [4 4]
             :metronome/drop-pct 0.3}
            {:music/time-signature [6 8]
             :metronome/drop-pct 0.25}]))))
