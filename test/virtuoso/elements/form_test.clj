(ns virtuoso.elements.form-test
  (:require [clojure.test :refer [deftest is testing]]
            [virtuoso.elements.form :as sut]))

(deftest prepare-select-test
  (testing "Expects event value as number when current is number"
    (is (= (sut/prepare-select {:val 42} :val [[42 "42"]])
           {:input/kind :input.kind/select
            :on {:input [[:action/db.add {:val 42} :val :event/target-value-num]]}
            :options [{:value "42"
                       :text "42"
                       :selected? true}]})))

  (testing "Expects event value as keyword when current is keyword"
    (is (= (sut/prepare-select {} :val [[:some/kw "Some kw"]])
           {:input/kind :input.kind/select
            :on {:input [[:action/db.add {} :val :event/target-value-kw]]}
            :options [{:value "some/kw"
                       :text "Some kw"
                       :selected? true}]})))

  (testing "Expects raw event value when option is string"
    (is (= (sut/prepare-select {} :val [["1" "One"]
                                        ["2" "Two"]] "2")
           {:input/kind :input.kind/select
            :on {:input [[:action/db.add {} :val :event/target-value]]}
            :options [{:value "1"
                       :text "One"}
                      {:value "2"
                       :text "Two"
                       :selected? true}]}))))

(deftest prepare-multi-select-test
  (testing "Prepares some pills"
    (is (= (sut/prepare-multi-select {} :val [:one :two])
           {:input/kind :input.kind/pill-select
            :options [{:text ":one" :on {:click [[:action/db.add {} :val :one]]}}
                      {:text ":two" :on {:click [[:action/db.add {} :val :two]]}}]})))

  (testing "Selected value is marked as selected"
    (is (= (-> (sut/prepare-multi-select {:val #{:two}} :val [:one :two])
               :options
               second
               (select-keys [:text :selected?]))
           {:text ":two"
            :selected? true})))

  (testing "Clicking selected value removes it"
    (is (= (-> (sut/prepare-multi-select {:val #{:two}} :val [:one :two])
               :options
               second
               :on
               :click)
           [[:action/db.retract {:val #{:two}} :val :two]]))))

(deftest prepare-number-input-test
  (testing "Prepares input"
    (is (= (sut/prepare-number-input {} :num)
           {:input/kind :input.kind/number
            :on {:input [[:action/db.add {} :num :event/target-value-num]]}
            :value nil})))

  (testing "Prepares input with current value"
    (is (= (sut/prepare-number-input {:num 42} :num)
           {:input/kind :input.kind/number
            :on {:input [[:action/db.add {:num 42} :num :event/target-value-num]]}
            :value 42}))))
