(ns virtuoso.pages.metronome
  (:require [phosphor.icons :as icons]))

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

(defn prepare-metronome [activity]
  {:sections
   [(prepare-badge activity)
    (prepare-button-panel activity)]})
