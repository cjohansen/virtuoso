(ns ^:figwheel-hooks virtuoso.ui.main
  (:require [replicant.dom :as replicant]
            [virtuoso.elements.page :as page]
            [virtuoso.pages.interleaved-clickup :as icu-page]
            [virtuoso.ui.actions :as actions]
            [virtuoso.ui.metronome :as metronome]))

(defonce store (atom nil))
(defonce metronome (metronome/create-metronome))
(def ^:dynamic *on-render* nil)

(defmethod actions/execute-side-effect! ::start-metronome [_ {:keys [args]}]
  (let [[bpm] args]
    (metronome/start metronome bpm)))

(defmethod actions/execute-side-effect! ::stop-metronome [_ _]
  (metronome/stop metronome))

(defmethod actions/perform-action :action/start-metronome [_ _ args]
  (let [[bpm] args]
    [{:kind ::actions/assoc-in
      :args [[:metronome :bpm] bpm]}
     {:kind ::start-metronome
      :args [bpm]}]))

(defmethod actions/perform-action :action/stop-metronome [_ _ _]
  [{:kind ::stop-metronome}])

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
      (prepare-and-render el state icu-page/prepare-ui-data page/page))))

(defn execute-actions [store actions]
  (some->> actions
           (actions/perform-actions @store)
           (actions/execute! store)))

(defn boot-roots [roots]
  (doseq [el roots]
    (case (.getAttribute el "data-view")
      "interleaved-clickup"
      (execute-actions store (icu-page/get-boot-actions @store)))))

(defn get-roots []
  (seq (js/document.querySelectorAll ".replicant-root")))

(defn ^:after-load main []
  (swap! store assoc :reloaded-at (.getTime (js/Date.))))

(defn process-event [_rdata event data]
  (execute-actions store (actions/interpolate-event-data event data)))

(defn boot []
  (replicant/set-dispatch! #'process-event)
  (let [roots (get-roots)]
    (boot-roots roots)
    (add-watch store ::render (fn [_ _ _ state] (render state roots))))
  (js/document.body.addEventListener
   "keydown"
   (fn [e]
     (when (= js/document.body (.-target e))
       (execute-actions store (actions/get-keypress-actions @store {:key (.-key e)} e)))))
  (swap! store assoc :booted-at (.getTime (js/Date.))))
