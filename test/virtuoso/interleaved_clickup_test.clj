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
    (is (= (sut/get-next-phrase {:phrase-size 3 :tabs tabs})
           0)))

  (testing "With tabs: Picks the next phrase"
    (is (= (sut/get-next-phrase {:phrase-current 2
                                 :phrase-size 3
                                 :tabs tabs})
           3)))

  (testing "With tabs: Can't pick phrases beyond the last one"
    (is (nil? (sut/get-next-phrase {:phrase-current 4
                                    :phrase-size 3
                                    :tabs tabs}))))

  (testing "With phrase count: Starts at phrase 0"
    (is (= (sut/get-next-phrase {:phrase-count 4}) 0)))

  (testing "With phrase count: Picks the next phrase"
    (is (= (sut/get-next-phrase {:phrase-current 2 :phrase-count 4}) 3)))

  (testing "With phrase count: Can't pick phrases beyond the last one"
    (is (nil? (sut/get-next-phrase {:phrase-current 9 :phrase-count 10})))))

(deftest get-bpm-test
  (testing "Defaults to 60bpm"
    (is (= (sut/get-bpm {}) 60)))

  (testing "Gets the default bpm"
    (is (= (sut/get-bpm {:bpm-start 54}) 54)))

  (testing "Gets current bpm"
    (is (= (sut/get-bpm {:bpm-current 65
                         :bpm-start 54}) 65)))

  (testing "Gets next bpm from defaults"
    (is (= (sut/increase-bpm {}) 65)))

  (testing "Gets next bpm from custom start"
    (is (= (sut/increase-bpm {:bpm-start 55}) 60)))

  (testing "Gets next bpm from current bpm"
    (is (= (sut/increase-bpm {:bpm-current 75}) 80)))

  (testing "Gets next bpm from current bpm with custom step"
    (is (= (sut/increase-bpm {:bpm-current 74 :bpm-step 2}) 76))))

(deftest get-phrases-test
  (testing "Forward: Starts with the first phrase"
    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 0})
           [0])))

  (testing "Forward: Continues with the first phrase"
    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 0
             :bpm-current 65})
           [0]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 0
             :bpm-current 115})
           [0])))

  (testing "Forward 2 phrases"
    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 1
             :bpm-current 60})
           [0 1]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 1
             :bpm-current 65})
           [1]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 1
             :bpm-current 70})
           [0 1]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 1
             :bpm-current 75})
           [1])))

  (testing "Forward 3 phrases"
    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 2
             :bpm-current 60})
           [0 1 2]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 2
             :bpm-current 65})
           [2]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 2
             :bpm-current 70})
           [1 2]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 2
             :bpm-current 75})
           [2]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 2
             :bpm-current 80})
           [0 1 2]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 2
             :bpm-current 85})
           [2])))

  (testing "Forward 4 phrases"
    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 3
             :bpm-current 60})
           [0 1 2 3]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 3
             :bpm-current 65})
           [3]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 3
             :bpm-current 70})
           [2 3]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 3
             :bpm-current 75})
           [3]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 3
             :bpm-current 80})
           [1 2 3]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 3
             :bpm-current 85})
           [3]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 3
             :bpm-current 90})
           [0 1 2 3])))

  (testing "Forward 5 phrases"
    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 4
             :bpm-current 60})
           [0 1 2 3 4]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 4
             :bpm-current 65})
           [4]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 4
             :bpm-current 70})
           [3 4]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 4
             :bpm-current 75})
           [4]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 4
             :bpm-current 80})
           [2 3 4]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 4
             :bpm-current 85})
           [4]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 4
             :bpm-current 90})
           [1 2 3 4]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 4
             :bpm-current 95})
           [4]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 4
             :bpm-current 100})
           [0 1 2 3 4])))

  (testing "Forward 4 phrases with sliding window"
    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 2
             :max-phrases 3
             :bpm-current 60})
           [0 1 2]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 3
             :max-phrases 3
             :bpm-current 60})
           [1 2 3]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 3
             :max-phrases 3
             :bpm-current 65})
           [3]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 3
             :max-phrases 3
             :bpm-current 70})
           [2 3]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 3
             :max-phrases 3
             :bpm-current 75})
           [3]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 3
             :max-phrases 3
             :bpm-current 80})
           [1 2 3]))

    (is (= (sut/get-phrases
            {:start-at :start/beginning
             :phrase-current 15
             :max-phrases 3
             :bpm-current 60})
           [13 14 15]))))

(deftest select-phrases-test
  (testing "Tabs, forward: Selects given phrases"
    (is (= (sut/select-phrases {:tabs tabs} [0]) [0]))
    (is (= (sut/select-phrases {:tabs tabs} [0 1 2]) [0 1 2])))

  (testing "Tabs, backward: Selects given from the end"
    (is (= (sut/select-phrases {:start-at :start/end
                                :phrase-size 3
                                :tabs tabs} [0]) [4]))
    (is (= (sut/select-phrases {:start-at :start/end
                                :phrase-size 3
                                :tabs tabs} [0 1 2]) [2 3 4])))

  (testing "Phrase count, forward: Selects given phrases"
    (is (= (sut/select-phrases {:phrase-count 4} [0]) [0]))
    (is (= (sut/select-phrases {:phrase-count 4} [0 1 2]) [0 1 2])))

  (testing "Phrase count, backward: Selects given phrases"
    (is (= (sut/select-phrases {:start-at :start/end :phrase-count 10} [0]) [9]))
    (is (= (sut/select-phrases {:start-at :start/end :phrase-count 10} [0 1 2]) [7 8 9]))))

(deftest backwards-interleaving-test
  (testing "Progresses correctly through sequence from the end"
    (is (= (->> {:phrase-current 2
                 :bpm-current 60}
                sut/get-phrases
                (sut/select-phrases {:tabs tabs :start-at :start/end}))
           [1 2 3]))

    (is (= (->> {:phrase-current 2
                 :bpm-current 65}
                sut/get-phrases
                (sut/select-phrases {:tabs tabs :start-at :start/end}))
           [1]))

    (is (= (->> {:phrase-current 2
                 :bpm-current 70}
                sut/get-phrases
                (sut/select-phrases {:tabs tabs :start-at :start/end}))
           [1 2]))

    (is (= (->> {:phrase-current 2
                 :bpm-current 75}
                sut/get-phrases
                (sut/select-phrases {:tabs tabs :start-at :start/end}))
           [1]))

    (is (= (->> {:phrase-current 2
                 :bpm-current 80}
                sut/get-phrases
                (sut/select-phrases {:tabs tabs :start-at :start/end}))
           [1 2 3]))))
