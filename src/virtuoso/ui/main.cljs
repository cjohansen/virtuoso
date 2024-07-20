(ns ^:figwheel-hooks virtuoso.ui.main
  (:require [datascript.core :as d]
            [replicant.dom :as replicant]
            [virtuoso.elements.page :as page]
            [virtuoso.metronome :as metronome]
            [virtuoso.pages.interleaved-clickup :as icu-page]
            [virtuoso.ui.actions :as actions]))

(defonce conn
  (d/create-conn
   {:music/tempo {} ;; number, bpm
    :music/time-signature {} ;; tuple of numbers [4 4]

    :metronome/accentuate-beats {} ;; set of numbers #{1}
    :metronome/tick-beats {} ;; set of numbers #{1 2 3 4}
    :metronome/drop-pct {} ;; number, percentage of beats to randomly drop

    ;; Interleaved-clickup specific attributes
    :virtuoso.interleaved-clickup/tempo-start {} ;; number
    :virtuoso.interleaved-clickup/tempo-step {} ;; number
    :virtuoso.interleaved-clickup/tempo-current {} ;; number
    :virtuoso.interleaved-clickup/phrase-max {} ;; number
    :virtuoso.interleaved-clickup/phrase-count {} ;; number
    :virtuoso.interleaved-clickup/phrase-size {} ;; number
    :virtuoso.interleaved-clickup/phrase-kind {} ;; keyword
    :virtuoso.interleaved-clickup/start-at {} ;; keyword
    }))

(defonce store (atom nil))
(defonce metronome (metronome/create-metronome))
(def ^:dynamic *on-render* nil)

(defmethod actions/execute-side-effect! ::transact [_ conn {:keys [args]}]
  (d/transact conn args))

(defmethod actions/execute-side-effect! ::start-metronome [_ _ {:keys [args]}]
  (let [[bar tempo] args]
    (cond->> [bar]
      :always metronome/click-beats
      :always metronome/accentuate-beats
      tempo (metronome/set-tempo tempo)
      :then (metronome/start metronome))))

(defmethod actions/execute-side-effect! ::stop-metronome [_ _ _]
  (metronome/stop metronome))

(defmethod actions/perform-action :action/start-metronome [_ _ _ args]
  (let [[options tempo] args]
    [{:kind ::actions/assoc-in
      :args [[:metronome :bpm] tempo]}
     {:kind ::start-metronome
      :args [options tempo]}]))

(defmethod actions/perform-action :action/stop-metronome [_ _ _ _]
  [{:kind ::stop-metronome}])

(defn prepare-and-render [el state db prepare render-f]
  (let [page-data (prepare state db)]
    (when (ifn? *on-render*)
      (*on-render* page-data))
    (some->> page-data
             render-f
             (replicant/render el))))

(defn render [state db roots]
  (doseq [el roots]
    (case (.getAttribute el "data-view")
      "interleaved-clickup"
      (prepare-and-render el state db icu-page/prepare-ui-data page/page))))

(defn execute-actions [store conn actions]
  (some->> actions
           (actions/perform-actions @store @conn)
           (actions/execute! store conn)))

(defn boot-roots [store conn roots]
  (doseq [el roots]
    (case (.getAttribute el "data-view")
      "interleaved-clickup"
      (execute-actions store conn (icu-page/get-boot-actions @store @conn)))))

(defn get-roots []
  (seq (js/document.querySelectorAll ".replicant-root")))

(defn ^:after-load main []
  (swap! store assoc :reloaded-at (.getTime (js/Date.))))

(defn process-event [store conn event data]
  (execute-actions store conn (actions/interpolate-event-data event data)))

(defn boot []
  (replicant/set-dispatch! #(process-event store conn %2 %3))
  (let [roots (get-roots)]
    (boot-roots store conn roots)
    (add-watch store ::render (fn [_ _ _ state] (render state @conn roots)))
    (add-watch conn ::render (fn [_ _ _ db] (render @store db roots)))
    )
  (js/document.body.addEventListener
   "keydown"
   (fn [e]
     (when (= js/document.body (.-target e))
       (execute-actions store conn (actions/get-keypress-actions @store {:key (.-key e)} e)))))
  (swap! store assoc :booted-at (.getTime (js/Date.))))
