(ns virtuoso.interleaved-clickup)

(defn get-bpm-step [options]
  (or (:bpm-step options) 5))

(defn get-bpm-start [options]
  (or (:bpm-start options) 60))

(defn get-bpm [options]
  (or (:bpm-current options) (get-bpm-start options)))

(defn increase-bpm [options]
  (+ (get-bpm options) (get-bpm-step options)))

(defn decrease-bpm [options]
  (let [bpm (get-bpm options)]
    (when (< (get-bpm-start options) bpm)
      (- bpm (get-bpm-step options)))))

(defn get-phrase-size [options]
  (or (:phrase-size options) 4))

(defn get-phrases-tabs [options]
  (->> (:tabs options)
       (partition-all (get-phrase-size options))
       vec))

(defn get-n-phrases [options]
  (or (:phrase-count options)
      (count (get-phrases-tabs options))))

(defn get-next-phrase [options]
  (let [curr (:phrase-current options)]
    (cond
      (nil? curr)
      0

      (= curr (dec (get-n-phrases options)))
      nil

      :else
      (inc curr))))

(defn get-prev-phrase [options]
  (let [curr (:phrase-current options)]
    (when-not (or (nil? curr) (= 0 curr))
      (dec curr))))

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

(defn select-phrases [options phrases]
  (cond
    (= :start/end (:start-at options))
    (let [n (get-n-phrases options)]
      (reverse (map #(- n % 1) phrases)))

    :else
    phrases))
