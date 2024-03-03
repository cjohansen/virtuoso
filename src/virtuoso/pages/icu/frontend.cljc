(ns virtuoso.pages.icu.frontend
  (:require [phosphor.icons :as icons]
            [virtuoso.elements.form :as form]
            [virtuoso.interleaved-clickup :as icu]
            [virtuoso.ui.actions :as actions]))

(def phrase-label
  {:phrase.kind/beat "Beat"
   :phrase.kind/bar "Bar"
   :phrase.kind/line "Line"
   :phrase.kind/phrase "Phrase"})

(def phrase-kinds
  [[:phrase.kind/beat "Beats"]
   [:phrase.kind/bar "Bars"]
   [:phrase.kind/line "Lines"]
   [:phrase.kind/phrase "Phrases"]])

(def denominators
  [3 3 4 8 16 32])

(def starts
  [[:start/beginning "the top"]
   [:start/end "the bottom"]])

(defn started? [{:keys [icu]}]
  (:bpm-current icu))

(defn decrease-bpm [options]
  (when-not (:paused? options)
    (when-let [bpm (icu/decrease-bpm options)]
      [[:action/assoc-in [:icu :bpm-current] bpm]
       [:action/start-metronome bpm]])))

(defn increase-bpm [options]
  (when-not (:paused? options)
    (let [bpm (icu/increase-bpm options)]
      [[:action/assoc-in [:icu :bpm-current] bpm]
       [:action/start-metronome bpm]])))

(defn change-phrase [options next-phrase]
  (when-not (:paused? options)
    (when next-phrase
      (let [bpm (icu/get-bpm-start options)]
        [[:action/assoc-in
          [:icu :bpm-current] bpm
          [:icu :phrase-current] next-phrase]
         [:action/start-metronome bpm]]))))

(defn forward-phrase [options]
  (when-not (:paused? options)
    (->> (icu/get-next-phrase options)
         (change-phrase options))))

(defn backward-phrase [options]
  (when-not (:paused? options)
    (->> (icu/get-prev-phrase options)
         (change-phrase options))))

(defn stop [options]
  [[:action/assoc-in [:icu] (dissoc options :bpm-current :phrase-current)]
   [:action/stop-metronome]])

(defn pause []
  [[:action/assoc-in [:icu :paused?] true]
   [:action/stop-metronome]])

(defn play [options]
  (when (:paused? options)
    [[:action/assoc-in [:icu :paused?] false]
     [:action/start-metronome (icu/get-bpm options)]]))

(defmethod actions/get-keypress-actions ::tool [state e]
  (when (started? state)
    (case (:key e)
      "+" (increase-bpm (:icu state))
      "-" (decrease-bpm (:icu state))
      " " (if (:paused? (:icu state))
            (play (:icu state))
            (pause))
      "n" (forward-phrase (:icu state))
      "p" (backward-phrase (:icu state))
      nil)))

(defn prepare-icu [state]
  (let [label (phrase-label (get-in state [:icu :phrase-kind]))]
    (prn (icu/get-phrases (:icu state)))
    [{:kind :element.kind/clickup
      :text (str (get-in state [:icu :bpm-current]) " bpm")
      :bpm (get-in state [:icu :bpm-current])
      :boxes (->> (icu/get-phrases (:icu state))
                  (icu/select-phrases (:icu state))
                  (map (fn [idx]
                         {:text (str label " " idx)
                          :color-idx idx})))}
     {:kind :element.kind/button-panel
      :buttons (for [button [{:text "Lower BPM"
                              :icon (icons/icon :phosphor.bold/minus)
                              :actions (decrease-bpm (:icu state))
                              :kbd "-"}
                             {:text "Previous phrase"
                              :icon (icons/icon :phosphor.fill/skip-back)
                              :actions (backward-phrase (:icu state))
                              :kbd "p"}
                             (if (get-in state [:icu :paused?])
                               {:text "Play"
                                :icon (icons/icon :phosphor.fill/play)
                                :actions (play (:icu state))
                                :size :large
                                :kbd "space"}
                               {:text "Pause"
                                :icon (icons/icon :phosphor.fill/pause)
                                :actions (pause)
                                :size :large
                                :kbd "space"})
                             {:text "Next phrase"
                              :icon (icons/icon :phosphor.fill/skip-forward)
                              :actions (forward-phrase (:icu state))
                              :kbd "n"}
                             {:text "Bump BPM"
                              :icon (icons/icon :phosphor.bold/plus)
                              :actions (increase-bpm (:icu state))
                              :kbd "+"}]]
                 (cond-> button
                   (nil? (:actions button)) (assoc :disabled? true)))}
     {:kind :element.kind/boxed-form
      :button {:text "Start over"
               :subtle? true
               :actions (stop (:icu state))}}]))

