(ns jsongraph.impl.query
  (:require [jsongraph.impl.utils :refer :all]
            [clj-uuid :as uuid]))


(defn gen-query-node
  [labels properties where & [index]]
  {(if index index (uuid/v0))
     {:out-edge   nil
      :labels     labels
      :properties properties
      :where      where}})


(defn query-edge-target
  [query-edge]
  (get-key query-edge))
(defn query-edge-labels
  [query-edge]
  ((get-val query-edge) :labels))
(defn query-edge-prop
  [query-edge]
  ((get-val query-edge) :properties))

(defn gen-query-edge
  [target labels properties]
  {target
   {:labels     labels
    :properties properties}})

;;comparison signs map
(def cs-map
  {
   :eq =  :ne not=
   :gt >  :lt <
   :ge >= :le <=
   }
  )

(defn query-where-cs [query-prop-item]
  (first (second query-prop-item)))

(defn query-where-val [query-prop-item]
  (second (second query-prop-item)))

(defn query-where-key [query-prop-item]
    (first query-prop-item))

(defn match-node-where-properties [node query-node]
  (let [node-prop   ((get-val node) :properties)
        query-where ((get-val query-node)  :where)  ; maybe nil
        d (list-difference (keys query-where) (keys node-prop))]
    (if (empty? d)
      (every?
         #((cs-map (query-where-cs %))
            (node-prop (query-where-key %))
            (query-where-val %))
        (get-items query-where)
      )
     (do (println (str "Keys " (vec d) " not found")) false)
    )
  )
 )

(defn match-node-properties [node query-node]
  (let [
        query-node-prop (query-node :properties)
        node-prop       (node       :properties)
        ]
    (if (map? query-node-prop)
      (empty? (json-difference query-node-prop node-prop))
      (and
        (empty? (json-difference (second query-node-prop) node-prop))
        (match-node-where-properties node-prop (first query-node-prop))
        )
      )
    )
  )

(defn match-node-labels [node query-node]
  (lists-equal ((get-val node) :labels) ((get-val query-node) :labels))
  )

(defn match-node [node query-node]
  (and
    (match-node-labels node query-node)
     ;(match-node-properties node query-node)
    (match-node-where-properties node query-node)
  )
)