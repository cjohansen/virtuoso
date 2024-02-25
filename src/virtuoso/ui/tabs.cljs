(ns virtuoso.ui.tabs
  (:require [clojure.string :as str]))

(defn pad [s w]
  (let [s (str s)]
    (str (str/join (repeat (- w (count s)) " ")) s)))

(defn get-text [note]
  (or (some-> note :fret str)
      (first (:markers note))))

(defn visualize [tab-notes]
  (let [strings [:e :B :G :D :A :E nil]
        strs (reduce
              (fn [res notes]
                (let [w (apply max 0 (map count (keep get-text notes)))
                      nbs (group-by :string notes)]
                  (reduce
                   (fn [res string]
                     (update res string conj
                             (pad (get-text (get-in nbs [string 0])) w)))
                   res
                   strings)))
              (into {} (map (juxt identity (constantly [])) strings))
              tab-notes)]
    (->> (for [s strings]
           (str (or (some-> s name) " ") " | " (str/join " " (conj (strs s) "|"))))
         (str/join "\n"))))

(defn visualize-multi-line [tab-notes & [n]]
  (->> (partition-all (or n 30) tab-notes)
       (map visualize)))
