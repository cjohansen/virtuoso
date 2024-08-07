(ns virtuoso.elements.modal
  (:require [datascript.core :as d]
            [replicant.core :as replicant]
            [virtuoso.elements.page :as page]
            [virtuoso.ui.actions :as actions]))

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

(defmethod actions/perform-action :action/clear-modal [db _ _]
  (when-let [modal (get-current-modal db)]
    [{:kind :virtuoso.ui.db/transact
      :args [[:db/retractEntity (:db/id modal)]]}]))

(defn open-modal [{:replicant/keys [node]}]
  (.showModal node)
  (->> (fn [e]
         (replicant/*dispatch* e [[:action/clear-modal]]))
       (.addEventListener node "close")))

(defn render [props]
  (when props
    [:dialog#modal.modal.modal-bottom.sm:modal-middle
     {:replicant/on-mount #'open-modal}
     [:div.modal-box
      {:class (:modal/class props)}
      (when-let [title (:title props)]
        [:h1.text-lg.mb-2 title])
      (page/page props)
      [:form.modal-action {:method "dialog"}
       [:button.btn "Close"]]]
     [:form.modal-backdrop {:method "dialog"}
      [:button "Close"]]]))
