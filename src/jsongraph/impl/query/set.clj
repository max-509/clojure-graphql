(ns jsongraph.impl.query.set
   (:require [jsongraph.impl.utils :refer [add-items filter-nil]]))


(defn set-node-labels  [node-data labels]
  (if (some? labels)
    (assoc node-data :labels labels)
    node-data))

(defn set-node-properties [node-data properties]
    (assoc
      node-data :properties
      (filter-nil (add-items (node-data :properties) properties))))


(defn set-node [adjacency node-index query-data]
  (assoc!
    adjacency node-index
      (-> (adjacency node-index)
          (set-node-labels  (query-data :labels))
          (set-node-properties (query-data :properties)))))

(defn set-nodes [adjacency ways query-data]
  (loop [adjacency (transient adjacency)
         ways ways]
    (if-let [node-index (first (first ways))]  ;ways is ((uuid-1) (uuid-2) ...)
      (recur
        (set-node adjacency node-index query-data)
        (rest ways))
      (persistent! adjacency))))


(defn SET [adjacency ways set-query]
  (case (count (first ways))
    0 (println "match answer empty")             ;ways empty
    1 (set-nodes adjacency ways set-query)       ;nodes
    2 (println "no implement for edges")         ;edges
    (println "no implement for this match answer")
    ))




