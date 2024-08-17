(ns virtuoso.metronome)

(defn- create-sine-wave
  "Creates a sine wave oscillator with a gain and connects it to the audio
  context. Returns a map of `{:oscillator :gain}`"
  [audio-ctx frequency]
  (let [oscillator (.createOscillator audio-ctx)
        gain (.createGain audio-ctx)]
    (set! (.-type oscillator) "sine")
    (set! (.. oscillator -frequency -value) frequency)
    (set! (.. gain -gain -value) 0)
    (.connect oscillator gain)
    (.connect gain (.-destination audio-ctx))
    (.start oscillator 0)
    {:oscillator oscillator
     :gain gain}))

(defn ^:export create-metronome
  "Creates a metronome with an audio contex. Optionally pass in a pre-existing
  audio context.

  The optional map argument can be used to configure the metronome:

  - `:tick-frequency` the frequency for the tick. Defaults to 1000.
  - `:accent-frequency` the frequency of accented ticks. Defaults to 1250.
  - `:count-in-frequency` the frequency of the count-in. No default."
  [& {:keys [audio-ctx tick-frequency accent-frequency count-in-frequency]}]
  (atom {:audio-ctx (or audio-ctx #?(:cljs (js/AudioContext.)))
         :tick-frequency (or tick-frequency 1000)
         :accent-frequency (or accent-frequency 1250)
         :count-in-frequency count-in-frequency}))

(defn ^:export configure!
  "Re-configure the metronome, see `create-metronome` for options."
  [metronome {:keys [tick-frequency accent-frequency count-in-frequency] :as opt}]
  (swap! metronome
         (fn [m]
           (cond-> m
             :always (update :tick-frequency #(or tick-frequency %))
             (contains? opt :accent-frequency) (assoc :accent-frequency accent-frequency)
             (contains? opt :count-in-frequency) (assoc :count-in-frequency count-in-frequency)))))

(defn- unmount [audio-ctx {:keys [oscillator gain]}]
  (some-> gain (.disconnect (.-destination audio-ctx)))
  (when oscillator
    (.stop oscillator)
    (.disconnect oscillator gain)))

(defn generate-bar-clicks
  "Generates clicks for a single bar. A bar is a map of:

  - `:music/time-signature` - a tuple of [beats subdivision], e.g. `[4 4]`.
  - `:music/tempo` - the bar tempo, as a number of beats per minute.
  - `:bar/rhythm` - a list of note durations that make up the bar rhythm
  - `:bar/reps` - optional number of times to repeat the bar.
  - `:click?` - a function that receives a click and decides if it should, well,
                click.
  - `:accentuate?` - a function that receives a click and decides if it should
                     be accentuated.`

  A click, as passed to `:metronome/click?` and `:metronome/accentuate?` is a
  map of:

  - `:bar/n` - the total bar number, 1 indexed.
  - `:bar/beat` - the beat number within the bar, 1 indexed.
  - `:beat/n` - the total beat number, 1 indexed.
  - `:rhythm/n` - the current position in the rhythm pattern

  Returns a sequence of clicks. In addition to the above keys, it includes:

  - `:metronome/click-at` a timestamp (e.g. ms since epoch) of when to trigger
                          this click.
  - `:metronome/accentuate?` a boolean indicating if the click should be
                             accentuated."
  [bar {:keys [first-beat first-bar start-time]}]
  (let [[beats subdivision] (or (:music/time-signature bar) [4 4])
        accentuate? (or (:accentuate? bar) (constantly false))
        click? (or (:click? bar) (constantly true))
        quarter-note-duration (/ (* 60 1000) (or (:music/tempo bar) 120))
        beat-duration (* quarter-note-duration (/ 4 subdivision))
        bar-duration (* quarter-note-duration
                        beats
                        (/ 4 subdivision)) ;; A beat is always a quarter note
        duration (* bar-duration (or (:bar/reps bar) 1))
        rhythm (or (:bar/rhythm bar) [(/ 1 subdivision)])
        rhythm-pattern (cycle rhythm)
        rhythm-pattern-len (count rhythm)]
    (loop [now 0
           bar-now 0
           n 0
           specs rhythm-pattern
           clicks []]
      (if (< now duration)
        (if (< bar-now bar-duration)
          ;; (/ 1 4) represents a quarter note, so multiply by 4
          (let [dur (* quarter-note-duration (first specs) 4)
                bar-n (int (/ now bar-duration))
                beat-n (int (/ now beat-duration))
                click {:bar/n (+ first-bar bar-n)
                       :bar/beat (inc (mod beat-n beats))
                       :beat/n (+ first-beat beat-n)
                       :rhythm/n (inc (mod n rhythm-pattern-len))}]
            (recur
             (+ now dur)
             (+ bar-now dur)
             (inc n)
             (next specs)
             (cond-> clicks
               (click? click)
               (conj (cond-> click
                       (accentuate? click) (assoc :metronome/accentuate? true)
                       :then (assoc :metronome/click-at (+ start-time now)))))))
          (recur (+ (- now bar-now) bar-duration) 0 n rhythm-pattern clicks))
        {:duration duration
         :beats (* beats (or (:bar/reps bar) 1))
         :clicks clicks}))))

(defn generate-clicks
  "Generate clicks for the metronome from the sequence of `bars`."
  [bars & [{:keys [now first-bar first-beat]}]]
  (loop [bars (seq bars)
         res nil
         bar-n (or first-bar 1)
         beat-offset (or first-beat 1)
         start-time (or now 0)]
    (if (nil? bars)
      {:clicks res
       :bar-count (dec bar-n)
       :beat-count (dec beat-offset)
       :time start-time
       :duration (- start-time (or now 0))}
      (let [bar (update (first bars) :music/time-signature #(or % [4 4]))
            {:keys [beats duration clicks]}
            (generate-bar-clicks bar {:first-beat beat-offset
                                      :start-time start-time
                                      :first-bar bar-n})]
        (recur (next bars)
               (concat res clicks)
               (+ bar-n (:bar/reps bar 1))
               (+ beat-offset beats)
               (+ start-time duration))))))

(defn- set-timeout [f ms]
  #?(:cljs (js/setTimeout f ms)
     :clj [f ms] ;; silence clj-kondo
     ))

(defn- clear-timeout [id]
  #?(:cljs (js/clearTimeout id)
     :clj id ;; silence clj-kondo
     ))

(defn schedule-ticks [metronome bars opt]
  (let [{:keys [accent tick]} @metronome
        {:keys [clicks bar-count beat-count time duration]} (generate-clicks bars opt)]
    (doseq [{:metronome/keys [click-at accentuate?]} clicks]
      (let [{:keys [gain]} (if accentuate? accent tick)
            click-at (/ click-at 1000)]
        (.cancelScheduledValues (.-gain gain) click-at)
        (.setValueAtTime (.-gain gain) 0 click-at)
        (.linearRampToValueAtTime (.-gain gain) 1 (+ click-at 0.001))
        (.linearRampToValueAtTime (.-gain gain) 0 (+ click-at 0.001 0.01))))
    (swap! metronome assoc :tick-schedule
           (set-timeout
            (fn []
              (when (:running? @metronome)
                (->> (merge opt {:now time
                                 :first-bar bar-count
                                 :first-beat beat-count})
                     (schedule-ticks metronome bars))))
            (* 0.9 duration)))))

(defn set-tempo [tempo bars]
  (let [start-tempo (or (:music/tempo (first bars)) tempo)
        scale (/ tempo start-tempo)]
    (loop [bars (seq bars)
           res []
           current-tempo start-tempo]
      (if (nil? bars)
        res
        (let [bar (first bars)
              bar-tempo (or (:music/tempo bar) current-tempo)]
          (recur
           (next bars)
           (conj res (assoc bar :music/tempo (* scale bar-tempo)))
           bar-tempo))))))

(defn set-default [k v bars]
  (for [bar bars]
    (update (into {} bar) k #(or % v))))

(defn accentuate-beats
  "Converts `:metronome/accentuate-beats` on bars to an `:accentuate?` function."
  [bars]
  (for [bar bars]
    (let [beats (:metronome/accentuate-beats bar)]
      (cond-> (into {} bar)
        beats (dissoc :metronome/accentuate-beats)
        beats (assoc :accentuate? (comp (set beats) :bar/beat))))))

(defn click-beats
  "Prepares a `:click?` function on bars that have either
  `:metronome/click-beats` (a set of beat numbers to click), or
  `:metronome/drop-pct` (a percentage of beats to randomly drop)."
  [bars]
  (for [bar bars]
    (let [click? (or (when-let [pct (:metronome/drop-pct bar)]
                       (when (< 0 pct)
                         (if-let [click-beat? (some-> bar :metronome/click-beats set)]
                           (fn [click]
                             (let [i (rand-int 100)]
                               (and (< pct i) (click-beat? (:bar/beat click)))))
                           (fn [_]
                             (let [i (rand-int 100)]
                               (< pct i))))))
                     (some-> (:metronome/click-beats bar) set (comp :bar/beat)))]
      (cond-> (into {} bar)
        click? (dissoc :metronome/click-beats :metronome/drop-pct)
        click? (assoc :click? click?)))))

(defn stop [metronome]
  (when (:running? @metronome)
    (let [{:keys [tick accent count-in audio-ctx]} @metronome]
      (unmount audio-ctx tick)
      (when-not (= tick accent)
        (unmount audio-ctx accent))
      (unmount audio-ctx count-in))
    (when-let [t (:tick-schedule @metronome)]
      (clear-timeout t))
    (swap! metronome dissoc :running? :tick :accent :count-in :tick-schedule))
  nil)

(defn start [metronome bars]
  (stop metronome)
  (let [{:keys [tick-frequency accent-frequency count-in-frequency audio-ctx]} @metronome
        tick (create-sine-wave audio-ctx (or tick-frequency 1000))]
    (swap!
     metronome
     (fn [m]
       (-> m
           (assoc :running? true)
           (assoc :count-in (when count-in-frequency
                              (create-sine-wave audio-ctx count-in-frequency)))
           (assoc :tick tick)
           (assoc :accent (or (when accent-frequency
                                (create-sine-wave audio-ctx accent-frequency))
                              tick)))))
    (schedule-ticks
     metronome
     (if (map? bars) [bars] bars)
     {;; Offset slightly to avoid the very fist click occasionally being cut short
      :now (+ 5 (* 1000 (.-currentTime (:audio-ctx @metronome))))
      :first-bar 1
      :first-beat 1})
    nil))