(defn prepare-time-signature [state]
  (let [[numerator denominator] (get-in state [:icu :time-signature])]
    {:label "Time signature"
     :inputs
     [{:input/kind :input.kind/number
       :on {:input [[:action/assoc-in [:icu :time-signature] [:event/target-value-num denominator]]]}
       :value numerator}
      {:input/kind :input.kind/select
       :on {:input [[:action/assoc-in [:icu :time-signature] [numerator :event/target-value-num]]]}
       :options (for [i denominators]
                  (cond-> {:value (str i) :text (str i)}
                    (= i denominator) (assoc :selected? true)))}]}))

(defn prepare-settings [state]
  [{:kind :element.kind/boxed-form
    :button {:text "Start"
             :right-icon (icons/icon :phosphor.regular/metronome)
             :actions (let [bpm (get-in state [:icu :bpm-start])]
                        [[:action/assoc-in
                          [:icu :bpm-current] bpm
                          [:icu :phrase-current] (icu/get-next-phrase (:icu state))]
                         [:action/start-metronome bpm]])}
    :boxes
    [{:title "Exercise details"
      :fields
      [{:controls
        [{:label "Length"
          :inputs
          [(form/prepare-number-input state [:icu :phrase-count])
           (form/prepare-select state [:icu :phrase-kind] phrase-kinds)]}
         (prepare-time-signature state)]}]}

     {:title "Session settings"
      :fields [{:controls
                [{:label "Start at"
                  :inputs [(form/prepare-select state [:icu :start-at] starts)]}
                 {:label "Max phrase length"
                  :inputs [(form/prepare-number-input state [:icu :max-phrases])]}]}]}

     (let [beats (range 1 (inc (first (get-in state [:icu :time-signature]))))]
       {:title "Metronome settings"
        :fields
        [{:size :md
          :controls
          [{:label "Start tempo"
            :inputs [(form/prepare-number-input state [:icu :bpm-start])]}
           {:label "BPM step"
            :inputs [(form/prepare-number-input state [:icu :bpm-step])]}
           {:label "Drop beats (%)"
            :inputs [(form/prepare-number-input state [:icu :metronome-drop-pct])]}]}
         {:controls
          [{:label "Tick beats"
            :inputs [(form/prepare-multi-select state [:icu :tick-beats] beats)]}]}
         {:controls
          [{:label "Accentuate beats"
            :inputs [(form/prepare-multi-select state [:icu :accentuate-beats] beats)]}]}]})]}])

(defn prepare-ui-data [state]
  (if (started? state)
    (prepare-icu state)
    (prepare-settings state)))

(defn get-settings [{:keys [phrase-count phrase-kind time-signature start-at
                            max-phrases bpm-start bpm-step metronome-drop-pct
                            tick-beats accentuate-beats]}]
  (let [beats (or (get time-signature 0) 4)]
    {:phrase-count (or phrase-count 4)
     :phrase-kind (or phrase-kind :phrase.kind/bar)
     :time-signature [beats (or (get time-signature 1) 4)]
     :start-at (or start-at :start/beginning)
     :max-phrases (or max-phrases 0)
     :bpm-start (or bpm-start 60)
     :bpm-step (or bpm-step 5)
     :metronome-drop-pct (or metronome-drop-pct 0)
     :tick-beats (or tick-beats (set (range 1 (inc beats))))
     :accentuate-beats (or accentuate-beats #{1})}))

(defn get-boot-actions [state]
  (let [settings (get-settings (:icu state))]
    [[:action/assoc-in [:action/keypress-handler] ::tool]
     (when-not (= (:icu state) settings)
       [:action/assoc-in [:icu] settings])]))
