(ns virtuoso.ui.actions
  (:require [clojure.walk :as walk]))

(defmulti get-keypress-actions (fn [state _e] (:action/keypress-handler state)))

(defmethod get-keypress-actions :default [_state _e])

(defmulti execute-side-effect! (fn [_store {:keys [kind]}] kind))

(defmulti perform-action (fn [_state action _args] action))

(defn parse-number [s]
  (when (not-empty s)
    (parse-long s)))

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

(defmethod perform-action :action/assoc-in [_ _ args]
  [{:kind ::assoc-in
    :args args}])

(defn perform-actions [state actions]
  (mapcat
   (fn [[action & args]]
     (apply println "[virtuoso.ui.action]" action args)
     (perform-action state action args))
   actions))

(defn assoc-in* [m args]
  (reduce
   (fn [m [path v]]
     (assoc-in m path v))
   m
   (partition 2 args)))

(defmethod execute-side-effect! ::assoc-in [store {:keys [args]}]
  (swap! store assoc-in* args))

(defn execute! [store effects]
  (doseq [effect effects]
    (execute-side-effect! store effect)))
