(ns virtuoso.elements.bar-scenes
  (:require [phosphor.icons :as icons]
            [portfolio.replicant :refer [defscene]]
            [virtuoso.elements.bar :as bar]))

(defscene bar
  (bar/bar
   {:beats {:val 4}
    :subdivision {:val 4}
    :dots [{:highlight? true}
           {}
           {}
           {}]}))

(defscene repeated-bar
  (bar/bar
   {:beats {:val 4}
    :subdivision {:val 4}
    :reps {:val 2 :unit "times"}
    :dots [{} {} {} {}]}))

(defscene odd-time-signature
  (bar/bar
   {:beats {:val 15}
    :subdivision {:val 8}
    :dots (repeat 15 {})}))

(defscene with-tempo
  (bar/bar
   {:beats {:val 7}
    :subdivision {:val 8}
    :tempo {:val 60 :unit "BPM"}
    :dots [{}
           {:disabled? true}
           {}
           {:disabled? true}
           {:disabled? true}
           {}]}))

(defscene being-clicked
  :params (atom {:beats {:val 7}
                 :subdivision {:val 8}
                 :tempo {:val 60 :unit "BPM"}
                 :current 2
                 :dots [{}
                        {:disabled? true}
                        {}
                        {:disabled? true}
                        {:disabled? true}
                        {}
                        {}]})
  :on-mount (fn [params]
              (swap! params assoc ::timer
                     (js/setInterval
                      (fn []
                        (swap! params update :current #(inc (mod % 7)))) 500)))
  :on-unmount (fn [params]
                (js/clearInterval (::timer @params)))
  [params]
  (let [{:keys [current] :as bar} @params]
    (bar/bar
     (assoc bar :dots (map-indexed
                       (fn [idx dot]
                         (cond-> dot
                           (= (inc idx) current)
                           (assoc :current? true)))
                       (:dots bar))))))

(defscene multiple-bars
  (bar/bars
   {:bars [{:beats {:val 4}
            :subdivision {:val 4}
            :tempo {:val 60 :unit "BPM"}
            :reps {:val 2 :unit "times"}
            :dots [{:highlight? true} {} {} {}]}
           {:beats {:val 12}
            :subdivision {:val 8}
            :tempo {:val 70 :unit "BPM"}
            :dots [{:highlight? true} {} {}
                   {:disabled? true} {} {} {} {} {}
                   {:disabled? true} {} {}]}]}))

(defscene bar-with-buttons
  (bar/bar
   {:beats {:val 4
            :left-button {:icon (icons/icon :phosphor.regular/minus-circle)
                          :actions []}
            :right-button {:icon (icons/icon :phosphor.regular/plus-circle)
                           :actions []}}
    :subdivision {:val 4
                  :left-button {:icon (icons/icon :phosphor.regular/minus-circle)}
                  :right-button {:icon (icons/icon :phosphor.regular/plus-circle)
                                 :actions []}}
    :dots [{:highlight? true} {} {} {}]}))

(defscene editable-big-bar
  (bar/bar
   {:beats {:val 4
            :left-button {:icon (icons/icon :phosphor.regular/minus-circle)
                          :actions []}
            :right-button {:icon (icons/icon :phosphor.regular/plus-circle)
                           :actions []}}
    :subdivision {:val 4
                  :left-button {:icon (icons/icon :phosphor.regular/minus-circle)}
                  :right-button {:icon (icons/icon :phosphor.regular/plus-circle)
                                 :actions []}}
    :reps {:val 1
           :unit "time"
           :button-above {:icon (icons/icon :phosphor.regular/minus-circle)}
           :button-below {:icon (icons/icon :phosphor.regular/plus-circle)
                          :actions []}}
    :tempo {:val 70 :unit "BPM" :actions []}
    :dots [{:highlight? true} {} {} {}]
    :size :large}))

(defscene editable-bar-without-tempo
  (bar/bar
   {:beats {:val 4
            :left-button {:icon (icons/icon :phosphor.regular/minus-circle)
                          :actions []}
            :right-button {:icon (icons/icon :phosphor.regular/plus-circle)
                           :actions []}}
    :subdivision {:val 4
                  :left-button {:icon (icons/icon :phosphor.regular/minus-circle)}
                  :right-button {:icon (icons/icon :phosphor.regular/plus-circle)
                                 :actions []}}
    :reps {:val 2
           :unit "times"
           :button-above {:icon (icons/icon :phosphor.regular/minus-circle)
                          :actions []}
           :button-below {:icon (icons/icon :phosphor.regular/plus-circle)
                          :actions []}}
    :dots [{:highlight? true} {} {} {}]
    :size :large}))

(defscene editable-bar-with-default-tempo
  (bar/bar
   {:beats {:val 4
            :left-button {:icon (icons/icon :phosphor.regular/minus-circle)
                          :actions []}
            :right-button {:icon (icons/icon :phosphor.regular/plus-circle)
                           :actions []}}
    :subdivision {:val 4
                  :left-button {:icon (icons/icon :phosphor.regular/minus-circle)}
                  :right-button {:icon (icons/icon :phosphor.regular/plus-circle)
                                 :actions []}}
    :reps {:val 2
           :unit "times"
           :button-above {:icon (icons/icon :phosphor.regular/minus-circle)
                          :actions []}
           :button-below {:icon (icons/icon :phosphor.regular/plus-circle)
                          :actions []}}
    :tempo {:val 70 :unit "BPM" :actions [] :default? true}
    :dots [{:highlight? true} {} {} {}]
    :size :large}))