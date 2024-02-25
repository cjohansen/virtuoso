(ns virtuoso.ui.metronome)

(defn create-sine-wave [audio-ctx frequency]
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

(defn unmount [audio-ctx {:keys [oscillator gain]}]
  (some-> gain (.disconnect (.-destination audio-ctx)))
  (when oscillator
    (.stop oscillator)
    (.disconnect oscillator gain)))

(defn generate-bar-clicks
  ([bar]
   (generate-bar-clicks bar {:first-bar 0
                             :first-beat 0
                             :start-time 0}))
  ([bar {:keys [first-beat first-bar start-time]}]
   (let [[beats subdivision] (:time-signature bar)
         accentuate (set (:accentuate bar))
         click? (or (:click? bar) (constantly true))
         ms (/ (* 60 1000 4) (:bpm bar) subdivision)]
     (apply concat
            (for [rep (range (:repeat bar 1))]
              (let [rep-offset (* beats rep)
                    offset (+ first-beat rep-offset)]
                (->> (range beats)
                     (map (fn [beat]
                            {:bar-n (+ first-bar rep)
                             :bar-beat beat
                             :beat (+ first-beat (* rep beats) beat)}))
                     (filter #(click? % (+ offset %)))
                     (map (fn [click]
                            (cond-> (assoc click :click-at (+ start-time (* ms (+ (:bar-beat click) rep-offset))))
                              (accentuate (:bar-beat click)) (assoc :accentuate? true)))))))))))

(defn get-bar-duration [{:keys [time-signature repeat bpm]}]
  (let [beats (* (first time-signature) (or repeat 1))]
    {:beats beats
     :ms (* beats (/ (* 60 1000 4) bpm (second time-signature)))}))

(defn generate-clicks [bars & [{:keys [now first-bar first-beat]}]]
  (loop [bars (seq bars)
         res nil
         bar-n (or first-bar 0)
         beat-offset (or first-beat 0)
         start-time (or now 0)]
    (if (nil? bars)
      {:clicks res
       :bars bar-n
       :beats beat-offset
       :time start-time}
      (let [bar (first bars)
            {:keys [beats ms]} (get-bar-duration bar)]
        (recur (next bars)
               (concat res (generate-bar-clicks bar {:first-beat beat-offset
                                                     :start-time start-time
                                                     :first-bar bar-n}))
               (+ bar-n (:repeat bar 1))
               (+ beat-offset beats)
               (+ start-time ms))))))

(defn schedule-ticks [metronome opt]
  (let [{:keys [accent tick bars]} @metronome
        {:keys [clicks time bars beats]} (generate-clicks bars opt)]
    (doseq [{:keys [click-at accentuate?]} clicks]
      (let [{:keys [gain]} (if accentuate? accent tick)
            click-at (/ click-at 1000)]
        (.cancelScheduledValues (.-gain gain) click-at)
        (.setValueAtTime (.-gain gain) 0 click-at)
        (.linearRampToValueAtTime (.-gain gain) 1 (+ click-at 0.001))
        (.linearRampToValueAtTime (.-gain gain) 0 (+ click-at 0.001 0.01))))
    (swap! metronome assoc :tick-schedule
           (js/setTimeout
            (fn []
              (when (:running? @metronome)
                (schedule-ticks metronome {:now time
                                           :first-bar bars
                                           :first-beat beats})))
            (* 0.9 (- time (:now opt)))))))

(defn set-bpm [bpm bar]
  (assoc bar :bpm (int (* bpm (or (:tempo bar) 1)))))

(defn create-metronome [& audio-ctx]
  (atom {:audio-ctx (or audio-ctx (js/AudioContext.))}))

(defn stop [metronome]
  (when (:running? @metronome)
    (let [{:keys [tick accent count-in audio-ctx]} @metronome]
      (unmount audio-ctx tick)
      (when-not (= tick accent)
        (unmount audio-ctx accent))
      (unmount audio-ctx count-in))
    (when-let [t (:tick-schedule @metronome)]
      (js/clearTimeout t))
    (swap! metronome dissoc :running? :tick :accent :count-in :tick-schedule))
  nil)

(defn start [metronome bpm]
  (stop metronome)
  (let [bpm (or bpm 120)
        {:keys [tick-frequency accent-frequency count-in-frequency audio-ctx]} @metronome
        tick (create-sine-wave audio-ctx (or tick-frequency 1000))]
    (swap!
     metronome
     (fn [m]
       (-> m
           (assoc :running? true)
           (update :bars (fn [bars]
                           (->> (or (seq bars) [{:time-signature [4 4]}])
                                (map #(set-bpm bpm %)))))
           (assoc :count-in (when count-in-frequency
                              (create-sine-wave audio-ctx count-in-frequency)))
           (assoc :tick tick)
           (assoc :accent (or (when accent-frequency
                                (create-sine-wave audio-ctx accent-frequency))
                              tick)))))
    ;; Offset slightly to avoid the very fist click occasionally being cut short
    (schedule-ticks metronome {:now (+ 5 (* 1000 (.-currentTime (:audio-ctx @metronome))))
                               :first-bar 0
                               :first-beat 0})
    nil))

(comment

  (generate-clicks
   [{:time-signature [4 4]
     :accentuate #{0}
     :click? (constantly true)
     :repeat 2
     :bpm 120}
    {:time-signature [6 8]
     :accentuate #{0}
     :click? (constantly true)
     :repeat 2
     :bpm 90}])

  (def metronome
    (create-metronome
     (js/AudioContext.)
     {:bars [{:time-signature [4 4]
              :accentuate #{0}
              :click? #(< (rand-int 10) 5)}
             {:time-signature [6 4]
              :accentuate #{1}
              :click? (constantly true)
              :repeat 2
              :tempo (/ 6 4)}]
      :count-in-frequency 1500
      :tick-frequency 1000
      :accent-frequency 1250}))

  (start metronome 60)
  (start metronome 80)
  (start metronome 100)
  (start metronome 120)
  (start metronome 140)
  (start metronome 180)
  (stop metronome)

)
