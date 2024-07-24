(ns virtuoso.test-helper
  (:require [clojure.walk :as walk]
            [datascript.core :as d]
            [virtuoso.ui.actions :as actions]
            [virtuoso.ui.db :as db]))

(defn ^{:style/indent 1} execute-actions [conn actions]
  (some->> actions
           (actions/perform-actions @conn)
           (actions/execute! conn)))

(defmacro with-conn
  {:clj-kondo/lint-as 'clojure.core/fn}
  [[binding] & body]
  `(let [~binding (db/connect)]
     ~@body))

(defmacro with-db
  {:clj-kondo/lint-as 'clojure.core/let}
  [[binding get-boot-actions] & body]
  `(let [conn# (db/connect)]
     (when (ifn? ~get-boot-actions)
       (execute-actions conn# (~get-boot-actions (d/db conn#))))
     (let [~binding (d/db conn#)]
       ~@body)))

(defn e->map [x]
  (cond
    (:db/id x) (update-vals (into {:db/id (:db/id x)} x) e->map)
    (map? x) (update-vals x e->map)
    (set? x) (set (map e->map x))
    (vector? x) (mapv e->map x)
    (coll? x) (map e->map x)
    :else x))

(defn strip-keys-by-ns [nss data]
  (walk/postwalk
   (fn [x]
     (cond-> x
       (map? x) (select-keys (remove (comp nss namespace) (keys x)))))
   (e->map data)))
