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

(defn get-settings [{:keys [phrase-count phrase-kind time-signature start-at
                            max-phrase-n bpm-start bpm-step metronome-drop-pct
                            tick-beats accentuate-beats]}]
  (let [beats (or (get time-signature 0) 4)]
    {:phrase-count (or phrase-count 4)
     :phrase-kind (or phrase-kind :phrase.kind/bar)
     :time-signature [beats (or (get time-signature 1) 4)]
     :start-at (or start-at 0)
     :max-phrase-n (or max-phrase-n 0)
     :bpm-start (or bpm-start 60)
     :bpm-step (or bpm-step 5)
     :metronome-drop-pct (or metronome-drop-pct 0)
     :tick-beats (or tick-beats (set (range 1 (inc beats))))
     :accentuate-beats (or accentuate-beats #{1})}))

(defn get-boot-actions [state]
  (let [settings (get-settings (:icu state))]
    (when-not (= (:icu state) settings)
      [[:action/assoc-in [:icu] settings]])))
