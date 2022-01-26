(ns jsongraph.api.graph-api
  (:require
    [jsongraph.impl.index :refer [add-labels-in-index delete-labels-in-index]]
    [jsongraph.impl.graph :refer :all]
    [jsongraph.impl.utils :refer [get-key get-field add-items split-json]]
    [clj-uuid :as uuid] [jsonista.core :as j])

  (:import (java.io File)))

(defn edge-source [edge]
  (get-edge-source edge))

(defn edge-target [edge]
  (get-edge-target edge))

(defn edge-properties [edge]
  (:properties (second edge)))

(defn edge-labels [edge]
  (:labels (second edge)))

(defn node-val [node]
  (first (vals node)))

(defn node-properties [node-val]
  (:properties node-val))

(defn node-labels [node-val]
  (:labels node-val))

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
  (graph-from-meta-adj
    (graph :metadata)
    (apply-to-adjacency graph add-items nodes)))

(defn delete-nodes [graph nodes]
  (graph-from-meta-adj
    (graph :metadata)
    (delete-node-by-index graph (index-from-many nodes))))

(defn gen-edge-data
  [source target
   labels properties]
  (let [source (if (uuid? source) source (index source))
        target (if (uuid? target) target (index target))]
    (gen-edge source target
              labels properties)))

(defn add-edges [graph edges]
  (graph-from-meta-adj
    (graph :metadata)
    (apply-to-adjacency graph add-out-edges! edges)))

(defn delete-edges [graph edges]
  (graph-from-meta-adj
    (graph :metadata)
    (apply-to-adjacency graph delete-edges-from-adjacency edges)))

(defn create-graph
  ([] (gen-empty-graph))
  ([nodes] (add-nodes (gen-empty-graph) nodes))
  ([nodes edges]
   (-> (gen-empty-graph)
       (add-nodes nodes)
       (add-edges edges))))

(defn get-nodes-from-graph [graph]
  (split-json (graph :adjacency)))


(defn save-graph [graph ^String path]
  (j/write-value (File. path) graph (j/object-mapper {:pretty true})))

(defn load-graph [^String path]
  (j/read-value (File. path) (j/object-mapper {:decode-key-fn true})))

(defn add-labels-index [graph labels & [metadata-only]]
  (if (boolean metadata-only) (add-labels-in-index graph labels)
   (graph-from-meta-adj
     (add-labels-in-index graph labels)
     (graph :adjacency))))

(defn delete-labels-index [graph labels]
  (graph-from-meta-adj
     (delete-labels-in-index (graph :metadata) labels)
     (graph :adjacency)))