(ns virtuoso.ui.actions
  (:require [clojure.walk :as walk]))

(defmulti execute-side-effect! (fn [_conn {:keys [kind]}] kind))

(defmulti perform-action (fn [_db action _args] action))

(defmethod perform-action :action/start-metronome [_ _ args]
  (let [[options tempo] args]
    [{:kind :virtuoso/start-metronome
      :args [options tempo]}]))

(defmethod perform-action :action/stop-metronome [_ _ _]
  [{:kind :virtuoso/stop-metronome}])

(defn parse-number [s]
  (when (not-empty s)
    (parse-long s)))

(defn interpolate-event-data [event data]
  (walk/postwalk
   (fn [x]
     (cond
       (= :event/key x) (.-key event)
       (= :event/target-value x) (some-> event .-target .-value)
       (= :event/target-value-num x) (or (some-> event .-target .-value .trim parse-number) 0)
       (= :event/target-value-kw x) (some-> event .-target .-value .trim keyword)
       :else x))
   data))

(defn perform-actions [db actions]
  (mapcat
   (fn [[action & args]]
     (apply println "[virtuoso.ui.action]" action args)
     (perform-action db action args))
   (remove nil? actions)))

(defn execute! [conn effects]
  (doseq [effect effects]
    (execute-side-effect! conn effect)))
