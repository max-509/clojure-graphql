(ns jsongraph.api
      (:require [jsongraph.impl.core :refer :all]
                [jsongraph.impl.utils :refer [get-key]]
                [clj-uuid :as uuid])
  )


(defn gen-node
  [labels properties & [index]]
   {(if index index (uuid/v4)) (gen-adjacency-item [] {} labels properties)}
   )

(defn index [node]
  (get-key node)
  )

(defn gen-edge
  [
   source target
   labels properties
   ]
 [[(index source) (index target)]
   {:labels labels :properties properties}])



(defn create-graph
  ([] (gen-empty-graph))
  ([nodes] (add-node (gen-empty-graph) nodes))
  ([nodes edges]
   (-> (gen-empty-graph)
     (add-node nodes)
     (add-edge edges)))
  )