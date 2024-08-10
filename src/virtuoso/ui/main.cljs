(ns ^:figwheel-hooks virtuoso.ui.main
  (:require [datascript.core :as d]
            [replicant.dom :as replicant]
            [virtuoso.elements.modal :as modal]
            [virtuoso.elements.page :as page]
            [virtuoso.metronome :as metronome]
            [virtuoso.pages.interleaved-clickup :as icu-page]
            [virtuoso.pages.metronome :as metronome-page]
            [virtuoso.ui.actions :as actions]
            [virtuoso.ui.db :as db]))

(defonce conn (db/connect))
(defonce metronome (metronome/create-metronome))
(def ^:dynamic *on-render* nil)

(def features
  {"interleaved-clickup"
   {:feature/prepare icu-page/prepare-ui-data
    :feature/render page/page
    :feature/get-boot-actions icu-page/get-boot-actions
    :feature/get-keypress-actions icu-page/get-keypress-actions}

   "metronome"
   {:feature/prepare metronome-page/prepare-ui-data
    :feature/prepare-modal metronome-page/prepare-modal-data
    :feature/render page/page
    :feature/get-boot-actions metronome-page/get-boot-actions
    :feature/get-keypress-actions metronome-page/get-keypress-actions}})

(defmethod actions/execute-side-effect! ::start-metronome [_ {:keys [args]}]
  (let [[activity] args
        drop-pct (:metronome/drop-pct activity)
        tempo (:music/tempo activity)]
    (cond->> (:music/bars activity)
      drop-pct (metronome/set-default :metronome/drop-pct drop-pct)
      :always metronome/click-beats
      :always metronome/accentuate-beats
      tempo (metronome/set-tempo tempo)
      :then (metronome/start metronome))))

(defmethod actions/execute-side-effect! ::stop-metronome [_ _]
  (metronome/stop metronome))

(defmethod actions/execute-side-effect! ::db/transact [conn {:keys [args]}]
  (try
    (d/transact conn args)
    (catch :default e
      (throw (ex-info "Failed to transact data" {:tx-data args} e)))))

(defmethod actions/perform-action :action/start-metronome [_ _ args]
  (let [[options tempo] args]
    [{:kind ::start-metronome
      :args [options tempo]}]))

(defmethod actions/perform-action :action/stop-metronome [_ _ _]
  [{:kind ::stop-metronome}])

(defn prepare-and-render-ui [el db k prepare render & data]
  (let [page-data (apply prepare db data)]
    (when (ifn? *on-render*)
      (*on-render* k page-data))
    (->> page-data
         render
         (replicant/render el))))

(defn prepare-and-render [el db {:feature/keys [prepare render prepare-modal render-modal]}]
  (let [ui-el (.-firstChild el)
        modal-el (.-nextSibling ui-el)
        render-page (or render page/page)
        render-modal (or render-modal modal/render)]
    (if-let [modal (when prepare-modal (modal/get-current-modal db))]
      (do
        (when-not (.-firstChild ui-el)
          ;; Render UI at least once, so it displays under the modal
          (prepare-and-render-ui ui-el db ::ui-layer prepare render-page))
        (prepare-and-render-ui modal-el db ::modal-layer prepare-modal render-modal modal))
      (do
        (when (.-firstChild modal-el)
          ;; Unmount existing modal
          (prepare-and-render-ui modal-el db ::modal-layer (constantly nil) render-modal nil))
        (prepare-and-render-ui ui-el db ::ui-layer prepare render-page)))))

(defn render [db roots]
  (doseq [{:keys [el id]} roots]
    (prepare-and-render el db (get features id))))

(defn execute-actions [conn actions]
  (some->> actions
           (actions/perform-actions @conn)
           (actions/execute! conn)))

(defn add-class [el class]
  (.add (.-classList el) class))

(defn boot-roots [conn roots]
  (doseq [{:keys [el id]} roots]
    (.appendChild el (doto (js/document.createElement "div")
                       (add-class "ui-layer")))
    (.appendChild el (doto (js/document.createElement "div")
                       (add-class "modal-layer")))
    (when-let [get-boot-actions (get-in features [id :feature/get-boot-actions])]
      (execute-actions conn (get-boot-actions @conn)))))

(defn get-roots []
  (for [el (seq (js/document.querySelectorAll ".replicant-root"))]
    {:el el
     :id (.getAttribute el "data-view")}))

(defn ^{:after-load true :export true} main []
  (d/transact conn [{:db/ident :virtuoso/app
                     :app/reloaded-at (.getTime (js/Date.))}]))

(defn process-event [conn event data]
  (execute-actions conn (actions/interpolate-event-data (:replicant/js-event event) data)))

(defn get-keypress-actions [roots db data]
  (loop [xs (seq roots)]
    (when xs
      (let [f (get-in features [(:id (first xs)) :feature/get-keypress-actions])]
        (if (ifn? f)
          (f db data)
          (recur (next xs)))))))

(defn boot []
  (replicant/set-dispatch! #(process-event conn %1 %2))
  (let [roots (get-roots)]
    (boot-roots conn roots)
    (add-watch conn ::render (fn [_ _ _ db] (render db roots)))
    (js/document.body.addEventListener
     "keydown"
     (fn [e]
       (when (= js/document.body (.-target e))
         (when-let [actions (get-keypress-actions roots @conn {:key (.-key e)})]
           (.preventDefault e)
           (.stopPropagation e)
           (execute-actions conn actions))))))
  (d/transact conn [{:db/ident :virtuoso/app
                     :app/booted-at (.getTime (js/Date.))}]))
