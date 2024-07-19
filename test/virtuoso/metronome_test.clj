(ns virtuoso.metronome-test
  (:require [clojure.test :refer [deftest is testing]]
            [virtuoso.metronome :as sut]))

(deftest generate-clicks-test
  (testing "Generates clicks for 4/4 @60BPM"
    (is (= (-> [{:time-signature [4 4]
                 :tempo 60}]
               sut/generate-clicks
               :clicks)
           [{:bar-n 1 :bar-beat 1 :beat-n 1 :click-at 1000}
            {:bar-n 1 :bar-beat 2 :beat-n 2 :click-at 2000}
            {:bar-n 1 :bar-beat 3 :beat-n 3 :click-at 3000}
            {:bar-n 1 :bar-beat 4 :beat-n 4 :click-at 4000}])))

  (testing "Generates clicks for 3/4 @60BPM"
    (is (= (-> [{:time-signature [3 4]
                 :tempo 60}]
               sut/generate-clicks
               :clicks)
           [{:bar-n 1 :bar-beat 1 :beat-n 1 :click-at 1000}
            {:bar-n 1 :bar-beat 2 :beat-n 2 :click-at 2000}
            {:bar-n 1 :bar-beat 3 :beat-n 3 :click-at 3000}])))

  (testing "Generates clicks against bar division by default"
    (is (= (-> [{:time-signature [6 8]
                 :tempo 60}]
               sut/generate-clicks
               :clicks)
           [{:bar-n 1 :bar-beat 1 :beat-n 1 :click-at 1000}
            {:bar-n 1 :bar-beat 2 :beat-n 2 :click-at 2000}
            {:bar-n 1 :bar-beat 3 :beat-n 3 :click-at 3000}
            {:bar-n 1 :bar-beat 4 :beat-n 4 :click-at 4000}
            {:bar-n 1 :bar-beat 5 :beat-n 5 :click-at 5000}
            {:bar-n 1 :bar-beat 6 :beat-n 6 :click-at 6000}])))

  (testing "Generates clicks against a quarter note pulse"
    (is (= (-> [{:time-signature [6 8]
                 :tempo 60}]
               (sut/generate-clicks {:relative-subdivision 4})
               :clicks)
           [{:bar-n 1 :bar-beat 1 :beat-n 1 :click-at 500}
            {:bar-n 1 :bar-beat 2 :beat-n 2 :click-at 1000}
            {:bar-n 1 :bar-beat 3 :beat-n 3 :click-at 1500}
            {:bar-n 1 :bar-beat 4 :beat-n 4 :click-at 2000}
            {:bar-n 1 :bar-beat 5 :beat-n 5 :click-at 2500}
            {:bar-n 1 :bar-beat 6 :beat-n 6 :click-at 3000}])))

  (testing "Repeats a bar"
    (is (= (-> [{:time-signature [2 4]
                 :reps 2
                 :tempo 60}]
               sut/generate-clicks
               :clicks)
           [{:bar-n 1 :bar-beat 1 :beat-n 1 :click-at 1000}
            {:bar-n 1 :bar-beat 2 :beat-n 2 :click-at 2000}
            {:bar-n 2 :bar-beat 1 :beat-n 3 :click-at 3000}
            {:bar-n 2 :bar-beat 2 :beat-n 4 :click-at 4000}])))

  (testing "Accentuates the first beat of the bar"
    (is (= (->> [{:time-signature [4 4]
                  :accentuate? (comp #{1} :bar-beat)
                  :reps 2
                  :tempo 60}]
                sut/generate-clicks
                :clicks
                (filter :accentuate?))
           [{:bar-n 1 :bar-beat 1 :beat-n 1 :click-at 1000, :accentuate? true}
            {:bar-n 2 :bar-beat 1 :beat-n 5 :click-at 5000, :accentuate? true}])))

  (testing "Drops some clicks"
    (is (= (->> [{:time-signature [4 4]
                  :click? (comp #{1 4} :bar-beat)
                  :reps 2
                  :tempo 60}]
                sut/generate-clicks
                :clicks)
           [{:bar-n 1 :bar-beat 1 :beat-n 1 :click-at 1000}
            {:bar-n 1 :bar-beat 4 :beat-n 4 :click-at 4000}
            {:bar-n 2 :bar-beat 1 :beat-n 5 :click-at 5000}
            {:bar-n 2 :bar-beat 4 :beat-n 8 :click-at 8000}])))

  (testing "Uses individual bar tempo"
    (is (= (->> [{:time-signature [4 4]
                  :tempo 60}
                 {:time-signature [4 4]
                  :tempo 120}]
                sut/generate-clicks
                :clicks)
           [{:bar-n 1 :bar-beat 1 :beat-n 1 :click-at 1000}
            {:bar-n 1 :bar-beat 2 :beat-n 2 :click-at 2000}
            {:bar-n 1 :bar-beat 3 :beat-n 3 :click-at 3000}
            {:bar-n 1 :bar-beat 4 :beat-n 4 :click-at 4000}
            {:bar-n 2 :bar-beat 1 :beat-n 5 :click-at 4500}
            {:bar-n 2 :bar-beat 2 :beat-n 6 :click-at 5000}
            {:bar-n 2 :bar-beat 3 :beat-n 7 :click-at 5500}
            {:bar-n 2 :bar-beat 4 :beat-n 8 :click-at 6000}])))

  (testing "Generates longer sequence of clicks"
    (is (= (->> [{:time-signature [4 4]
                  :accentuate? (comp #{1} :bar-beat)
                  :reps 2
                  :tempo 120}
                 {:time-signature [6 8]
                  :accentuate? (comp #{1} :bar-beat)
                  :reps 2
                  :tempo 90}]
                sut/generate-clicks)
           {:clicks [{:bar-n 1, :bar-beat 1, :beat-n 1, :click-at 500, :accentuate? true}
                     {:bar-n 1, :bar-beat 2, :beat-n 2, :click-at 1000}
                     {:bar-n 1, :bar-beat 3, :beat-n 3, :click-at 1500}
                     {:bar-n 1, :bar-beat 4, :beat-n 4, :click-at 2000}
                     {:bar-n 2, :bar-beat 1, :beat-n 5, :click-at 2500, :accentuate? true}
                     {:bar-n 2, :bar-beat 2, :beat-n 6, :click-at 3000}
                     {:bar-n 2, :bar-beat 3, :beat-n 7, :click-at 3500}
                     {:bar-n 2, :bar-beat 4, :beat-n 8, :click-at 4000}
                     {:bar-n 3, :bar-beat 1, :beat-n 9, :click-at 13000/3, :accentuate? true}
                     {:bar-n 3, :bar-beat 2, :beat-n 10, :click-at 14000/3}
                     {:bar-n 3, :bar-beat 3, :beat-n 11, :click-at 5000N}
                     {:bar-n 3, :bar-beat 4, :beat-n 12, :click-at 16000/3}
                     {:bar-n 3, :bar-beat 5, :beat-n 13, :click-at 17000/3}
                     {:bar-n 3, :bar-beat 6, :beat-n 14, :click-at 6000N}
                     {:bar-n 4, :bar-beat 1, :beat-n 15, :click-at 19000/3, :accentuate? true}
                     {:bar-n 4, :bar-beat 2, :beat-n 16, :click-at 20000/3}
                     {:bar-n 4, :bar-beat 3, :beat-n 17, :click-at 7000N}
                     {:bar-n 4, :bar-beat 4, :beat-n 18, :click-at 22000/3}
                     {:bar-n 4, :bar-beat 5, :beat-n 19, :click-at 23000/3}
                     {:bar-n 4, :bar-beat 6, :beat-n 20, :click-at 8000N}]
            :bar-count 4
            :beat-count 20
            :time 8000N
            :duration 8000N})))

  (testing "Handles floating point now"
    (is (= (sut/generate-clicks
            [{:bpm 60}]
            {:now 257113.7528344671
             :first-bar 1
             :first-beat 1
             :relative-subdivision 4})
           ))))

(deftest set-tempo-test
  (testing "Changes tempo of bar"
    (is (= (sut/set-tempo 40 [{:tempo 60
                               :time-signature [4 4]}])
           [{:tempo 40
             :time-signature [4 4]}])))

  (testing "Uses exact specified tempo"
    (is (= (sut/set-tempo 50 [{:tempo 60}])
           [{:tempo 50}])))

  (testing "Scales tempo linearly across bars"
    (is (= (sut/set-tempo 50 [{:tempo 60}
                              {:tempo 120}
                              {:tempo 90}])
           [{:tempo 50}
            {:tempo 100}
            {:tempo 75}])))

  (testing "Sets tempo when not specified"
    (is (= (sut/set-tempo 60 [{:time-signature [4 4]}
                              {:time-signature [6 8]}])
           [{:time-signature [4 4] :tempo 60}
            {:time-signature [6 8] :tempo 60}])))

  (testing "Defaults same tempo across all bars"
    (is (= (sut/set-tempo 80 [{:tempo 120
                               :time-signature [4 4]}
                              {:time-signature [6 8]}])
           [{:time-signature [4 4] :tempo 80}
            {:time-signature [6 8] :tempo 80}]))))
