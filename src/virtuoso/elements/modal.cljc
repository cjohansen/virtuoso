(ns virtuoso.elements.modal
  (:require [datascript.core :as d]
            [virtuoso.elements.page :as page]))

(def schema
  {:modal/layer-idx {:db/unique :db.unique/identity} ;; number
   :modal/kind {} ;; keyword
   :modal/params {}
   })

(defn get-current-modal [db]
  (some->> (d/q '[:find ?e ?idx
                  :where
                  [?e :modal/layer-idx ?idx]]
                db)
           (sort-by second)
           ffirst
           (d/entity db)))

(defn get-open-modal-actions [db kind & [params]]
  (let [modal (get-current-modal db)]
    [[:action/transact
      [(cond-> {:modal/layer-idx (or (some-> modal :modal/layer-idx inc) 0)
                :modal/kind kind}
         params (assoc :modal/params params))]]]))

(defn open-modal [{:replicant/keys [node]}]
  (.showModal node))

(defn render [props]
  [:dialog
   {:replicant/on-mount #'open-modal}
   (when-let [title (:title props)]
     [:h1 title])
   (page/page props)])
