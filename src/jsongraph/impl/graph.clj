(ns jsongraph.impl.graph
    (:require [jsongraph.impl.utils :refer :all]
              [clojure.set :refer :all]
              [clojure.data :refer :all]))

(defn gen-adjacency-item
  [in-edges out-edges labels properties]
  {
   :in-edges  in-edges
   :out-edges out-edges

   :labels    labels
   :properties properties})

(defn assoc-out-edges-adjacency-item
  [adjacency-item  out-edges]
  (assoc adjacency-item :out-edges (merge (:out-edges adjacency-item) out-edges)))

(defn gen-edge
  [source-idx target-idx
   labels properties]
 [[source-idx target-idx]
  {:labels labels :properties properties}])

(defn get-edge-source [edge-data]
  (first (first edge-data)))

(defn get-edge-target [edge-data]
  (second (first edge-data)))

(defn get-edge-data [edge-data]
  (second edge-data))

(defn graph-from-meta-adj [metadata adjacency]
  {:metadata   metadata
   :adjacency  adjacency})

(defn gen-empty-graph []
  (graph-from-meta-adj {} {}))

(defn node-to-edges-data [node]
  (map
    #(gen-edge (get-key node) (first %)
               ((second %) :labels) ((second %) :properties))
    (get-field node :out-edges)))

(defn adjacency-to-edges-data [adjacency]
  (loop [nodes (split-json adjacency)
         edges ()]
    (if-let [node (first nodes)]
      (recur
        (rest nodes)
        (concat edges (node-to-edges-data node)))
      edges)))

(defn convert-edge-to-adjacency [edge]
  [(get-edge-source edge)
   {(get-edge-target edge)
    (get-edge-data edge)}])

(defn adjacency-from-edges [edges]
  (assoc-items (map convert-edge-to-adjacency edges)))


(defn add-in-edge-adjacency!
  [adjacency
   target source]

  (let [adjacency-item (adjacency target)]
    (assoc!
      adjacency target
      (gen-adjacency-item
        (conj (adjacency-item :in-edges) source)
        (adjacency-item :out-edges)
        (adjacency-item :labels)
        (adjacency-item :properties)))))


(defn add-in-edges!
  [adjacency targets source]

  (if (empty? targets)
    adjacency
    (recur
      (add-in-edge-adjacency!
        adjacency
        (first targets)
        source)

      (rest targets)
      source)))

(defn add-out-edges! [adjacency edges]
  (let [edges (adjacency-from-edges edges)]
    (loop [-keys (keys edges)
           -vals (vals edges)
           adjacency (transient adjacency)]
      (if (empty? -keys)
        (persistent! adjacency)
        (recur
          (rest -keys) (rest -vals)
          (assoc!
            (add-in-edges!
              adjacency
              (keys (first -vals))
              (first -keys))

            (first -keys)
            (assoc-out-edges-adjacency-item
              (adjacency (first -keys))
              (first -vals))))))))


(defn delete-in-edge-adjacency
  [adjacency target sources]
  (if-let [adjacency-item (adjacency target)]
    (assoc!
      adjacency
      target
      (gen-adjacency-item
        (filterv #(not (.contains sources %)) (adjacency-item :in-edges))
        (adjacency-item :out-edges)
        (adjacency-item :labels)
        (adjacency-item :properties)))

    adjacency))


(defn delete-in-edges
  ([adjacency targets sources]
   (delete-in-edges adjacency [targets sources]))

  ([adjacency [targets sources]]
   (loop [targets (if (some? targets) (wrap targets) (keys adjacency))
          adjacency (transient adjacency)
          sources (wrap sources)]

     (if (empty? targets)
       (persistent! adjacency)
       (recur
         (rest targets)
         (delete-in-edge-adjacency
           adjacency
           (first targets)
           sources)
         sources)))))


(defn delete-in-edges-in-all-node [adjacency targets]
  (let [-keys (keys adjacency)]
    (delete-in-edges
      adjacency
      [-keys targets])))

(defn delete-adjacency-edge
  ([adjacency source targets]
   (delete-adjacency-edge adjacency [targets source]))

  ([adjacency [targets source]]
   (let [adjacency (transient
                     (delete-in-edges
                       adjacency targets source))
         adjacency-item (adjacency source)]
     (if (nil? adjacency-item)
       (persistent! adjacency)
       (persistent!
         (assoc!
           adjacency
           source
           (gen-adjacency-item
             (adjacency-item :in-edges)
             (delete-items (adjacency-item :out-edges) targets)
             (adjacency-item :labels)
             (adjacency-item :properties))))))))

(defn delete-kv-edges-from-adjacency
  [adjacency -keys -vals]
  (if (empty? -keys)
    adjacency
    (recur
      (delete-adjacency-edge
        adjacency
        (first -keys)
        (first -vals))

      (rest -keys) (rest -vals))))

(defn delete-edges-by-target-indexes [adjacency targets]
  (let [-keys (keys adjacency)]
    (delete-kv-edges-from-adjacency
      adjacency
      -keys (repeat (count -keys) targets))))

(defn delete-edges-from-adjacency [adjacency edges]
  (let [edges (adjacency-from-edges edges)]
    (delete-kv-edges-from-adjacency
      adjacency
      (keys edges) (map keys (vals edges)))))

(defn delete-node-by-index [graph idx-nodes]
  (let [idx-nodes (wrap idx-nodes)]
    (graph-from-meta-adj
      (graph :metadata)
      (-> (graph :adjacency)
          (delete-items idx-nodes)
          (delete-edges-by-target-indexes idx-nodes)
          (delete-in-edges-in-all-node idx-nodes)))))
