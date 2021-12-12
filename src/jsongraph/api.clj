(ns jsongraph.api
      (:require [jsongraph.impl.core :refer :all]
                [jsongraph.impl.utils :refer [get-key, get-items, add-items]]
                [clj-uuid :as uuid])
  )


(defn gen-node
  [labels properties & [index]]
   {(if index index (uuid/v4)) (gen-adjacency-item [] {} labels properties)}
   )

(defn index [node]
  (get-key node)
  )

(defn add-nodes [graph nodes]
  (merge
    (get-items graph :metadata)
    (apply-to-adjacency graph add-items nodes)
   )
 )

(defn gen-edge
  [
   source target
   labels properties
   ]
 [[(index source) (index target)]
   {:labels labels :properties properties}])

(defn add-edges [graph edges]
  (merge
    (get-items graph :metadata)
    (apply-to-adjacency
      graph add-out-edges! edges)))



(defn create-graph
  ([] (gen-empty-graph))
  ([nodes] (add-nodes (gen-empty-graph) nodes))
  ([nodes edges]
   (-> (gen-empty-graph)
       (add-nodes nodes)
       (add-edges edges)))
  )