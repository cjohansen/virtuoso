(ns virtuoso.pages.interleaved-clickup-test
  (:require [clojure.test :refer [deftest is testing]]
            [datascript.core :as d]
            [virtuoso.interleaved-clickup :as icu]
            [virtuoso.pages.interleaved-clickup :as sut]
            [virtuoso.test-helper :as helper]))

(deftest prepare-settings-test
  (testing "Shows setting when not started"
    (is (= (-> (helper/with-db [db sut/get-boot-actions]
                 (sut/prepare-ui-data db))
               :sections
               first
               :kind)
           :element.kind/boxed-form)))

  (testing "Starts exercise with the specified tempo"
    (is (= (->> (helper/with-conn [conn]
                  (helper/execute-actions conn (sut/get-boot-actions (d/db conn)))
                  (helper/execute-actions conn
                    [[:action/db.add (sut/get-activity (d/db conn)) ::icu/tempo-start 75]])
                  (sut/prepare-ui-data (d/db conn)))
                :sections
                first
                :button
                :actions
                (filter (comp #{:action/start-metronome} first))
                first
                (helper/strip-keys-by-ns #{"db" "virtuoso.interleaved-clickup"}))
           [:action/start-metronome
            [{:metronome/accentuate-beats #{1}
              :metronome/drop-pct 0
              :metronome/click-beats #{1 4 3 2}
              :music/time-signature [4 4]}]
            75]))))
