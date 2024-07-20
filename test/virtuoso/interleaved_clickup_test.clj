(ns virtuoso.interleaved-clickup-test
  (:require [clojure.test :refer [deftest is testing]]
            [virtuoso.interleaved-clickup :as sut]))

(def tabs
  [[{:fret 14 :string :B}]
   [{:fret 12 :string :B}]
   [{:fret 14 :string :B}]

   [{:fret 12 :string :B}]
   [{:fret 10 :string :B}]
   [{:fret 12 :string :B}]

   [{:fret 10 :string :B}]
   [{:fret 8 :string :B}]
   [{:fret 10 :string :B}]

   [{:fret 9 :string :G}]
   [{:fret 8 :string :B}]
   [{:fret 9 :string :G}]

   [{:fret 9 :string :G}]
   [{:fret 7 :string :G}]])

(deftest get-next-phrase-test
  (testing "With tabs: Starts at phrase 0"
    (is (= (sut/get-next-phrase
            {::sut/phrase-size 3
             ::sut/tabs tabs})
           0)))

  (testing "With tabs: Picks the next phrase"
    (is (= (sut/get-next-phrase
            {::sut/phrase-current 2
             ::sut/phrase-size 3
             ::sut/tabs tabs})
           3)))

  (testing "With tabs: Can't pick phrases beyond the last one"
    (is (nil? (sut/get-next-phrase
               {::sut/phrase-current 4
                ::sut/phrase-size 3
                ::sut/tabs tabs}))))

  (testing "With phrase count: Starts at phrase 0"
    (is (= (sut/get-next-phrase
            {::sut/phrase-count 4}) 0)))

  (testing "With phrase count: Picks the next phrase"
    (is (= (sut/get-next-phrase
            {::sut/phrase-current 2
             ::sut/phrase-count 4}) 3)))

  (testing "With phrase count: Can't pick phrases beyond the last one"
    (is (nil? (sut/get-next-phrase
               {::sut/phrase-current 9
                ::sut/phrase-count 10})))))

(deftest get-tempo-test
  (testing "Defaults to 60bpm"
    (is (= (sut/get-tempo {}) 60)))

  (testing "Gets the default tempo"
    (is (= (sut/get-tempo {::sut/tempo-start 54}) 54)))

  (testing "Gets current tempo"
    (is (= (sut/get-tempo {::sut/tempo-current 65
                           ::sut/tempo-start 54}) 65)))

  (testing "Gets next tempo from defaults"
    (is (= (sut/increase-tempo {}) 65)))

  (testing "Gets next tempo from custom start"
    (is (= (sut/increase-tempo {::sut/tempo-start 55}) 60)))

  (testing "Gets next tempo from current tempo"
    (is (= (sut/increase-tempo {::sut/tempo-current 75}) 80)))

  (testing "Gets next tempo from current tempo with custom step"
    (is (= (sut/increase-tempo {::sut/tempo-current 74 ::sut/tempo-step 2}) 76))))

