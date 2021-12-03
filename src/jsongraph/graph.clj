(ns jsongraph.graph
    (:require [jsongraph.utils :refer :all]
      ;[clojure.data.json :as json]
              ))

(defn gen-adjacency-item
  [
   in-edges
   out-edges
   data
   ]
  {
   :in-edges  in-edges                               ; in edges
   :out-edges out-edges                              ; out edges
   :node-data data                                   ; some node data
   }
  )

(defn assoc-out-edges-adjacency-item
  [
   adjacency-item
   out-edges
   ]
  (assoc adjacency-item :out-edges out-edges)
  )

(defn apply-to-adjacency
  [graph-adj func args & [no-zip-adj]]
  (let [graph-adj (func (graph-adj :adjacency graph-adj) args)]
    (if (boolean no-zip-adj)
      graph-adj
      {:adjacency  graph-adj}
    )
  )
)

(defn gen-node
  [
   tag                                                      ; index (name keyword)
   data                                                     ; node data (json)
   ]
  {
    tag (gen-adjacency-item [] {} data)
   }
  )

; TODO: develop edge format. Now it is '([source target] data)
(defn gen-edge
  [
   [source target]
   data
   ]
  ([source target] data)
  )


(defn get-edge-start [edge-data]
  (first (first edge-data))
  )

(defn get-edge-target [edge-data]
  (second (first edge-data))
  )

(defn get-edge-data [edge-data]
  (second edge-data)
  )


(defn gen-empty-graph []
  {
   :metadata   {}
   :adjacency  {}
   }
  )

(defn convert-edge-to-adjacency [edge]
  (list
    (get-edge-start edge)
    {
       (get-edge-target edge)
       (get-edge-data edge)
    }
    )
  )

(defn adjacency-from-edges [edges]
  (assoc-items (map convert-edge-to-adjacency edges))
  )


(defn add-node [graph nodes]
  (merge
    (get-item graph :metadata)
    (apply-to-adjacency graph add-items nodes)
   )
 )

(defn add-in-edge-adjacency
  [
   adjacency
   target source
   ]
  (let [adjacency-item (adjacency target)]
      (assoc
        adjacency
        target
        (gen-adjacency-item
          (conj (adjacency-item :in-edges) source)
          (adjacency-item :out-edges)
          (adjacency-item :node-data)
         )
      )
    )
  )

(defn add-in-edges
  [
   adjacency targets source
   ]
  (if (empty? targets)
    adjacency
    (recur
      (add-in-edge-adjacency
        adjacency
        (first targets)
        source
        )
      (rest targets)
      source
    )
  )
)

(defn add-out-edges  [adjacency edges]
  (let [edges (adjacency-from-edges edges)]
    (loop [-keys (keys edges)
           -vals (vals edges)
           adjacency adjacency]
      (if (empty? -keys)
        adjacency
        (recur
          (rest -keys) (rest -vals)
          (assoc
            (add-in-edges
              adjacency
              (keys (first -vals))
              (first -keys)
            )
            (first -keys)
            (assoc-out-edges-adjacency-item
              (adjacency (first -keys))
              (first -vals)
            )
          )
        )
      )
    )
  )
)

(defn add-edge [graph edges]
  (merge
    (get-item graph :metadata)
    (apply-to-adjacency
      graph add-out-edges edges)
  )
 )


(defn delete-in-edge-adjacency
  [
   adjacency
   target sources
   ]
  (if-let [adjacency-item (adjacency target)]
      (assoc
        adjacency
        target
        (gen-adjacency-item
          (filterv #(not (.contains sources %)) (adjacency-item :in-edges))
          (adjacency-item :out-edges)
          (adjacency-item :node-data)
        )
      )
      adjacency
    )
  )


(defn delete-in-edges
  ([adjacency targets sources]
   (delete-in-edges adjacency [targets sources])
   )

  ( [adjacency [targets sources]]
   (loop [adjacency adjacency
          targets (if (some? targets) (if (coll? targets) targets [targets]) (keys adjacency))
          sources (if (coll? sources) sources [sources])]
      (if (empty? targets)
        adjacency
        (recur
          (delete-in-edge-adjacency
            adjacency
            (first targets)
            sources
           )
          (rest targets)
          sources
        )
      )
    )
   )
  )


(defn delete-in-edges-in-all-node [adjacency targets]
  (let [-keys (keys adjacency)]
     (delete-in-edges
        adjacency
        [-keys targets]
      )
   )
 )

(defn delete-adjacency-edge
  ([adjacency source targets]
   (delete-adjacency-edge adjacency [targets source])
   )
  ([adjacency [targets source]]
     (let [adjacency (delete-in-edges
                         adjacency targets source)
           adjacency-item (adjacency source)]
       (assoc
         adjacency
         source
         (gen-adjacency-item
            (adjacency-item :in-edges)
            (delete-items (adjacency-item :out-edges) targets)
            (adjacency-item :node-data)
         )
        )
     )
   )
)

(defn delete-kv-edges-from-adjacency
  [adjacency -keys -vals]
    (if (empty? -keys)
      adjacency
      (recur
        (delete-adjacency-edge
          adjacency
           (first -keys)
           (first -vals)
         )
        (rest -keys) (rest -vals)
      )
   )
 )

(defn delete-edges-in-all-nodes [adjacency targets]
  (let [-keys (keys adjacency)]
     (delete-kv-edges-from-adjacency
        adjacency
        -keys (repeat (count -keys) targets)
      )
   )
 )

(defn delete-edges-from-adjacency  [adjacency edges]
  (let [edges (adjacency-from-edges edges)]
    (delete-kv-edges-from-adjacency
      adjacency
      (keys edges) (map keys (vals edges))
      )
  )
)

(defn delete-node [graph nodes-tags]
  (let [nodes-tags  (if (coll? nodes-tags) nodes-tags [nodes-tags])]
   (merge
     (get-item graph :metadata)
     (-> graph
         (apply-to-adjacency
             delete-items
             nodes-tags true
          )
         (apply-to-adjacency
             delete-edges-in-all-nodes
             nodes-tags true
          )
         (apply-to-adjacency
             delete-in-edges-in-all-node
             nodes-tags
         )
    )
   )
  )
 )

(defn delete-edges [graph edges]
  (merge
    (get-item graph :metadata)
    (apply-to-adjacency
      graph delete-edges-from-adjacency edges)
  )
 )