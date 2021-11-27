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
    {
     :adjacency
     (add-items
       (graph :adjacency)
       nodes
       )
     }
   )
 )


(defn delete-node [graph tags]
  (println tags)
  (merge
    (get-item graph :metadata)
    {
     :adjacency
     (delete-items
       (graph :adjacency)
       (if (coll? tags) tags [tags])
       )
     }
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
          (assoc (add-in-edges adjacency
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
    {
     :adjacency
     (add-out-edges
      (graph :adjacency) edges
      )
     }
  )
 )

(defn delete-adjacency-edge [adjacency source targets]
 (assoc
   adjacency source
   (gen-adjacency-item []
     (delete-items ((adjacency source) :out-edges) targets)
     ((adjacency source) :node-data)
   )
 )
)

(defn delete-edges-from-adjacency  [adjacency edges]
  (let [edges (adjacency-from-edges edges)]
    (loop [-keys (keys edges)
           -vals (vals edges)
           adjacency adjacency]
      (if (empty? -keys)
        adjacency
        (recur
          (rest -keys) (rest -vals)
          (delete-adjacency-edge
            adjacency
            (first -keys) (keys (first -vals))
          )
        )
      )
    )
  )
)


(defn delete-edges [graph edges]
  (merge
    (get-item graph :metadata)
    {
     :adjacency
     (delete-edges-from-adjacency
       (graph :adjacency)
       edges
       )
     }
  )
 )