(deftest get-phrases-test
  (testing "Forward: Starts with the first phrase"
    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 0})
           [0])))

  (testing "Forward: Treats phrase-max 0 as not set"
    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 0
             ::sut/phrase-max 0})
           [0])))

  (testing "Forward: Continues with the first phrase"
    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 0
             ::sut/tempo-current 65})
           [0]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 0
             ::sut/tempo-current 115})
           [0])))

  (testing "Forward 2 phrases"
    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 1
             ::sut/tempo-current 60})
           [0 1]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 1
             ::sut/tempo-current 65})
           [1]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 1
             ::sut/tempo-current 70})
           [0 1]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 1
             ::sut/tempo-current 75})
           [1])))

  (testing "Forward 3 phrases"
    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 2
             ::sut/tempo-current 60})
           [0 1 2]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 2
             ::sut/tempo-current 65})
           [2]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 2
             ::sut/tempo-current 70})
           [1 2]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 2
             ::sut/tempo-current 75})
           [2]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 2
             ::sut/tempo-current 80})
           [0 1 2]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 2
             ::sut/tempo-current 85})
           [2])))

  (testing "Forward 4 phrases"
    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 3
             ::sut/tempo-current 60})
           [0 1 2 3]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 3
             ::sut/tempo-current 65})
           [3]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 3
             ::sut/tempo-current 70})
           [2 3]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 3
             ::sut/tempo-current 75})
           [3]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 3
             ::sut/tempo-current 80})
           [1 2 3]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 3
             ::sut/tempo-current 85})
           [3]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 3
             ::sut/tempo-current 90})
           [0 1 2 3])))

  (testing "Forward 5 phrases"
    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 4
             ::sut/tempo-current 60})
           [0 1 2 3 4]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 4
             ::sut/tempo-current 65})
           [4]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 4
             ::sut/tempo-current 70})
           [3 4]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 4
             ::sut/tempo-current 75})
           [4]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 4
             ::sut/tempo-current 80})
           [2 3 4]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 4
             ::sut/tempo-current 85})
           [4]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 4
             ::sut/tempo-current 90})
           [1 2 3 4]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 4
             ::sut/tempo-current 95})
           [4]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 4
             ::sut/tempo-current 100})
           [0 1 2 3 4])))

  (testing "Forward 4 phrases with sliding window"
    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 2
             ::sut/phrase-max 3
             ::sut/tempo-current 60})
           [0 1 2]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 3
             ::sut/phrase-max 3
             ::sut/tempo-current 60})
           [1 2 3]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 3
             ::sut/phrase-max 3
             ::sut/tempo-current 65})
           [3]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 3
             ::sut/phrase-max 3
             ::sut/tempo-current 70})
           [2 3]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 3
             ::sut/phrase-max 3
             ::sut/tempo-current 75})
           [3]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 3
             ::sut/phrase-max 3
             ::sut/tempo-current 80})
           [1 2 3]))

    (is (= (sut/get-phrases
            {::sut/start-at :start/beginning
             ::sut/phrase-current 15
             ::sut/phrase-max 3
             ::sut/tempo-current 60})
           [13 14 15]))))

(deftest select-phrases-test
  (testing "Tabs, forward: Selects given phrases"
    (is (= (sut/select-phrases {::sut/tabs tabs} [0]) [0]))
    (is (= (sut/select-phrases {::sut/tabs tabs} [0 1 2]) [0 1 2])))

  (testing "Tabs, backward: Selects given from the end"
    (is (= (sut/select-phrases
            {::sut/start-at :start/end
             ::sut/phrase-size 3
             ::sut/tabs tabs} [0]) [4]))
    (is (= (sut/select-phrases
            {::sut/start-at :start/end
             ::sut/phrase-size 3
             ::sut/tabs tabs} [0 1 2]) [2 3 4])))

  (testing "Phrase count, forward: Selects given phrases"
    (is (= (sut/select-phrases {::sut/phrase-count 4} [0]) [0]))
    (is (= (sut/select-phrases {::sut/phrase-count 4} [0 1 2]) [0 1 2])))

  (testing "Phrase count, backward: Selects given phrases"
    (is (= (sut/select-phrases {::sut/start-at :start/end ::sut/phrase-count 10} [0]) [9]))
    (is (= (sut/select-phrases {::sut/start-at :start/end ::sut/phrase-count 10} [0 1 2]) [7 8 9]))))

(deftest backwards-interleaving-test
  (testing "Progresses correctly through sequence from the end"
    (is (= (->> {::sut/phrase-current 2
                 ::sut/tempo-current 60}
                sut/get-phrases
                (sut/select-phrases
                 {::sut/tabs tabs
                  ::sut/start-at :start/end}))
           [1 2 3]))

    (is (= (->> {::sut/phrase-current 2
                 ::sut/tempo-current 65}
                sut/get-phrases
                (sut/select-phrases
                 {::sut/tabs tabs
                  ::sut/start-at :start/end}))
           [1]))

    (is (= (->> {::sut/phrase-current 2
                 ::sut/tempo-current 70}
                sut/get-phrases
                (sut/select-phrases
                 {::sut/tabs tabs
                  ::sut/start-at :start/end}))
           [1 2]))

    (is (= (->> {::sut/phrase-current 2
                 ::sut/tempo-current 75}
                sut/get-phrases
                (sut/select-phrases
                 {::sut/tabs tabs
                  ::sut/start-at :start/end}))
           [1]))

    (is (= (->> {::sut/phrase-current 2
                 ::sut/tempo-current 80}
                sut/get-phrases
                (sut/select-phrases
                 {::sut/tabs tabs
                  ::sut/start-at :start/end}))
           [1 2 3]))))
