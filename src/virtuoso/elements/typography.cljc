(ns virtuoso.elements.typography)

(defn h1
  ([text] (h1 nil text))
  ([props text]
   [:h1.text-2xl.md:text-3xl
    props
    text]))

(defn h2
  ([text] (h2 nil text))
  ([_props text]
   [:h2.mb-4
    text]))

(defn p
  ([text] (p nil text))
  ([props & texts]
   [:p.opacity-80.my-4.text-sm.last:mb-0 props texts]))
