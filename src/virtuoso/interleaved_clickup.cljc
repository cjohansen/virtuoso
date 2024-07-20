(ns virtuoso.interleaved-clickup)

(defn get-tempo-step [options]
  (or (::tempo-step options) 5))

(defn get-tempo-start [options]
  (or (::tempo-start options) 60))

(defn get-tempo [options]
  (or (::tempo-current options) (get-tempo-start options)))

(defn increase-tempo [options]
  (+ (get-tempo options) (get-tempo-step options)))

(defn decrease-tempo [options]
  (let [tempo (get-tempo options)]
    (when (< (get-tempo-start options) tempo)
      (- tempo (get-tempo-step options)))))

(defn get-phrase-size [options]
  (or (::phrase-size options) 4))

(defn get-phrases-tabs [options]
  (->> (::tabs options)
       (partition-all (get-phrase-size options))
       vec))

(defn get-n-phrases [options]
  (or (::phrase-count options)
      (count (get-phrases-tabs options))))

(defn get-next-phrase [options]
  (let [curr (::phrase-current options)]
    (cond
      (nil? curr)
      0

      (= curr (dec (get-n-phrases options)))
      nil

      :else
      (inc curr))))

(defn get-prev-phrase [options]
  (let [curr (::phrase-current options)]
    (when-not (or (nil? curr) (= 0 curr))
      (dec curr))))

(defn get-phrase-indices [options]
  (let [tempo-step (get-tempo-step options)
        tempo-diff (- (get-tempo options) (get-tempo-start options))]
    (if (= 1 (mod (/ tempo-diff tempo-step) 2))
      [(::phrase-current options)]
      (let [n (inc (::phrase-current options))
            rest (if (= 0 (::phrase-current options))
                   0
                   (mod (/ tempo-diff (* 2 tempo-step)) (::phrase-current options)))
            start (if (= 0 rest) 0 (- n (inc rest)))]
        (range start n)))))

(defn get-phrases [options]
  (cond
    (and (::phrase-max options)
         (not= 0 (::phrase-max options))
         (<= (::phrase-max options)
             (::phrase-current options)))
    (let [offset (- (::phrase-current options)
                    (dec (::phrase-max options)))]
      (map #(+ offset %)
           (-> options
               (update ::phrase-current - offset)
               get-phrase-indices)))

    :else
    (get-phrase-indices options)))

(defn select-phrases [options phrases]
  (cond
    (= :start/end (::start-at options))
    (let [n (get-n-phrases options)]
      (reverse (map #(- n % 1) phrases)))

    :else
    phrases))
