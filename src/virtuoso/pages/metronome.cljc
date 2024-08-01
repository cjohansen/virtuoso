(ns virtuoso.pages.metronome
  (:require [datascript.core :as d]
            [phosphor.icons :as icons]
            [virtuoso.metronome :as metronome]
            [virtuoso.ui.actions :as actions]))

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
   :metronome/bars {:db/cardinality :db.cardinality/many
                    :db/type :db.type/ref}})

(defn get-step-size [activity]
  (or (:metronome/tempo-step-size activity) 5))

(defn start-metronome [activity & [tempo]]
  [:action/start-metronome (:metronome/bars activity) (or tempo (:music/tempo activity))])

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

(defn get-button-actions [activity]
  (let [step-size (get-step-size activity)
        space-actions (if (:activity/paused? activity)
                        [[:action/db.retract activity :activity/paused?]
                         (start-metronome activity)]
                        (stop-metronome activity))]
    {"p" (adjust-tempo activity (- step-size))
     "-" (adjust-tempo activity (- 1))
     "space" space-actions
     " " space-actions
     "+" (adjust-tempo activity 1)
     "n" (adjust-tempo activity step-size)}))

(defmethod actions/get-keypress-actions ::tool [db data e]
  (let [activity (get-activity db)]
    (when e
      (.preventDefault e)
      (.stopPropagation e))
    (get (get-button-actions activity) (:key data))))

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

(defn prepare-bar [activity bar paced-bar]
  (let [[beats subdivision] (:music/time-signature bar)
        beat-xs (range 1 (inc beats))
        click-beat? (set (or (:metronome/click-beats bar) beat-xs))
        base-actions (stop-metronome activity)]
    (cond-> {:beats {:val beats}
             :subdivision {:val subdivision}
             :dots (for [beat beat-xs]
                     (cond
                       (not (click-beat? beat))
                       {:disabled? true
                        :actions (conj base-actions [:action/db.add (:db/id bar) :metronome/click-beats beat])}

                       (contains? (:metronome/accentuate-beats bar) beat)
                       {:highlight? true
                        :actions (into base-actions
                                       [[:action/db.retract (:db/id bar) :metronome/accentuate-beats beat]
                                        (if (:metronome/click-beats bar)
                                          [:action/db.retract (:db/id bar) :metronome/click-beats beat]
                                          [:action/transact [{:db/id (:db/id bar) :metronome/click-beats (set (remove #{beat} beat-xs))}]])])}

                       :else
                       {:actions (conj base-actions [:action/db.add (:db/id bar) :metronome/accentuate-beats beat])}))}
      (:music/tempo bar)
      (assoc :tempo {:val (:music/tempo paced-bar)
                     :unit "BPM"})

      (< 1 (or (:metronome/reps bar) 1))
      (assoc :reps {:val (:metronome/reps bar)
                    :unit "times"})

      (< 1 (count (:metronome/bars activity)))
      (assoc :buttons [{:text "Remove bar"
                        :icon (icons/icon :phosphor.regular/minus-circle)
                        :theme :warn
                        :actions (conj base-actions [:action/transact [[:db/retractEntity (:db/id bar)]]])}]))))

(defn prepare-bars [activity]
  (let [paced-bars (metronome/set-tempo (:music/tempo activity) (map #(into {} %) (:metronome/bars activity)))]
    {:kind :element.kind/bars
     :bars (map #(prepare-bar activity %1 %2) (:metronome/bars activity) paced-bars)
     :buttons [{:text "Add bar"
                :icon (icons/icon :phosphor.regular/music-notes-plus)
                :icon-size :large
                :actions (cond-> []
                           (not (:activity/paused? activity))
                           (into (stop-metronome activity))

                           :then
                           (conj [:action/transact
                                  [{:db/id (:db/id activity)
                                    :metronome/bars
                                    [{:ordered/idx (inc (apply max 0 (keep :ordered/idx (:metronome/bars activity))))
                                      :music/time-signature [4 4]}]}]]))}]}))

(defn prepare-metronome [activity]
  {:sections
   [(prepare-badge activity)
    (prepare-bars activity)
    (prepare-button-panel activity)]})
