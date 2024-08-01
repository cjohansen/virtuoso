(ns virtuoso.ui.db
  (:require [datascript.core :as d]
            [virtuoso.pages.interleaved-clickup :as icu]
            [virtuoso.ui.actions :as actions]
            [virtuoso.pages.metronome :as metronome]))

(defn connect []
  (d/create-conn
   (merge
    {:activity/paused? {} ;; boolean
     :ordered/idx {} ;; number, ordered collections
     :view/tool {:db/type :db.type/ref}
     }
    metronome/schema
    icu/schema)))

(defmethod actions/execute-side-effect! ::transact [conn {:keys [args]}]
  (try
    (d/transact conn args)
    (catch #?(:clj Exception
              :cljs :default) e
      (throw (ex-info "Failed to transact data" {:tx-data args} e)))))

(defn get-eid [e]
  (or (:db/id e) e))

(defmethod actions/perform-action :action/db.add [_ _ [e a v]]
  [{:kind ::transact
    :args [[:db/add (get-eid e) a v]]}])

(defmethod actions/perform-action :action/db.retract [_ _ [e a v]]
  [{:kind ::transact
    :args [[:db/retract (get-eid e) a v]]}])

(defmethod actions/perform-action :action/transact [_ _ [tx-data]]
  [{:kind ::transact
    :args tx-data}])
