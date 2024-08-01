(ns virtuoso.pages.metronome
  (:require [phosphor.icons :as icons]
            [virtuoso.metronome :as metronome]))

(defn get-step-size [activity]
  (or (:metronome/tempo-step-size activity) 5))

(defn start-metronome [activity & [tempo]]
  [:action/start-metronome (:metronome/bars activity) (or tempo (:music/tempo activity))])

(defn adjust-tempo [activity tempo-change]
  (let [target-tempo (+ (:music/tempo activity) tempo-change)]
    [[:action/db.add activity :music/tempo target-tempo]
     (start-metronome activity target-tempo)]))

(defn prepare-button-panel [activity]
  (let [step-size (get-step-size activity)]
    {:kind :element.kind/button-panel
     :buttons
     (for [button [{:text (str "Lower tempo by " step-size " bpm")
                    :icon (icons/icon :phosphor.bold/minus)
                    :icon-after-label (str step-size)
                    :actions (adjust-tempo activity (- step-size))
                    :kbd "p"}
                   {:text "Lower tempo"
                    :icon (icons/icon :phosphor.bold/minus)
                    :actions (adjust-tempo activity (- 1))
                    :kbd "-"}
                   (if (:activity/paused? activity)
                     {:text "Play"
                      :icon (icons/icon :phosphor.fill/play)
                      :actions [(start-metronome activity)]
                      :size :large
                      :kbd "space"}
                     {:text "Pause"
                      :icon (icons/icon :phosphor.fill/pause)
                      :actions [[:action/stop-metronome]]
                      :size :large
                      :kbd "space"})
                   {:text "Bump tempo"
                    :icon (icons/icon :phosphor.bold/plus)
                    :actions (adjust-tempo activity 1)
                    :kbd "+"}
                   {:text (str "Bump tempo by " step-size " bpm")
                    :icon (icons/icon :phosphor.bold/plus)
                    :icon-after-label (str step-size)
                    :actions (adjust-tempo activity step-size)
                    :kbd "n"}]]
       (cond-> button
         (nil? (:actions button)) (assoc :disabled? true)))}))

(defn prepare-badge [activity]
  {:kind :element.kind/round-badge
   :text (str (:music/tempo activity))
   :label "BPM"
   :theme (if (:activity/paused? activity) :neutral :success)})

(defn prepare-bar [activity bar paced-bar]
  (let [[beats subdivision] (:music/time-signature bar)
        beat-xs (range 1 (inc beats))
        click-beat? (set (or (:metronome/click-beats bar) beat-xs))]
    (cond-> {:beats {:val beats}
             :subdivision {:val subdivision}
             :dots (for [beat beat-xs]
                     (let [click-it? (click-beat? beat)]
                       (cond-> {}
                         (not click-it?)
                         (assoc :disabled? true)

                         (and click-it? (contains? (:metronome/accentuate-beats bar) beat))
                         (assoc :highlight? true))))}
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
                        :actions [[:action/transact [[:db/retractEntity (:db/id bar)]]]]}]))))

(defn prepare-bars [activity]
  (let [paced-bars (metronome/set-tempo (:music/tempo activity) (:metronome/bars activity))]
    {:kind :element.kind/bars
     :bars (map #(prepare-bar activity %1 %2) (:metronome/bars activity) paced-bars)
     :buttons [{:text "Add bar"
                :icon (icons/icon :phosphor.regular/music-notes-plus)
                :icon-size :large
                :actions [[:action/transact
                           [{:db/id (:db/id activity)
                             :metronome/bars
                             [{:ordered/idx (inc (apply max 0 (keep :ordered/idx (:metronome/bars activity))))
                               :music/time-signature [4 4]}]}]]]}]}))

(defn prepare-metronome [activity]
  {:sections
   [(prepare-badge activity)
    (prepare-bars activity)
    (prepare-button-panel activity)]})
