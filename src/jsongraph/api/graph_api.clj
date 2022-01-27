(ns jsongraph.api.graph-api
  (:require
    [jsongraph.impl.index :refer [add-labels-in-index delete-labels-in-index]]
    [jsongraph.impl.graph :refer :all]
    [jsongraph.impl.utils :refer [get-key get-field add-items split-json]]
    [clj-uuid :as uuid]
    [jsonista.core :as j]
    [clojure.walk :refer [keywordize-keys]])

  (:import (java.io File)
           (java.util UUID)))

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
   {(if index index (keyword (str (uuid/v4)))) (gen-adjacency-item [] {} labels properties)})

(defn index [node]
  (get-key node))

(defn index-from-many [nodes]
  (if (map? nodes)
    [(index nodes)]      ; only one node
    (mapv index nodes))) ; list of node


(defn add-nodes [graph nodes]
  (graph-from-meta-adj
    (graph :metadata)
    (add-items (graph :adjacency) nodes)))

(defn delete-nodes [graph nodes]
  (graph-from-meta-adj
    (graph :metadata)
    (delete-node-by-index graph (index-from-many nodes))))

(defn- keyword->uuid [kw]
  (try
    (UUID/fromString (name kw))
    (catch Exception _ nil)))

(defn gen-edge-data
  [source target
   labels properties]
  (let [source (if (not (nil? (keyword->uuid source))) source (index source))
        target (if (not (nil? (keyword->uuid target))) target (index target))]
    (gen-edge source target
              labels properties)))

(defn add-edges [graph edges]
  (graph-from-meta-adj
    (graph :metadata)
    (add-out-edges! (graph :adjacency)  edges)))

(defn delete-edges [graph edges]
  (graph-from-meta-adj
    (graph :metadata)
    (delete-edges-from-adjacency (graph :adjacency)  edges)))

(defn create-graph
  ([] (gen-empty-graph))
  ([nodes] (add-nodes (gen-empty-graph) nodes))
  ([nodes edges]
   (-> (gen-empty-graph)
       (add-nodes nodes)
       (add-edges edges))))

(defn get-nodes-from-graph [graph]
  (split-json (graph :adjacency)))


(defn add-labels-index [graph labels & [metadata-only]]
  (if (boolean metadata-only) (add-labels-in-index graph labels)
   (graph-from-meta-adj
     (add-labels-in-index graph labels)
     (graph :adjacency))))

(defn delete-labels-index [graph labels]
  (graph-from-meta-adj
     (delete-labels-in-index (graph :metadata) labels)
     (graph :adjacency)))


(defn save-graph [graph ^String filename]
  (j/write-value
    (File. (str "./resources/" filename)) graph
    (j/object-mapper {:pretty true :encode-key-fn true})))

(defn decode-str-to-kw [json]
  (let [new-json (transient {})]
    (do
      (doseq [-key (sort (keys json))]
        (let [json-item (json -key)]
         (if (map? json-item) (assoc! new-json (keyword -key) (decode-str-to-kw json-item))
           (if (coll? json-item) (assoc! new-json (keyword -key) (mapv #(keyword %) json-item))
             (assoc! new-json (keyword -key) json-item)))))
      (persistent! new-json))))



(defn load-graph [^String filename & [deep-decode]]
  (if (boolean deep-decode)
    (decode-str-to-kw    (j/read-value (File. (str "./resources/" filename))))
    (keywordize-keys    (j/read-value (File. (str "./resources/" filename))))))