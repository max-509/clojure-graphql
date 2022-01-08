(ns jsongraph.api.graph-api
  (:require
    [jsongraph.impl.graph :refer :all]
    [jsongraph.impl.utils :refer [get-key get-field get-item add-items split-json]]
    [clj-uuid :as uuid] [jsonista.core :as j])

  (:import (java.io File)))



(defn gen-node
  [labels properties & [index]]
   {(if index index (uuid/v4)) (gen-adjacency-item [] {} labels properties)})

(defn index [node]
  (get-key node))

(defn index-from-many [nodes]
  (if (map? nodes)
    [(index nodes)]      ; only one node
    (mapv index nodes))) ; list of node


(defn add-nodes [graph nodes]
  (merge
    (get-item graph :metadata)
    (apply-to-adjacency graph add-items nodes)))

(defn delete-nodes [graph nodes]
  (merge
    (get-item graph :metadata)
    (delete-node-by-index graph (index-from-many nodes))))

(defn gen-edge-data
  [source target
   labels properties]
   (gen-edge (index source) (index target)
     labels properties))

(defn add-edges [graph edges]
  (merge
    (get-item graph :metadata)
    (apply-to-adjacency
      graph add-out-edges! edges)))

(defn delete-edges [graph edges]
  (merge
    (get-item graph :metadata)
    (apply-to-adjacency
      graph delete-edges-from-adjacency edges)))

(defn create-graph
  ([] (gen-empty-graph))
  ([nodes] (add-nodes (gen-empty-graph) nodes))
  ([nodes edges]
   (-> (gen-empty-graph)
       (add-nodes nodes)
       (add-edges edges))))

(defn create-one-edge-adjacency
  ([n-source n-target
   e-labels e-properties]
  (->  {}
       (add-items [n-source n-target])
       (add-out-edges! [(gen-edge-data
                    n-source n-target
                    e-labels e-properties)])))
  ([n-source n-target edge-data]
    (create-one-edge-adjacency
      n-source n-target
      ((second edge-data) :labels)
      ((second edge-data) :properties))
    ))

(defn get-nodes-from-graph [graph]
  (split-json (graph :adjacency)))


(defn save-graph [graph ^String path]
  (j/write-value (File. path) graph (j/object-mapper {:pretty true})))

(defn load-graph [^String path]
  (j/read-value (File. path) (j/object-mapper {:decode-key-fn true})))