(ns virtuoso.pages.icu.frontend
  (:require [virtuoso.elements.form :as form]))

(defn started? [icu]
  (:bpm-current icu))

(defn prepare-icu [icu])

(def chunk-kinds
  [[:chunk/beat "Beats"]
   [:chunk/bar "Bars"]
   [:chunk/line "Lines"]
   [:chunk/phrase "Phrases"]])

(def denominators
  [2 3 4 8 16 32])

(def starts
  [[:start/beginning "the top"]
   [:start/end "the bottom"]])

(defn prepare-exercise-length [state]
  {:label "Length"
   :inputs
   [(form/prepare-number-input state [:icu :chunk-count] 4)
    (form/prepare-select state [:icu :chunk-kind] chunk-kinds :chunk/bar)]})

(defn get-time-signature [state]
  [(or (get-in state [:icu :time-signature 0]) 4)
   (or (get-in state [:icu :time-signature 1]) 4)])

(defn prepare-time-signature [state]
  (let [time-sig (get-time-signature state)]
    {:label "Time signature"
     :inputs
     [{:input/kind :input.kind/number
       :on {:input [[:action/assoc-in [:icu :time-signature] [:event/target-value-num (second time-sig)]]]}
       :value (first time-sig)}
      {:input/kind :input.kind/select
       :on {:input [[:action/assoc-in [:icu :time-signature] [(first time-sig) :event/target-value-num]]]}
       :options (for [i denominators]
                  (cond-> {:value (str i) :text (str i)}
                    (= i (second time-sig)) (assoc :selected? true)))}]}))

(defn prepare-settings [state]
  [{:kind :element.kind/boxed-form
    :boxes
    [{:title "Exercise details"
      :fields
      [{:controls
        [(prepare-exercise-length state)
         (prepare-time-signature state)]}]}

     {:title "Session settings"
      :fields [{:controls
                [{:label "Start at"
                  :inputs [(form/prepare-select state [:icu :start-at] starts)]}
                 {:label "Max phrase length"
                  :inputs [(form/prepare-number-input state [:icu :max-phrase-n] 0)]}]}]}

     (let [time-sig (get-time-signature state)
           beats (range 1 (inc (first time-sig)))]
       {:title "Metronome settings"
        :fields
        [{:controls
          [{:label "Start tempo"
            :inputs [(form/prepare-number-input state [:icu :bpm-start] 60)]}
           {:label "BPM step"
            :inputs [(form/prepare-number-input state [:icu :bpm-step] 5)]}
           {:label "Drop beats (%)"
            :inputs [(form/prepare-number-input state [:icu :metronome-drop-pct] 0)]}]}
         {:controls
          [{:label "Tick beats"
            :inputs [(form/prepare-multi-select state [:icu :tick-beats] beats beats)]}]}
         {:controls
          [{:label "Accentuate beats"
            :inputs [(form/prepare-multi-select state [:icu :accentuate-beats] beats #{1})]}]}]})]}])

(defn prepare-ui-data [state]
  (if (started? (:icu state))
    (prepare-icu (:icu state))
    (prepare-settings state)))
