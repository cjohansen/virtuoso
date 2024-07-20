(ns virtuoso.metronome-test
  (:require [clojure.test :refer [deftest is testing]]
            [virtuoso.metronome :as sut]))

(deftest generate-clicks-test
  (testing "Generates clicks for 4/4 @60BPM"
    (is (= (-> [{:music/time-signature [4 4]
                 :music/tempo 60}]
               sut/generate-clicks
               :clicks)
           [{:bar/n 1 :bar/beat 1 :beat/n 1 :metronome/click-at 0000}
            {:bar/n 1 :bar/beat 2 :beat/n 2 :metronome/click-at 1000}
            {:bar/n 1 :bar/beat 3 :beat/n 3 :metronome/click-at 2000}
            {:bar/n 1 :bar/beat 4 :beat/n 4 :metronome/click-at 3000}])))

  (testing "Generates clicks for 3/4 @60BPM"
    (is (= (-> [{:music/time-signature [3 4]
                 :music/tempo 60}]
               sut/generate-clicks
               :clicks)
           [{:bar/n 1 :bar/beat 1 :beat/n 1 :metronome/click-at 0000}
            {:bar/n 1 :bar/beat 2 :beat/n 2 :metronome/click-at 1000}
            {:bar/n 1 :bar/beat 3 :beat/n 3 :metronome/click-at 2000}])))

  (testing "Generates clicks against bar division by default"
    (is (= (-> [{:music/time-signature [6 8]
                 :music/tempo 60}]
               sut/generate-clicks
               :clicks)
           [{:bar/n 1 :bar/beat 1 :beat/n 1 :metronome/click-at 0000}
            {:bar/n 1 :bar/beat 2 :beat/n 2 :metronome/click-at 1000}
            {:bar/n 1 :bar/beat 3 :beat/n 3 :metronome/click-at 2000}
            {:bar/n 1 :bar/beat 4 :beat/n 4 :metronome/click-at 3000}
            {:bar/n 1 :bar/beat 5 :beat/n 5 :metronome/click-at 4000}
            {:bar/n 1 :bar/beat 6 :beat/n 6 :metronome/click-at 5000}])))

  (testing "Generates clicks against a quarter note pulse"
    (is (= (-> [{:music/time-signature [6 8]
                 :music/tempo 60}]
               (sut/generate-clicks {:relative-subdivision 4})
               :clicks)
           [{:bar/n 1 :bar/beat 1 :beat/n 1 :metronome/click-at 0}
            {:bar/n 1 :bar/beat 2 :beat/n 2 :metronome/click-at 500}
            {:bar/n 1 :bar/beat 3 :beat/n 3 :metronome/click-at 1000}
            {:bar/n 1 :bar/beat 4 :beat/n 4 :metronome/click-at 1500}
            {:bar/n 1 :bar/beat 5 :beat/n 5 :metronome/click-at 2000}
            {:bar/n 1 :bar/beat 6 :beat/n 6 :metronome/click-at 2500}])))

  (testing "Repeats a bar"
    (is (= (-> [{:music/time-signature [2 4]
                 :music/tempo 60
                 :bar/reps 2}]
               sut/generate-clicks
               :clicks)
           [{:bar/n 1 :bar/beat 1 :beat/n 1 :metronome/click-at 0000}
            {:bar/n 1 :bar/beat 2 :beat/n 2 :metronome/click-at 1000}
            {:bar/n 2 :bar/beat 1 :beat/n 3 :metronome/click-at 2000}
            {:bar/n 2 :bar/beat 2 :beat/n 4 :metronome/click-at 3000}])))

  (testing "Accentuates the first beat of the bar"
    (is (= (->> [{:music/time-signature [4 4]
                  :music/tempo 60
                  :bar/reps 2
                  :accentuate? (comp #{1} :bar/beat)}]
                sut/generate-clicks
                :clicks
                (filter :metronome/accentuate?))
           [{:bar/n 1 :bar/beat 1 :beat/n 1 :metronome/click-at 0, :metronome/accentuate? true}
            {:bar/n 2 :bar/beat 1 :beat/n 5 :metronome/click-at 4000, :metronome/accentuate? true}])))

  (testing "Accentuates the first beat of the bar with data"
    (is (= (->> [{:music/time-signature [4 4]
                  :music/tempo 60
                  :bar/reps 2
                  :metronome/accentuate-beats #{1}}]
                sut/generate-clicks
                :clicks
                (filter :metronome/accentuate?))
           [{:bar/n 1 :bar/beat 1 :beat/n 1 :metronome/click-at 0, :metronome/accentuate? true}
            {:bar/n 2 :bar/beat 1 :beat/n 5 :metronome/click-at 4000, :metronome/accentuate? true}])))

  (testing "Drops some clicks"
    (is (= (->> [{:music/time-signature [4 4]
                  :music/tempo 60
                  :bar/reps 2
                  :click? (comp #{1 4} :bar/beat)}]
                sut/generate-clicks
                :clicks)
           [{:bar/n 1 :bar/beat 1 :beat/n 1 :metronome/click-at 0}
            {:bar/n 1 :bar/beat 4 :beat/n 4 :metronome/click-at 3000}
            {:bar/n 2 :bar/beat 1 :beat/n 5 :metronome/click-at 4000}
            {:bar/n 2 :bar/beat 4 :beat/n 8 :metronome/click-at 7000}])))

  (testing "Drops some clicks with data"
    (is (= (->> [{:music/time-signature [4 4]
                  :music/tempo 60
                  :bar/reps 2
                  :metronome/click-beats #{1 4}}]
                sut/generate-clicks
                :clicks)
           [{:bar/n 1 :bar/beat 1 :beat/n 1 :metronome/click-at 0}
            {:bar/n 1 :bar/beat 4 :beat/n 4 :metronome/click-at 3000}
            {:bar/n 2 :bar/beat 1 :beat/n 5 :metronome/click-at 4000}
            {:bar/n 2 :bar/beat 4 :beat/n 8 :metronome/click-at 7000}])))

  (testing "Drops some clicks randomly with data"
    (is (< (->> [{:music/time-signature [4 4]
                  :music/tempo 60
                  :bar/reps 2
                  :metronome/drop-pct 75}]
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
                :clicks)
           [{:bar/n 1 :bar/beat 1 :beat/n 1 :metronome/click-at 0}
            {:bar/n 1 :bar/beat 2 :beat/n 2 :metronome/click-at 1000}
            {:bar/n 1 :bar/beat 3 :beat/n 3 :metronome/click-at 2000}
            {:bar/n 1 :bar/beat 4 :beat/n 4 :metronome/click-at 3000}
            {:bar/n 2 :bar/beat 1 :beat/n 5 :metronome/click-at 4000}
            {:bar/n 2 :bar/beat 2 :beat/n 6 :metronome/click-at 4500}
            {:bar/n 2 :bar/beat 3 :beat/n 7 :metronome/click-at 5000}
            {:bar/n 2 :bar/beat 4 :beat/n 8 :metronome/click-at 5500}])))

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
           {:clicks [{:bar/n 1, :bar/beat 1, :beat/n 1, :metronome/click-at 0, :metronome/accentuate? true}
                     {:bar/n 1, :bar/beat 2, :beat/n 2, :metronome/click-at 500}
                     {:bar/n 1, :bar/beat 3, :beat/n 3, :metronome/click-at 1000}
                     {:bar/n 1, :bar/beat 4, :beat/n 4, :metronome/click-at 1500}
                     {:bar/n 2, :bar/beat 1, :beat/n 5, :metronome/click-at 2000, :metronome/accentuate? true}
                     {:bar/n 2, :bar/beat 2, :beat/n 6, :metronome/click-at 2500}
                     {:bar/n 2, :bar/beat 3, :beat/n 7, :metronome/click-at 3000}
                     {:bar/n 2, :bar/beat 4, :beat/n 8, :metronome/click-at 3500}
                     {:bar/n 3, :bar/beat 1, :beat/n 9, :metronome/click-at 4000N, :metronome/accentuate? true}
                     {:bar/n 3, :bar/beat 2, :beat/n 10, :metronome/click-at 13000/3}
                     {:bar/n 3, :bar/beat 3, :beat/n 11, :metronome/click-at 14000/3}
                     {:bar/n 3, :bar/beat 4, :beat/n 12, :metronome/click-at 5000N}
                     {:bar/n 3, :bar/beat 5, :beat/n 13, :metronome/click-at 16000/3}
                     {:bar/n 3, :bar/beat 6, :beat/n 14, :metronome/click-at 17000/3}
                     {:bar/n 4, :bar/beat 1, :beat/n 15, :metronome/click-at 6000N, :metronome/accentuate? true}
                     {:bar/n 4, :bar/beat 2, :beat/n 16, :metronome/click-at 19000/3}
                     {:bar/n 4, :bar/beat 3, :beat/n 17, :metronome/click-at 20000/3}
                     {:bar/n 4, :bar/beat 4, :beat/n 18, :metronome/click-at 7000N}
                     {:bar/n 4, :bar/beat 5, :beat/n 19, :metronome/click-at 22000/3}
                     {:bar/n 4, :bar/beat 6, :beat/n 20, :metronome/click-at 23000/3}]
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
