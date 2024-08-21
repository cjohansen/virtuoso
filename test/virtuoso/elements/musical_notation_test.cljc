(ns virtuoso.elements.musical-notation-test
  (:require [virtuoso.elements.musical-notation :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest beam-test
  (testing "Beams eighth notes"
    (is (= (sut/beam [:note/eighth :note/eighth])
           [:beamed/note-stem
            :beamed/eighth-beam-long-stem
            :beamed/frac-eighth-long-stem])))

  (testing "Beams dotted eighth note with eight note"
    (is (= (sut/beam [[:notation/dot :note/eighth] :note/eighth])
           [[:notation/dot :beamed/note-stem]
            :beamed/eighth-beam-long-stem
            :beamed/frac-eighth-long-stem])))

  (testing "Beams eighth note with dotted eight note"
    (is (= (sut/beam [:note/eighth [:notation/dot :note/eighth]])
           [:beamed/note-stem
            :beamed/eighth-beam-long-stem
            [:notation/dot :beamed/frac-eighth-long-stem]])))

  (testing "Beams dotted eighth notes"
    (is (= (sut/beam [[:notation/dot :note/eighth] [:notation/dot :note/eighth]])
           [[:notation/dot :beamed/note-stem]
            :beamed/eighth-beam-long-stem
            [:notation/dot :beamed/frac-eighth-long-stem]])))

  (testing "Beams eighth note triples"
    (is (= (sut/beam [:note/eighth :note/eighth :note/eighth])
           [:beamed/note-stem
            :beamed/eighth-beam-long-stem
            :beamed/frac-eighth-long-stem
            :beamed/eighth-beam-long-stem
            :beamed/frac-eighth-long-stem])))

  (testing "Beams eighth note triples, dotting the first"
    (is (= (sut/beam [[:notation/dot :note/eighth] :note/eighth :note/eighth])
           [[:notation/dot :beamed/note-stem]
            :beamed/eighth-beam-long-stem
            :beamed/frac-eighth-long-stem
            :beamed/eighth-beam-long-stem
            :beamed/frac-eighth-long-stem])))

  (testing "Beams eighth note triples, dotting the second"
    (is (= (sut/beam [:note/eighth [:notation/dot :note/eighth] :note/eighth])
           [:beamed/note-stem
            :beamed/eighth-beam-long-stem
            [:notation/dot :beamed/frac-eighth-long-stem]
            :beamed/eighth-beam-long-stem
            :beamed/frac-eighth-long-stem])))

  (testing "Beams eighth note triples, dotting the third"
    (is (= (sut/beam [:note/eighth :note/eighth [:notation/dot :note/eighth]])
           [:beamed/note-stem
            :beamed/eighth-beam-long-stem
            :beamed/frac-eighth-long-stem
            :beamed/eighth-beam-long-stem
            [:notation/dot :beamed/frac-eighth-long-stem]])))

  (testing "Beams eighth note triples, dotting the two first"
    (is (= (sut/beam [[:notation/dot :note/eighth] [:notation/dot :note/eighth] :note/eighth])
           [[:notation/dot :beamed/note-stem]
            :beamed/eighth-beam-long-stem
            [:notation/dot :beamed/frac-eighth-long-stem]
            :beamed/eighth-beam-long-stem
            :beamed/frac-eighth-long-stem])))

  (testing "Beams eighth note triples, dotting the two last"
    (is (= (sut/beam [:note/eighth [:notation/dot :note/eighth] [:notation/dot :note/eighth]])
           [:beamed/note-stem
            :beamed/eighth-beam-long-stem
            [:notation/dot :beamed/frac-eighth-long-stem]
            :beamed/eighth-beam-long-stem
            [:notation/dot :beamed/frac-eighth-long-stem]])))

  (testing "Beams eighth note triples, dotting 1 and 3"
    (is (= (sut/beam [[:notation/dot :note/eighth] :note/eighth [:notation/dot :note/eighth]])
           [[:notation/dot :beamed/note-stem]
            :beamed/eighth-beam-long-stem
            :beamed/frac-eighth-long-stem
            :beamed/eighth-beam-long-stem
            [:notation/dot :beamed/frac-eighth-long-stem]])))

  (testing "Beams eighth note triples, dotting all of them"
    (is (= (sut/beam [[:notation/dot :note/eighth] [:notation/dot :note/eighth] [:notation/dot :note/eighth]])
           [[:notation/dot :beamed/note-stem]
            :beamed/eighth-beam-long-stem
            [:notation/dot :beamed/frac-eighth-long-stem]
            :beamed/eighth-beam-long-stem
            [:notation/dot :beamed/frac-eighth-long-stem]])))

  (testing "Beams an eighth note with two sixteenths"
    (is (= (sut/beam [:note/eighth :note/sixteenth :note/sixteenth])
           [:beamed/note-stem
            :beamed/eighth-beam-long-stem
            :beamed/frac-eighth-long-stem
            :beamed/sixteenth-beam-long-stem
            :beamed/frac-sixteenth-long-stem])))

  (testing "Beams an eighth note with a dotted sixteenth"
    (is (= (sut/beam [:note/eighth [:notation/dot :note/sixteenth]])
           [:beamed/note-stem
            :beamed/eighth-beam-long-stem
            [:notation/dot :beamed/frac-sixteenth-long-stem]]))))
