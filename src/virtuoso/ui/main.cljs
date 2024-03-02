(ns ^:figwheel-hooks virtuoso.ui.main
  (:require [replicant.dom :as replicant]
            [virtuoso.pages.icu.elements :as icue]
            [virtuoso.pages.icu.frontend :as icuf]
            [virtuoso.ui.actions :as actions]))

(defonce store (atom nil))
(def ^:dynamic *on-render* nil)

(defn prepare-and-render [el state prepare render-f]
  (let [page-data (prepare state)]
    (when (ifn? *on-render*)
      (*on-render* page-data))
    (some->> page-data
             render-f
             (replicant/render el))))

(defn render [state roots]
  (doseq [el roots]
    (case (.getAttribute el "data-view")
      "interleaved-clickup"
      (prepare-and-render el state icuf/prepare-ui-data icue/render))))

(defn get-roots []
  (seq (js/document.querySelectorAll ".replicant-root")))

(defn ^:after-load main []
  (swap! store assoc :reloaded-at (.getTime (js/Date.))))

(defn process-event [_rdata event data]
  (->> (actions/interpolate-event-data event data)
       actions/perform-actions
       (actions/execute! store)))

(defn boot []
  (replicant/set-dispatch! #'process-event)
  (let [roots (get-roots)]
    (add-watch store ::render (fn [_ _ _ state] (render state roots))))
  (js/document.body.addEventListener
   "keyup"
   (fn [e]
     (when (= js/document.body (.-target e))
       (->> (actions/get-keypress-actions @store {:key (.-key e)})
            actions/perform-actions
            (actions/execute! store)))))
  (swap! store assoc :booted-at (.getTime (js/Date.))))
