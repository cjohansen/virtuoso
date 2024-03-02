(ns virtuoso.ui.actions
  (:require [clojure.walk :as walk]
            [virtuoso.ui.metronome :as metronome]))

(defmulti get-keypress-actions
  (fn [state _e] (:action/keypress-handler state)))

(defmethod get-keypress-actions :default [_state _e])

(defn parse-number [s]
  (when (not-empty s)
    (js/parseInt s)))

(defn interpolate-event-data [event data]
  (walk/postwalk
   (fn [x]
     (cond
       (= :event/key x) (.-key event)
       (= :event/target-value x) (some-> event .-target .-value)
       (= :event/target-value-num x) (some-> event .-target .-value .trim parse-number)
       (= :event/target-value-kw x) (some-> event .-target .-value .trim keyword)
       :else x))
   data))

(def metronome (metronome/create-metronome))

(defn perform-actions [actions]
  (mapcat
   (fn [[action & args]]
     (apply println "[virtuoso.ui.action]" action args)
     (case action
       :action/assoc-in
       [{:kind ::assoc-in
         :args args}]

       :action/start-metronome
       (let [[bpm] args]
         [{:kind ::assoc-in
           :args [[:metronome :bpm] bpm]}
          {:kind ::start-metronome
           :args [bpm]}])

       :action/stop-metronome
       [{:kind ::stop-metronome}]))
   actions))

(defn assoc-in* [m args]
  (reduce
   (fn [m [path v]]
     (assoc-in m path v))
   m
   (partition 2 args)))

(defn execute! [store effects]
  (doseq [{:keys [kind args]} effects]
    (case kind
      ::assoc-in (swap! store assoc-in* args)
      ::start-metronome (let [[bpm] args]
                          (metronome/start metronome bpm))
      ::stop-metronome (metronome/stop metronome))))
