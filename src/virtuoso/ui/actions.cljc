(ns virtuoso.ui.actions
  (:require [clojure.walk :as walk]
            [datascript.core :as d]))

(defmulti get-keypress-actions
  (fn [db _e]
    (:action/keypress-handler (d/entity db :virtuoso/current-view))))

(defmethod get-keypress-actions :default [_db _e])

(defmulti execute-side-effect! (fn [_conn {:keys [kind]}] kind))

(defmulti perform-action (fn [_db action _args] action))

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

(defn perform-actions [db actions]
  (mapcat
   (fn [[action & args]]
     (apply println "[virtuoso.ui.action]" action args)
     (perform-action db action args))
   (remove nil? actions)))

(defn execute! [conn effects]
  (doseq [effect effects]
    (execute-side-effect! conn effect)))
