(ns virtuoso.pages.interleaved-clickup
  (:require [clojure.string :as str]
            [datascript.core :as d]
            [phosphor.icons :as icons]
            [virtuoso.elements.form :as form]
            [virtuoso.elements.layout :as layout]
            [virtuoso.elements.typography :as t]
            [virtuoso.interleaved-clickup :as icu]))

(defn render-page [_ctx _page]
  (layout/layout
   [:div.flex.flex-col.min-h-screen.justify-between
    (layout/header {:title "Interleaved Clicking Up"})
    [:main.grow.flex.flex-col.gap-4.justify-center
     {:class layout/container-classes
      :data-replicant-view "interleaved-clickup"}]
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

(def schema
  {::icu/tempo-start {} ;; number
   ::icu/tempo-step {} ;; number
   ::icu/tempo-current {} ;; number
   ::icu/phrase-max {} ;; number
   ::icu/phrase-count {} ;; number
   ::icu/phrase-size {} ;; number
   ::icu/phrase-kind {} ;; keyword
   ::icu/start-at {} ;; keyword
   })

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

(defn started? [activity]
  (::icu/tempo-current activity))

(defn decrease-tempo [activity]
  (when-not (:activity/paused? activity)
    (when-let [tempo (icu/decrease-tempo activity)]
      [[:action/db.add activity ::icu/tempo-current tempo]
       [:action/start-metronome {:music/bars [activity]
                                 :music/tempo tempo}]])))

(defn increase-tempo [activity]
  (when-not (:activity/paused? activity)
    (let [tempo (icu/increase-tempo activity)]
      [[:action/db.add activity ::icu/tempo-current tempo]
       [:action/start-metronome {:music/bars [activity]
                                 :music/tempo tempo}]])))

(defn change-phrase [activity next-phrase]
  (when-not (:activity/paused? activity)
    (when next-phrase
      (let [tempo (icu/get-tempo-start activity)]
        [[:action/transact
          [[:db/add (:db/id activity) ::icu/tempo-current tempo]
           [:db/add (:db/id activity) ::icu/phrase-current next-phrase]]]
         [:action/start-metronome {:music/bars [activity]
                                   :music/tempo tempo}]]))))

(defn forward-phrase [activity]
  (when-not (:activity/paused? activity)
    (->> (icu/get-next-phrase activity)
         (change-phrase activity))))

(defn backward-phrase [activity]
  (when-not (:activity/paused? activity)
    (->> (icu/get-prev-phrase activity)
         (change-phrase activity))))

(defn stop [activity]
  [[:action/transact
    [[:db/retract (:db/id activity) ::icu/tempo-current]
     [:db/retract (:db/id activity) ::icu/phrase-current]
     [:db/retract (:db/id activity) :activity/paused?]]]
   [:action/stop-metronome]])

(defn pause [activity]
  [[:action/db.add activity :activity/paused? true]
   [:action/stop-metronome]])

(defn play [activity]
  (when (:activity/paused? activity)
    [[:action/db.add activity :activity/paused? false]
     [:action/start-metronome {:music/bars [activity]
                               :music/tempo (icu/get-tempo activity)}]]))

(defn get-activity [db]
  (:view/tool (d/entity db :virtuoso/current-view)))

(defn get-keypress-actions [db data]
  (let [activity (get-activity db)]
    (when (started? activity)
      (case (:key data)
        "+" (increase-tempo activity)
        "-" (decrease-tempo activity)
        " " (if (:activity/paused? activity)
              (play activity)
              (pause activity))
        "n" (forward-phrase activity)
        "p" (backward-phrase activity)
        nil))))

(defn prepare-icu [activity]
  (let [label (phrase-label (::icu/phrase-kind activity))]
    {:spacing :wide
     :sections
     [{:kind :element.kind/colored-boxes
       :footer {:text (str (::icu/tempo-current activity) " BPM")}
       :bpm (::icu/tempo-current activity)
       :boxes (->> (icu/get-phrases activity)
                   (icu/select-phrases activity)
                   (map (fn [idx]
                          {:text (str label " " (inc idx))
                           :color-idx idx})))}
      {:kind :element.kind/button-panel
       :buttons (for [button [{:text "Lower BPM"
                               :icon (icons/icon :phosphor.bold/minus)
                               :actions (decrease-tempo activity)
                               :kbd "-"}
                              {:text "Previous phrase"
                               :icon (icons/icon :phosphor.fill/skip-back)
                               :actions (backward-phrase activity)
                               :kbd "p"}
                              (if (:activity/paused? activity)
                                {:text "Play"
                                 :icon (icons/icon :phosphor.fill/play)
                                 :actions (play activity)
                                 :size :large
                                 :kbd "space"}
                                {:text "Pause"
                                 :icon (icons/icon :phosphor.fill/pause)
                                 :actions (pause activity)
                                 :size :large
                                 :kbd "space"})
                              {:text "Next phrase"
                               :icon (icons/icon :phosphor.fill/skip-forward)
                               :actions (forward-phrase activity)
                               :kbd "n"}
                              {:text "Bump BPM"
                               :icon (icons/icon :phosphor.bold/plus)
                               :actions (increase-tempo activity)
                               :kbd "+"}]]
                  (cond-> button
                    (nil? (:actions button)) (assoc :disabled? true)))}
      {:kind :element.kind/footer
       :button {:text "Start over"
                :subtle? true
                :actions (stop activity)}}
      {:kind :element.kind/footer
       :heading "How to use"
       :text (str "Play the indicated " (str/lower-case label) " once, then
       click the + button or the + key on your keyboard to bump the tempo.
       Repeat until you are at the goal tempo, or you can no longer keep up.
       Click the skip button or the n key on your keyboard to add
       a " (str/lower-case label) ", then repeat the process.")}]}))

#_(defn prepare-time-signature [activity]
  (let [[numerator denominator] (:music/time-signature activity)]
    {:label "Time signature"
     :inputs
     [{:input/kind :input.kind/number
       :on {:input [[:action/db.add activity :music/time-signature [:event/target-value-num denominator]]]}
       :value numerator}
      {:input/kind :input.kind/select
       :on {:input [[:action/db.add activity :music/time-signature [numerator :event/target-value-num]]]}
       :options (for [i denominators]
                  (cond-> {:value (str i) :text (str i)}
                    (= i denominator) (assoc :selected? true)))}]}))

(defn prepare-settings [activity]
  {:sections
   [{:kind :element.kind/boxed-form
     :button {:text "Start"
              :right-icon (icons/icon :phosphor.regular/metronome)
              :actions (let [tempo (::icu/tempo-start activity)]
                         [[:action/transact
                           [{:db/id (:db/id activity)
                             ::icu/tempo-current tempo
                             ::icu/phrase-current (icu/get-next-phrase activity)}]]
                          [:action/start-metronome {:music/bars [activity]
                                                    :music/tempo tempo}]])}
     :boxes
     [{:title "Exercise details"
       :fields
       [{:controls
         [{:label "Length"
           :inputs
           [(form/prepare-number-input activity ::icu/phrase-count)
            (form/prepare-select activity ::icu/phrase-kind phrase-kinds)]}
          #_(prepare-time-signature activity)]}]}

      {:title "Session settings"
       :fields [{:controls
                 [{:label "Start at"
                   :inputs [(form/prepare-select activity ::icu/start-at starts)]}
                  {:label "Max phrase length"
                   :inputs [(form/prepare-number-input activity ::icu/phrase-max)]}]}]}

      (let [beats (range 1 (inc (first (:music/time-signature activity))))]
        {:title "Metronome settings"
         :fields
         [{:size :md
           :controls
           [{:label "Start tempo"
             :inputs [(form/prepare-number-input activity ::icu/tempo-start)]}
            {:label "BPM step"
             :inputs [(form/prepare-number-input activity ::icu/tempo-step)]}
            {:label "Drop beats (%)"
             :inputs [(form/prepare-number-input activity :metronome/drop-pct)]}]}
          {:controls
           [{:label "Click beats"
             :inputs [(form/prepare-multi-select activity :metronome/click-beats beats)]}]}
          {:controls
           [{:label "Accentuate beats"
             :inputs [(form/prepare-multi-select activity :metronome/accentuate-beats beats)]}]}]})]}]})

(defn prepare-ui-data [db]
  (let [activity (get-activity db)]
    (if (started? activity)
      (prepare-icu activity)
      (prepare-settings activity))))

(defn get-settings [{::icu/keys [phrase-max phrase-count phrase-kind
                                 start-at tempo-start tempo-step]
                     :metronome/keys [click-beats accentuate-beats drop-pct]
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
     :metronome/click-beats (or click-beats (set (range 1 (inc beats))))
     :metronome/accentuate-beats (or accentuate-beats settings #{1})}))

(defn get-boot-actions [db]
  [[:action/transact
    [{:db/ident :virtuoso/current-view
      :action/keypress-handler ::tool
      :view/tool (into {:db/ident ::tool}
                       (get-settings (d/entity db ::tool)))}]]])
