(ns virtuoso.ui.interleaved-clickup
  (:require [phosphor.icons :as icons]
            [virtuoso.elements.typography :as t]
            [virtuoso.interleaved-clickup :as icu]
            [virtuoso.ui.actions :as actions]
            [virtuoso.ui.tabs :as tabs]))

(defn started? [options]
  (:bpm-current options))

(defn bump-bpm [options]
  (let [bpm (icu/increase-bpm options)]
    [[:action/assoc-in [::options :bpm-current] bpm]
     [:action/start-metronome bpm]]))

(defn add-phrase [options next-phrase]
  (when next-phrase
    (let [bpm (icu/get-bpm-start options)]
      [[:action/assoc-in
        [::options :bpm-current] bpm
        [::options :phrase-current] next-phrase]
       [:action/start-metronome bpm]])))

(defn stop [options]
  [[:action/assoc-in [::options] (select-keys options [:direction :phrase-size :bpm-start])]
   [:action/stop-metronome]])

(defmethod actions/get-keypress-actions ::tool [{::keys [options] :keys [exercise]} e]
  (when (started? options)
    (case (:key e)
      "+" (bump-bpm options)
      " " (stop options)
      "n" (add-phrase options (icu/get-next-phrase options)))))

(defn prep-mode-button [state text options]
  (cond-> {:text text}
    (not= (select-keys (::options state) (keys options)) options)
    (assoc :actions [[:action/assoc-in [::options] options]])))

(defn prepare-controls [{::keys [options] :keys [exercise]}]
  (cond
    (nil? (:direction options))
    nil

    (not (started? options))
    (let [bpm (icu/get-bpm options)]
      {:form {:fields [{:label "Start BPM"
                        :value bpm
                        :actions [[:action/assoc-in [::options :bpm-start] :event/target-value-num]]}
                       {:label "BPM step"
                        :value (icu/get-bpm-step options)
                        :actions [[:action/assoc-in [::options :bpm-step] :event/target-value-num]]}]}
       :buttons [{:text "Start"
                  :size :large
                  :icon (icons/icon :phosphor.regular/play)
                  :actions [[:action/assoc-in
                             [::options :bpm-current] bpm
                             [::options :phrase-current] (icu/get-next-phrase options)]
                            [:action/start-metronome bpm]]}]})

    :else
    {:buttons [{:text "Stop"
                :size :large
                :icon (icons/icon :phosphor.regular/stop)
                :actions (stop options)}
               {:text "Bump BPM"
                :icon (icons/icon :phosphor.regular/plus)
                :actions (bump-bpm options)}
               (let [next-phrase (icu/get-next-phrase options)]
                 {:text "Add phrase"
                  :icon (icons/icon :phosphor.regular/skip-forward)
                  :disabled? (not next-phrase)
                  :actions (add-phrase options next-phrase)})]}))

(defn prepare-tabs [{::keys [options] :keys [exercise]}]
  (if (started? options)
    (let [phrases (icu/get-phrases-tabs options)
          indices (icu/select-phrases options (icu/get-phrases options))
          glue-note (first (get phrases (inc (last indices))))]
      (-> (cond-> (mapcat phrases indices)
            glue-note (concat [glue-note]))
          (tabs/visualize-multi-line 24)))
    (tabs/visualize-multi-line (:exercise/tabs exercise) 24)))

(defn prepare [{:keys [exercise] :as state}]
  {:preformatted (when (:exercise/tabs exercise)
                   (prepare-tabs state))
   :texts (remove empty? [(when-let [bpm (:bpm-current (::options state))]
                            [:strong bpm " BPM"])])
   :controls (prepare-controls state)
   :buttons (if (:exercise/tabs exercise)
              (->> [3 4 6 8]
                   (mapcat (fn [n]
                             [(prep-mode-button state (str n " from the top") {:direction :forward :phrase-size n})
                              (prep-mode-button state (str n " from the end") {:direction :backward :phrase-size n})])))
              [(prep-mode-button state "From the top" {:direction :forward})
               (prep-mode-button state "From the end" {:direction :backward})])})

(defn render-form [{:keys [fields]}]
  (for [{:keys [label value actions]} fields]
    [:label.form-control.mb-4
     [:div.label [:span.label-text.text-xs label]]
     [:input.input.input-bordered.input-sm.w-16
      {:type "text"
       :value value
       :on {:input actions}}]]))

(defn icon-button [{:keys [text size icon actions disabled?]}]
  [:div.btn.btn-circle.btn-primary.btn
   {:title text
    :class [(when-not (= :large size)
              :btn-sm)
            (when disabled? :btn-disabled)]
    :on {:click actions}}
   (icons/render icon {:class (if (= :large size)
                                "h-6 w-6"
                                "h-4 w-4")})])

(defn render [{:keys [preformatted buttons texts controls]}]
  [:div
   (for [text preformatted]
     (t/preformatted text))
   (for [text texts]
     [:p.mb-2 text])
   (when (or (:form controls) (:buttons controls))
     [:div.flex.items-center.gap-4.mt-4
      (when-let [form (:form controls)]
        (render-form form))
      (for [button (:buttons controls)]
        (icon-button button))])
   [:nav.flex.flex-wrap.gap-2.mt-4
    (for [{:keys [text actions]} buttons]
      [:button.btn.btn-sm
       {:class (when-not actions ["btn-primary"])
        :key text
        :on {:click actions}}
       text])]])
