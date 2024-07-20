(ns virtuoso.pages.interleaved-clickup
  (:require [clojure.string :as str]
            [phosphor.icons :as icons]
            [virtuoso.elements.form :as form]
            [virtuoso.elements.layout :as layout]
            [virtuoso.elements.typography :as t]
            [virtuoso.interleaved-clickup :as icu]
            [virtuoso.ui.actions :as actions]))

(defn render-page [_ctx _page]
  (layout/layout
   [:div.flex.flex-col.min-h-screen.justify-between
    (layout/header {:title "Interleaved Clicking Up"})
    [:main.grow.flex.flex-col.gap-4.replicant-root.justify-center
     {:class layout/container-classes
      :data-view "interleaved-clickup"}]
    [:footer.my-4 {:class layout/container-classes}
     [:div.px-4.md:px-0.mt-4
      (t/h2 "What's this?")
      (t/p
       "Interleaved clicking up helps you solidify and bring a piece of music up
       to speed by breaking it into chunks and clicking them up in a rotating
       pattern. You start with just a single chunk and click it up. You then add
       a unit, and will then be asked to play sections of varying lengths every
       time you increase the speed.")
      (t/p {}
       "This exercise was designed by "
       [:a.link {:href "https://www.mollygebrian.com/"} "Molly Gebrian"] ". She
      has a very good explanation and a demonstration of the process "
       [:a.link {:href "https://www.youtube.com/watch?v=it89AswI2dw"} "on YouTube"]
       ".")]]]))

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
  [2 3 4 8 16 32])

(def starts
  [[:start/beginning "the top"]
   [:start/end "the bottom"]])

(defn started? [{:keys [icu]}]
  (::icu/tempo-current icu))

(defn decrease-tempo [options]
  (when-not (:paused? options)
    (when-let [tempo (icu/decrease-tempo options)]
      [[:action/assoc-in [:icu ::icu/tempo-current] tempo]
       [:action/start-metronome tempo]])))

(defn increase-tempo [options]
  (when-not (:paused? options)
    (let [tempo (icu/increase-tempo options)]
      [[:action/assoc-in [:icu ::icu/tempo-current] tempo]
       [:action/start-metronome tempo]])))

(defn change-phrase [options next-phrase]
  (when-not (:paused? options)
    (when next-phrase
      (let [tempo (icu/get-tempo-start options)]
        [[:action/assoc-in
          [:icu ::icu/tempo-current] tempo
          [:icu ::icu/phrase-current] next-phrase]
         [:action/start-metronome tempo]]))))

(defn forward-phrase [options]
  (when-not (:paused? options)
    (->> (icu/get-next-phrase options)
         (change-phrase options))))

(defn backward-phrase [options]
  (when-not (:paused? options)
    (->> (icu/get-prev-phrase options)
         (change-phrase options))))

(defn stop [options]
  [[:action/assoc-in [:icu] (dissoc options
                                    ::icu/tempo-current
                                    ::icu/phrase-current
                                    :paused?)]
   [:action/stop-metronome]])

(defn pause []
  [[:action/assoc-in [:icu :paused?] true]
   [:action/stop-metronome]])

(defn play [options]
  (when (:paused? options)
    [[:action/assoc-in [:icu :paused?] false]
     [:action/start-metronome (icu/get-tempo options)]]))

(defmethod actions/get-keypress-actions ::tool [state data e]
  (when (started? state)
    (when e
      (.preventDefault e)
      (.stopPropagation e))
    (case (:key data)
      "+" (increase-tempo (:icu state))
      "-" (decrease-tempo (:icu state))
      " " (if (:paused? (:icu state))
            (play (:icu state))
            (pause))
      "n" (forward-phrase (:icu state))
      "p" (backward-phrase (:icu state))
      nil)))

