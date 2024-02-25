(ns virtuoso.interleaved-clickup)

(defn get-bpm-step [options]
  (or (:bpm-step options) 5))

(defn get-bpm-start [options]
  (or (:bpm-start options) 60))

(defn get-bpm [options]
  (or (:bpm-current options) (get-bpm-start options)))

(defn bump-bpm [options]
  (+ (get-bpm options) (get-bpm-step options)))

(defn get-phrase-size [options]
  (or (:phrase-size options) 4))

(defn get-phrases-tabs [exercise options]
  (->> (:exercise/tabs exercise)
       (partition-all (get-phrase-size options))
       vec))

(defn get-n-phrases [exercise options]
  (or (:exercise/phrases exercise)
      (count (get-phrases-tabs exercise options))))

(defn get-step-n [exercise options]
  (let [curr (:phrase-current options)]
    (cond
      (nil? curr)
      0

      (= curr (dec (get-n-phrases exercise options)))
      nil

      :else
      (inc curr))))

(defn get-phrase-indices [options]
  (let [bpm-step (get-bpm-step options)
        bpm-diff (- (get-bpm options) (get-bpm-start options))]
    (if (= 1 (mod (/ bpm-diff bpm-step) 2))
      [(:phrase-current options)]
      (let [n (inc (:phrase-current options))
            rest (if (= 0 (:phrase-current options))
                   0
                   (mod (/ bpm-diff (* 2 bpm-step)) (:phrase-current options)))
            start (if (= 0 rest) 0 (- n (inc rest)))]
        (range start n)))))

(defn get-phrases [options]
  (cond
    (and (:max-phrases options)
         (<= (:max-phrases options) (:phrase-current options)))
    (let [offset (- (:phrase-current options) (dec (:max-phrases options)))]
      (map #(+ offset %)
           (-> options
               (update :phrase-current - offset)
               get-phrase-indices)))

    :else
    (get-phrase-indices options)))

(defn select-phrases [exercise options phrases]
  (cond
    (= :backward (:direction options))
    (let [n (get-n-phrases exercise options)]
      (reverse (map #(- n % 1) phrases)))

    :else
    phrases))
