(ns ^:figwheel-hooks virtuoso.ui.main
  (:require [replicant.dom :as replicant]
            [virtuoso.elements.page :as page]
            [virtuoso.metronome :as metronome]
            [virtuoso.pages.interleaved-clickup :as icu-page]
            [virtuoso.ui.actions :as actions]
            [virtuoso.ui.db :as db]))

(defonce conn (db/connect))
(defonce store (atom nil))
(defonce metronome (metronome/create-metronome))
(def ^:dynamic *on-render* nil)

(defmethod actions/execute-side-effect! ::start-metronome [_ {:keys [args]}]
  (let [[bars tempo] args]
    (cond->> bars
      :always metronome/click-beats
      :always metronome/accentuate-beats
      tempo (metronome/set-tempo tempo)
      :then (metronome/start metronome))))

(defmethod actions/execute-side-effect! ::stop-metronome [_ _]
  (metronome/stop metronome))

(defmethod actions/perform-action :action/start-metronome [_ _ args]
  (let [[options tempo] args]
    [{:kind ::start-metronome
      :args [options tempo]}]))

(defmethod actions/perform-action :action/stop-metronome [_ _ _]
  [{:kind ::stop-metronome}])

(defn prepare-and-render [el db prepare render-f]
  (let [page-data (prepare db)]
    (when (ifn? *on-render*)
      (*on-render* page-data))
    (some->> page-data
             render-f
             (replicant/render el))))

(defn render [db roots]
  (doseq [el roots]
    (case (.getAttribute el "data-view")
      "interleaved-clickup"
      (prepare-and-render el db icu-page/prepare-ui-data page/page))))

(defn execute-actions [conn actions]
  (some->> actions
           (actions/perform-actions @conn)
           (actions/execute! conn)))

(defn boot-roots [conn roots]
  (doseq [el roots]
    (case (.getAttribute el "data-view")
      "interleaved-clickup"
      (execute-actions conn (icu-page/get-boot-actions @conn)))))

(defn get-roots []
  (seq (js/document.querySelectorAll ".replicant-root")))

(defn ^:after-load main []
  (swap! store assoc :reloaded-at (.getTime (js/Date.))))

(defn process-event [conn event data]
  (execute-actions conn (actions/interpolate-event-data event data)))

(defn boot []
  (replicant/set-dispatch! #(process-event conn %2 %3))
  (let [roots (get-roots)]
    (boot-roots conn roots)
    (add-watch store ::render (fn [_ _ _ _] (render @conn roots)))
    (add-watch conn ::render (fn [_ _ _ db] (render db roots))))
  (js/document.body.addEventListener
   "keydown"
   (fn [e]
     (when (= js/document.body (.-target e))
       (execute-actions conn (actions/get-keypress-actions @conn {:key (.-key e)} e)))))
  (swap! store assoc :booted-at (.getTime (js/Date.))))
