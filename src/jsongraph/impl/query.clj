(ns jsongraph.impl.query
  (:require [jsongraph.impl.utils :refer :all]
            [clj-uuid :as uuid]))


(defn gen-query-node
  [labels properties & [index]]
  {(if index index (uuid/v0))
     {:out-edges nil
      :labels    labels
      :properties properties}})


(defn match-node-labels [adj-node query-node]
  (lists-equal (adj-node :labels) (query-node :labels))
  )


;;comparison signs map
(def cs-map
  {
   :eq =  :ne not=
   :gt >  :lt <
   :ge >= :le <=
   }
  )

(defn- query-prop-cs [query-prop-item]
  (first (second query-prop-item)))

(defn- query-prop-val [query-prop-item]
  (second (second query-prop-item)))

(defn- query-prop-key [query-prop-item]
    (first query-prop-item))

(defn match-node-where-properties [adj-node-prop where-query-node]
  (if (subvec? (keys where-query-node) (keys adj-node-prop))
     (every?
       #((cs-map (query-prop-cs %))
          (adj-node-prop (query-prop-key %))
          (query-prop-val %)  )
       (get-items where-query-node)
     )
    (do (println "\n!where query contains unknown keys!\n") false)
    )

  )

(defn match-node-properties [node query-node]
  (let [
        query-node-prop (query-node :properties)
        node-prop   (node   :properties)
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

(defn match-node-in-edges [adj-node query-node]
  (lists-equal (adj-node :in-edges) (query-node :in-edges))
  )

(defn match-node [adj-node query-node]
  (let [diff (json-difference adj-node query-node)]
    (if (empty? diff)
      true
      2)
    )
  )