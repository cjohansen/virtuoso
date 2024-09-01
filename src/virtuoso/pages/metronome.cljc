(ns virtuoso.pages.metronome
  (:require [datascript.core :as d]
            [phosphor.icons :as icons]
            [virtuoso.elements.layout :as layout]
            [virtuoso.elements.modal :as modal]
            [virtuoso.elements.typography :as t]
            [virtuoso.metronome :as metronome]))

(defn render-page [_ctx _page]
  (layout/layout
   [:div.flex.flex-col.min-h-screen.justify-between
    (layout/header {:title "Metronome"})
    [:main.grow.flex.flex-col.gap-4.justify-center
     {:class layout/container-classes
      :data-replicant-view "metronome"}]
    [:footer.my-4 {:class layout/container-classes}
     [:div.px-4.md:px-0.mt-4
      (t/h2 "Help")
      (t/p
       "To change time signature, click the bar indicating the time signature,
       and make your changes. To accentuate certain beats, click the
       corresponding dot under the bar. Clicking it again will disable clicking
       at this beat.")
      (t/p
       "The metronome supports both tempo and time signature changes. To use
       them, click the notes with a plus next to the bar to add another bar.
       Bars can have varying tempos, time signatures, and may repeat. The tempo
       will be recalculated appropriately when stepping the metronome tempo up
       and down.")]]]))

(def schema
  {:music/tempo {} ;; number, bpm
   :music/time-signature {} ;; tuple of numbers [4 4]

   ;; set of numbers #{1}
   :metronome/accentuate-beats {:db/cardinality :db.cardinality/many}
   ;; set of numbers #{1 2 3 4}
   :metronome/click-beats {:db/cardinality :db.cardinality/many}
   ;; number, percentage of beats to randomly drop
   :metronome/drop-pct {}
   ;; bars
   :music/bars {:db/cardinality :db.cardinality/many
                :db/type :db.type/ref}})

(defn get-step-size [activity]
  (or (:metronome/tempo-step-size activity) 5))

(defn start-metronome [activity & [tempo]]
  [:action/start-metronome
   (update (into {} activity) :music/tempo #(or tempo %))
   {:on-click [[:action/transact
                [{:db/id (:db/id activity)
                  :metronome/current-bar [:metronome/click :bar/idx]
                  :metronome/current-beat [:metronome/click :bar/beat]}]]]}])

(defn stop-metronome [activity]
  (when-not (:activity/paused? activity)
    [[:action/db.add activity :activity/paused? true]
     [:action/stop-metronome]]))

(defn adjust-tempo [activity tempo-change]
  (let [target-tempo (+ (:music/tempo activity) tempo-change)]
    (cond-> [[:action/db.add activity :music/tempo target-tempo]]
      (not (:activity/paused? activity))
      (conj (start-metronome activity target-tempo)))))

(defn get-activity [db]
  (:view/tool (d/entity db :virtuoso/current-view)))

(defn alias-ks [m alias->k]
  (loop [m m
         alias->k (seq alias->k)]
    (if (nil? alias->k)
      m
      (let [[alias k] (first alias->k)]
        (recur (assoc m alias (get m k)) (next alias->k))))))

(defn get-button-actions [activity]
  (let [step-size (get-step-size activity)]
    (alias-ks
     {"p" (adjust-tempo activity (- step-size))
      "-" (adjust-tempo activity (- 1))
      "space" (if (:activity/paused? activity)
                [[:action/db.retract activity :activity/paused?]
                 (start-metronome activity)]
                (stop-metronome activity))
      "+" (adjust-tempo activity 1)
      "n" (adjust-tempo activity step-size)}
     {" " "space"
      "ArrowUp" "+"
      "ArrowDown" "-"
      "ArrowRight" "n"
      "ArrowLeft" "p"})))

(defn get-keypress-actions [db data]
  (-> (get-activity db)
      get-button-actions
      (get (:key data))))

(defn prepare-button-panel [activity]
  (let [actions (get-button-actions activity)
        step-size (get-step-size activity)]
    {:kind :element.kind/button-panel
     :buttons
     (for [button [{:text (str "Lower tempo by " step-size " bpm")
                    :icon (icons/icon :phosphor.bold/minus)
                    :icon-size :tiny
                    :icon-after-label (str step-size)
                    :kbd "p"}
                   {:text "Lower tempo"
                    :icon (icons/icon :phosphor.bold/minus)
                    :kbd "-"}
                   (if (:activity/paused? activity)
                     {:text "Play"
                      :icon (icons/icon :phosphor.fill/play)
                      :size :large
                      :kbd "space"}
                     {:text "Pause"
                      :icon (icons/icon :phosphor.fill/pause)
                      :size :large
                      :kbd "space"})
                   {:text "Bump tempo"
                    :icon (icons/icon :phosphor.bold/plus)
                    :kbd "+"}
                   {:text (str "Bump tempo by " step-size " bpm")
                    :icon (icons/icon :phosphor.bold/plus)
                    :icon-size :tiny
                    :icon-after-label (str step-size)
                    :kbd "n"}]]
       (assoc button :actions (get actions (:kbd button))))}))

(defn prepare-badge [activity]
  {:kind :element.kind/round-badge
   :text (str (:music/tempo activity))
   :label "BPM"
   :theme (if (:activity/paused? activity) :neutral :success)})

(defn prepare-dots [activity bar & [{:keys [active?]}]]
  (let [beat-xs (range 1 (inc (first (:music/time-signature bar))))
        click-beat? (set (or (:metronome/click-beats bar) beat-xs))
        base-actions (stop-metronome activity)]
    (for [[n beat] (map-indexed vector beat-xs)]
      (cond
        (not (click-beat? beat))
        (cond-> {:disabled? true
                 :actions (conj base-actions [:action/db.add (:db/id bar) :metronome/click-beats beat])}
          (and active? (= (:metronome/current-beat activity) (inc n)))
          (assoc :current? true))

        (contains? (:metronome/accentuate-beats bar) beat)
        {:highlight? true
         :actions (into base-actions
                        [[:action/db.retract (:db/id bar) :metronome/accentuate-beats beat]
                         (if (:metronome/click-beats bar)
                           [:action/db.retract (:db/id bar) :metronome/click-beats beat]
                           [:action/transact [{:db/id (:db/id bar) :metronome/click-beats (set (remove #{beat} beat-xs))}]])])}

        :else
        (cond-> {:actions (conj base-actions [:action/db.add (:db/id bar) :metronome/accentuate-beats beat])}
          (and active? (= (:metronome/current-beat activity) (inc n)))
          (assoc :current? true))))))

(def whole-note (/ 1 1))
(def half-note (/ 1 2))
(def quarter-note (/ 1 4))
(def eighth-note (/ 1 8))
(def eighth-note-triplet (* (/ 2 3) (/ 1 8)))
(def dotted-eighth-note (* (/ 3 2) (/ 1 8)))
(def sixteenth-note (/ 1 16))
(def sixteenth-note-triplet (* (/ 2 3) (/ 1 16)))
(def dotted-sixteenth-note (* (/ 3 2) (/ 1 16)))

(def note-val->sym
  {whole-note :note/whole
   half-note :note/half
   quarter-note :note/quarter
   eighth-note :note/eighth
   eighth-note-triplet :note/eighth
   sixteenth-note :note/sixteenth
   sixteenth-note-triplet :note/sixteenth})

(defn symbolize-note-val [nv]
  (or (note-val->sym nv)
      (some->> (* nv (/ 2 3))
               note-val->sym
               (conj [:notation/dot]))))

(defn collect-for [nvs duration]
  (loop [elapsed 0
         nvs (seq nvs)
         res []]
    (if (nil? nvs)
      [nil (if (= 1 (count res))
             (first res)
             res)]
      (let [nv (first nvs)
            new-elapsed (+ elapsed nv)]
        (if (<= new-elapsed duration)
          (recur new-elapsed (next nvs) (conj res nv))
          [nvs (if (= 1 (count res))
                 (first res)
                 res)])))))

(defn symbolize-rhythm [rhythm]
  (loop [nvs (seq rhythm)
         res []]
    (if nvs
      (let [nv (first nvs)
            [next-nvs group] (if (#{eighth-note
                                    dotted-eighth-note
                                    eighth-note-triplet
                                    sixteenth-note
                                    dotted-sixteenth-note
                                    sixteenth-note-triplet} nv)
                               (collect-for nvs quarter-note)
                               [(next nvs) nv])]
        (->> (if (coll? group)
               (->> group
                    (map symbolize-note-val)
                    (into [:notation/beam]))
               (symbolize-note-val group))
             (conj res)
             (recur next-nvs)))
      res)))

(defn prepare-bar [db activity bar paced-bar bar-n]
  (let [[beats subdivision] (:music/time-signature bar)]
    (cond-> {:replicant/key [:bar (:db/id bar)]
             :beats {:val beats}
             :subdivision {:val subdivision}
             :dots (prepare-dots activity bar {:active? (and (not (:activity/paused? activity))
                                                             (= (:metronome/current-bar activity) bar-n))})}
      (:bar/rhythm bar)
      (assoc :rhythm {:pattern (symbolize-rhythm (:bar/rhythm bar))})

      db
      (assoc :actions (concat (modal/get-open-modal-actions db ::edit-bar-modal {:idx (:ordered/idx bar)})
                              (stop-metronome activity)))

      (< 1 (or (:metronome/reps bar) 1))
      (assoc :reps {:val (:metronome/reps bar)
                    :unit "times"})

      (< 1 (count (:music/bars activity)))
      (assoc :buttons [{:text "Remove bar"
                        :icon (icons/icon :phosphor.regular/minus-circle)
                        :theme :warn
                        :actions (conj (stop-metronome activity)
                                       [:action/transact [[:db/retractEntity (:db/id bar)]]])}])

      (:music/tempo bar)
      (assoc :tempo {:val (Math/round (float (:music/tempo paced-bar)))
                     :unit "BPM"}))))

(defn prepare-bars [db activity]
  (let [paced-bars (metronome/set-tempo (:music/tempo activity) (map #(into {} %) (:music/bars activity)))]
    {:kind :element.kind/bars
     :bars (map #(prepare-bar db activity %1 %2 (inc %3)) (:music/bars activity) paced-bars (range))
     :buttons [{:text "Add bar"
                :icon (icons/icon :phosphor.regular/music-notes-plus)
                :icon-size :large
                :actions (let [idx (inc (apply max 0 (keep :ordered/idx (:music/bars activity))))]
                           (cond-> []
                             (not (:activity/paused? activity))
                             (into (stop-metronome activity))

                             :then
                             (conj [:action/transact
                                    [{:db/id (:db/id activity)
                                      :music/bars
                                      [{:ordered/idx idx
                                        :bar/rhythm [(/ 1 4)]
                                        :music/time-signature [4 4]}]}]])

                             db
                             (into (modal/get-open-modal-actions db ::edit-new-bar-modal {:idx idx}))

                             db
                             (into (stop-metronome activity))))}]}))

(defn prepare-form [activity]
  {:kind :element.kind/boxed-form
   :boxes
   (let [stop-actions (stop-metronome activity)]
     [{:title "Settings"
       :fields
       [{:controls
         [{:label "Drop % of beats"
           :inputs
           [{:input/kind :input.kind/number
             :on
             {:input
              (cond-> [[:action/db.add activity :metronome/drop-pct :event/target-value-num]]
                stop-actions (into stop-actions))}
             :value (:metronome/drop-pct activity)}]}
          {:label "Skip interval"
           :inputs
           [{:input/kind :input.kind/number
             :on
             {:input
              [[:action/db.add
                activity
                :metronome/tempo-step-size
                :event/target-value-num]]}
             :value (get-step-size activity)}]}]}]}])})

(defn prepare-metronome [db activity]
  {:sections
   [(prepare-badge activity)
    (prepare-bars db activity)
    (prepare-button-panel activity)
    (prepare-form activity)]})

(defn prepare-ui-data [db]
  (prepare-metronome db (get-activity db)))

(def subdivisions
  [4 8 16 32 64])

(defn get-prev [xs x]
  (loop [xs (seq xs)
         val nil]
    (when xs
      (let [candidate (first xs)]
        (if (= candidate x)
          val
          (recur (next xs) candidate))))))

(defn get-next [xs x]
  (loop [xs (seq xs)]
    (when xs
      (let [rest (next xs)]
        (if (= (first xs) x)
          (first rest)
          (recur rest))))))

(defn prepare-reps-edit [bar]
  (let [reps (or (:metronome/reps bar) 1)]
    {:val reps
     :unit (if (= 1 reps) "time" "times")
     :button-above (cond-> {:icon (icons/icon :phosphor.regular/minus-circle)}
                     (< 1 reps) (assoc :actions [[:action/db.add bar :metronome/reps (dec reps)]]))
     :button-below {:icon (icons/icon :phosphor.regular/plus-circle)
                    :actions [[:action/db.add bar :metronome/reps (inc reps)]]}}))

(defn prepare-tempo-edit [activity bar]
  (cond-> {:val (or (:music/tempo bar) (:music/tempo activity))
           :unit "BPM"
           :actions (concat (for [bar (->> (:music/bars activity)
                                           (remove :music/tempo)
                                           (remove (comp #{(:db/id bar)} :db/id)))]
                              [:action/db.add bar :music/tempo (:music/tempo activity)])
                            [[:action/db.add bar :music/tempo :event/target-value-num]])}
    (not (:music/tempo bar)) (assoc :subtle? true)))

(def subdivision-rhythms
  {4 [[quarter-note]
      [eighth-note eighth-note]
      [dotted-eighth-note sixteenth-note]
      (repeat 3 eighth-note-triplet)
      (repeat 4 sixteenth-note)
      (repeat 6 sixteenth-note-triplet)]
   8 [[quarter-note]
      [eighth-note]
      [sixteenth-note sixteenth-note]
      (repeat 3 sixteenth-note-triplet)]
   :default [[quarter-note]
             [eighth-note]
             [sixteenth-note]]})

(defn prepare-bar-edit-modal [activity bar]
  (let [[beats subdivision] (:music/time-signature bar)
        multi-bar? (< 1 (count (:music/bars activity)))]
    {:title "Configure bar"
     :classes ["max-w-64"]
     :sections
     [{:kind :element.kind/bars
       :bars
       [(cond-> {:beats {:val beats
                         :left-button (cond-> {:icon (icons/icon :phosphor.regular/minus-circle)}
                                        (< 1 beats)
                                        (assoc :actions [[:action/db.add bar :music/time-signature [(dec beats) subdivision]]]))
                         :right-button {:icon (icons/icon :phosphor.regular/plus-circle)
                                        :actions [[:action/db.add bar :music/time-signature [(inc beats) subdivision]]]}}
                 :subdivision {:val subdivision
                               :left-button
                               (let [prev-s (get-prev subdivisions subdivision)]
                                 (cond-> {:icon (icons/icon :phosphor.regular/minus-circle)}
                                   prev-s
                                   (assoc :actions [[:action/db.add bar :music/time-signature [beats prev-s]]])))

                               :right-button
                               (let [next-s (get-next subdivisions subdivision)]
                                 (cond-> {:icon (icons/icon :phosphor.regular/plus-circle)}
                                   next-s
                                   (assoc :actions [[:action/db.add bar :music/time-signature [beats next-s]]])))}
                 :dots (prepare-dots activity bar)
                 :size :large}
          multi-bar? (assoc :reps (prepare-reps-edit bar))
          multi-bar? (assoc :tempo (prepare-tempo-edit activity bar)))]}
      {:kind :element.kind/musical-notation-selection
       :items (for [rhythm (or (subdivision-rhythms subdivision)
                               (subdivision-rhythms :default))]
                (let [active? (= rhythm (:bar/rhythm bar))]
                  (cond-> {:notation (symbolize-rhythm rhythm)}
                    active? (assoc :active? true)
                    (not active?) (assoc :actions [[:action/db.add bar :bar/rhythm rhythm]]))))}]}))

(defn prepare-new-bar-modal [activity modal]
  (let [n (.indexOf (map :ordered/idx (:music/bars activity))
                    (-> modal :modal/params :idx))
        [template bar] (drop (dec n) (:music/bars activity))]
    (->> (merge
          (select-keys template [:music/time-signature :bar/rhythm])
          bar
          {:db/id (:db/id bar)})
         (prepare-bar-edit-modal activity))))

(defn prepare-existing-bar-edit-modal [activity modal]
  (->> (:music/bars activity)
       (filter (comp #{(-> modal :modal/params :idx)} :ordered/idx))
       first
       (prepare-bar-edit-modal activity)))

(defn prepare-modal-data [db modal]
  (case (:modal/kind modal)
    ::edit-new-bar-modal (prepare-new-bar-modal (get-activity db) modal)
    ::edit-bar-modal (prepare-existing-bar-edit-modal (get-activity db) modal)))

(defn get-settings [settings]
  {:activity/paused? true
   :music/tempo (or (:music/tempo settings) 60)
   :metronome/drop-pct (or (:metronome/drop-pct settings) 0)
   :metronome/tempo-step-size (or (:metronome/tempo-step-size settings) 5)
   :music/bars (or (:music/bars settings)
                   [{:ordered/idx 0
                     :music/time-signature [4 4]
                     :bar/rhythm [(/ 1 4)]}])})

(defn get-boot-actions [db]
  [[:action/transact
    [{:db/ident :virtuoso/current-view
      :action/keypress-handler ::tool
      :view/tool (into {:db/ident ::tool}
                       (get-settings (d/entity db ::tool)))}]]])
