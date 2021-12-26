(ns jsongraph.api.match-api
  (:require [jsongraph.impl.utils :refer :all]
            [jsongraph.impl.query.match :refer :all]
            [clj-uuid :as uuid]))

;;generators
(defn gen-query-edge
  ([]
   {(uuid/v4)
   {:labels     nil
    :properties nil
    :where      nil}})
  ( [labels properties & [where]]
  {(uuid/v4)
   {:labels     labels
    :properties properties
    :where      where}}))

(defn gen-query-node
  ([]
   {(uuid/v4)
   {:out-edge   nil
    :labels     nil
    :properties nil
    :where      nil}})
  ([labels properties where & [index]]
  {(if index index (uuid/v4))
     {:out-edge   nil
      :labels     labels
      :properties properties
      :where      where}}))

(defn node-to-query-node [node where]
  (gen-query-node
    (get-field node :labels)
    (get-field node :properties)
    where   (get-key node)
    )
  )


(defn add-edge-into-query-node [query-node query-edge query-target-node]
  (merge
    (assoc-in query-node [(get-key query-node) :out-edge] query-edge)
         {(get-key query-edge) (get-val query-target-node)}))

(defn match-query [graph query]

  (let [adjacency (graph :adjacency)]
  (loop [nodes (split-json adjacency)
         query-nodes (split-json query)
         matched-nodes (transient {})]

    (if (empty? nodes)
      (persistent! matched-nodes)
      (recur
        (rest nodes)
        query-nodes
        (if (match-node (first nodes) (first query-nodes))
          (assoc!
             matched-nodes
             (get-key (first nodes))
             (get-matched-targets
               adjacency query
               (get-matched-edges (first nodes) query)))
          matched-nodes))))))
