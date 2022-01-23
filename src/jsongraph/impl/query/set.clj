(ns jsongraph.impl.query.set
   (:require [jsongraph.impl.utils :refer [add-items filter-nil]]
             [jsongraph.impl.graph :refer [gen-adjacency-item]]
             [jsongraph.impl.query.match :refer [ways-to-adj-edges]]))


(defn set-labels  [data labels]
  (if (some? labels)
    (assoc data :labels labels)
    data))

(defn set-properties [data properties]
    (assoc data :properties
      (filter-nil (add-items (data :properties) properties))))


(defn set-data [data query-data]
  ;(println data query-data)
  (-> data
    (set-labels (query-data :labels))
    (set-properties (query-data :properties))))

(defn set-node [adjacency node-index query-data]
  ;(println node-index query-data)
  (assoc!
    adjacency node-index
    (set-data (adjacency node-index) query-data)))

(defn set-nodes [adjacency ways query-data]
  ;(println ways query-data)
  (loop [adjacency (transient adjacency)
         ways ways]
    (if-let [node-index (first (first ways))]  ;ways is ((uuid-1) (uuid-2) ...)
      (recur
        (set-node adjacency node-index query-data)
        (rest ways))
      (persistent! adjacency))))


(defn set-out-edges [out-edges targets query-data]
  (loop [out-edges (transient out-edges)
         targets targets]
    (if (empty? targets)
      (persistent! out-edges)
      (recur
        (assoc! out-edges (first targets)
          (set-data (out-edges (first targets)) query-data))
        (rest targets)))))

(defn set-node-out-edges! [adjacency source targets query-data]
  (let [adj-item (adjacency source)]
  (assoc!
    adjacency source
    (gen-adjacency-item
      (adj-item :in-edges)
      (set-out-edges (adj-item :out-edges) targets query-data)
      (adj-item :labels)
      (adj-item :properties)))))

(defn set-edges [adjacency ways query-data]
  (let [adj-edges  (ways-to-adj-edges ways)]
   (loop [nodes (keys adj-edges)
          adjacency (transient adjacency)]
    (if (empty? nodes)
      (persistent! adjacency)
      (recur (rest nodes)
        (set-node-out-edges!
          adjacency
          (first nodes) (adj-edges (first nodes))
          query-data))))))


(defn set_impl [adjacency ways set-query]
  (case (count (first ways))
    0 (println "match answer empty")             ;ways empty
    1 (set-nodes adjacency ways set-query)       ;nodes
    2 (set-edges adjacency ways set-query)       ;edges
    (println "no implement for this match answer")
    ))




