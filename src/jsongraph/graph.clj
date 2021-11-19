(ns jsongraph.graph
    (:require [jsongraph.utils :refer :all]

            )
  )


(defn gen-node
  [
   tag                                                      ; Keyword
   data                                                     ; json
   ]
  {
    tag data
   }
  )


(defn gen-edge
  [
   tag                                                      ; Keyword
   [source target]                                          ; source and target node tag
   data                                                     ; json
    ]
  {
    tag {
         :nodes (list source target)
         :data  data
         }
   }
  )



(defn gen-empty-graph []
  {
   :metadata {}
   :nodes {}
   :edges {}
   }
  )



(defn add-node [graph nodes]
  (merge
    (get-item graph :metadata)
    {
     :nodes
     (add-items
       (graph :nodes)
       nodes
       )
     }
    (get-item graph :edges)
   )
 )


(defn delete-node [graph tags]
  (println tags)
  (merge
    (get-item graph :metadata)
    {
     :nodes
     (delete-items
       (graph :nodes)
       (if (coll? tags) tags [tags])
       )
     }
    (get-item graph :edges )
   )
 )



(defn add-edge [graph edges]
  (merge
    (get-item graph :metadata)
    (get-item graph :nodes)
    {
     :edges
     (add-items
       (graph :edges)
       edges
       )
     }
   )
 )


(defn delete-node-from-edges [graph node-tag]
  (filter #(contains? % node-tag) (map #(vec (% :nodes)) (vals (graph :edges))))
  )