(defn prepare-icu [state _db]
  (let [label (phrase-label (get-in state [:icu ::icu/phrase-kind]))]
    {:spacing :wide
     :sections
     [{:kind :element.kind/colored-boxes
       :footer {:text (str (get-in state [:icu ::icu/tempo-current]) " BPM")}
       :bpm (get-in state [:icu ::icu/tempo-current])
       :boxes (->> (icu/get-phrases (:icu state))
                   (icu/select-phrases (:icu state))
                   (map (fn [idx]
                          {:text (str label " " (inc idx))
                           :color-idx idx})))}
      {:kind :element.kind/button-panel
       :buttons (for [button [{:text "Lower BPM"
                               :icon (icons/icon :phosphor.bold/minus)
                               :actions (decrease-tempo (:icu state))
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
                               :actions (increase-tempo (:icu state))
                               :kbd "+"}]]
                  (cond-> button
                    (nil? (:actions button)) (assoc :disabled? true)))}
      {:kind :element.kind/footer
       :button {:text "Start over"
                :subtle? true
                :actions (stop (:icu state))}}
      {:kind :element.kind/footer
       :heading "How to use"
       :text (str "Play the indicated " (str/lower-case label) " once, then
       click the + button or the + key on your keyboard to bump the tempo.
       Repeat until you are at the goal tempo, or you can no longer keep up.
       Click the skip button or the n key on your keyboard to add
       a " (str/lower-case label) ", then repeat the process.")}]}))

(defn prepare-settings [state _db]
  {:sections
   [{:kind :element.kind/boxed-form
     :button {:text "Start"
              :right-icon (icons/icon :phosphor.regular/metronome)
              :actions (let [tempo (get-in state [:icu ::icu/tempo-start])]
                         [[:action/assoc-in
                           [:icu ::icu/tempo-current] tempo
                           [:icu ::icu/phrase-current] (icu/get-next-phrase (:icu state))]
                          [:action/start-metronome tempo]])}
     :boxes
     [{:title "Exercise details"
       :fields
       [{:controls
         [{:label "Length"
           :inputs
           [(form/prepare-number-input state [:icu ::icu/phrase-count])
            (form/prepare-select state [:icu ::icu/phrase-kind] phrase-kinds)]}]}]}

      {:title "Session settings"
       :fields [{:controls
                 [{:label "Start at"
                   :inputs [(form/prepare-select state [:icu ::icu/start-at] starts)]}
                  {:label "Max phrase length"
                   :inputs [(form/prepare-number-input state [:icu ::icu/phrase-max])]}]}]}

      (let [beats (range 1 (inc (first (get-in state [:icu :music/time-signature]))))]
        {:title "Metronome settings"
         :fields
         [{:size :md
           :controls
           [{:label "Start tempo"
             :inputs [(form/prepare-number-input state [:icu ::icu/tempo-start])]}
            {:label "BPM step"
             :inputs [(form/prepare-number-input state [:icu ::icu/tempo-step])]}]}]})]}]})

(defn prepare-ui-data [state db]
  (if (started? state)
    (prepare-icu state db)
    (prepare-settings state db)))

(defn get-settings [{::icu/keys [phrase-max phrase-count phrase-kind
                                 start-at tempo-start tempo-step]
                     :metronome/keys [tick-beats accentuate-beats drop-pct]
                     :as settings}]
  (let [beats (or (get (:music/time-signature settings) 0) 4)]
    {::icu/phrase-count (or phrase-count 4)
     ::icu/phrase-kind (or phrase-kind :phrase.kind/bar)
     ::icu/phrase-max (or phrase-max 0)
     ::icu/start-at (or start-at :start/beginning)
     ::icu/tempo-start (or tempo-start 60)
     ::icu/tempo-step (or tempo-step 5)
     :music/time-signature [beats (or (get (:music/time-signature settings) 1) 4)]
     :metronome/drop-pct (or drop-pct 0)
     :metronome/tick-beats (or tick-beats (set (range 1 (inc beats))))
     :metronome/accentuate-beats (or accentuate-beats settings #{1})}))

(defn get-boot-actions [state _db]
  (let [settings (get-settings (:icu state))]
    (into [[:action/assoc-in [:action/keypress-handler] ::tool]

           (when (and (not (started? state))
                      (not= (:icu state) settings))
             [:action/assoc-in [:icu] settings])]
          (keep
           (fn [[k v]]
             (when (nil? (get-in state [:icu k]))
               [:action/assoc-in [:icu k] v]))
           settings))))