(ns virtuoso.elements.musical-notation
  "Express musical notation using the Bravura SMuFL
  font (https://w3c.github.io/smufl/latest/index.html).

  Turns out expressing musical notation with data is really quite difficult.
  This namespace merely provides a sort-of solution for a very narrow use case,
  and I'm not even very happy with how that is solved 😅 Enjoy!")

(defn ustr [code-point]
  #?(:cljs (js/String.fromCodePoint code-point)
     :clj (String. (Character/toChars code-point))))

(def augmentation-dot (ustr 0x1D16D))

(def sixteenth-note (ustr 0x1D161))
(def eighth-note (ustr 0x1D160))
(def quarter-note (ustr 0x1D15F))
(def half-note (ustr 0x1D15E))
(def whole-note (ustr 0x1D157))

(def note-long-stem (ustr 0xE1F1))
(def eighth-beam-long-stem (ustr 0xE1F8))
(def frac-eighth-long-stem (ustr 0xE1F3))
(def sixteenth-beam-long-stem (ustr 0xE1FA))
(def frac-sixteenth-long-stem (ustr 0xE1F5))

(def skewed? #{note-long-stem
               eighth-beam-long-stem
               frac-eighth-long-stem
               sixteenth-beam-long-stem
               frac-sixteenth-long-stem})

(defn position [note]
  (if (skewed? note)
    [:span.relative {:style {:bottom "-0.4em"}} note]
    note))

(defn dot [note]
  [:span.relative
   note
   [:span.pl-1.absolute
    (cond-> {}
      (not (#{frac-eighth-long-stem
              frac-sixteenth-long-stem} note))
      (assoc :class ["absolute"])

      (#{eighth-beam-long-stem
         sixteenth-beam-long-stem} note)
      (assoc :style {:left "0"}))
    augmentation-dot]])

(def note->hiccup
  {:note/sixteenth sixteenth-note
   :note/dotted-sixteenth (dot sixteenth-note)
   :note/eighth eighth-note
   :note/dotted-eighth (dot eighth-note)
   :note/quarter quarter-note
   :note/dotted-quarter (dot quarter-note)
   :note/half half-note
   :note/dotted-half (dot half-note)
   :note/whole whole-note
   :note/dotted-whole (dot whole-note)

   :beamed/note-stem note-long-stem
   :beamed/eighth-beam-long-stem eighth-beam-long-stem
   :beamed/sixteenth-beam-long-stem sixteenth-beam-long-stem
   :beamed/frac-eighth-long-stem frac-eighth-long-stem
   :beamed/frac-sixteenth-long-stem frac-sixteenth-long-stem})

(def beam-symbol
  {:note/eighth :beamed/eighth-beam-long-stem
   :note/sixteenth :beamed/sixteenth-beam-long-stem})

(def beamed-symbol
  {:note/eighth :beamed/frac-eighth-long-stem
   :note/sixteenth :beamed/frac-sixteenth-long-stem
   :beamed/note-stem :beamed/note-stem})

(defn beam-note [note]
  (if (vector? note)
    (update note 1 beamed-symbol)
    (beamed-symbol note)))

(defn beam [notes]
  (loop [prev :beamed/note-stem
         notes (seq notes)
         res []]
    (if notes
      (let [[candidate & more] notes
            [wrapped? note] (if (vector? candidate)
                              [true (second candidate)]
                              [false candidate])]
        (recur
         note
         more
         (cond-> res
           :always (conj (cond->> (beam-note (if more prev note))
                           wrapped? (assoc candidate 1)))
           more (conj (beam-symbol note)))))
      res)))

(defn render-beamed [note]
  (if (vector? note)
    (dot (position (note->hiccup (second note))))
    (position (note->hiccup note))))

(defn ^{:indent 1} render
  ([notes]
   (render nil notes))
  ([attrs notes]
   (into [:div.flex.gap-4
          (merge {:class (concat ["font-['Bravura']"] (:class attrs))}
                 (dissoc attrs :class))]
         (for [note notes]
           (cond
             (vector? note)
             (let [[notation & ns] note]
               (case notation
                 :notation/beam [:span (map render-beamed (beam ns))]
                 :notation/dot (dot (position (note->hiccup (first ns))))))

             :else
             [:span (position (note->hiccup note))])))))
