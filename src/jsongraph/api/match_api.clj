(ns jsongraph.api.match-api
  (:require [jsongraph.impl.utils :refer :all]
            [jsongraph.impl.query.match :refer :all]
            [clj-uuid :as uuid]))

;;generators

(defn gen-query-data
  ([]
   {(uuid/v4)
   {:out-edge   nil
    :labels     nil
    :properties nil}})
  ([labels properties & [index]]
  {(if index index (uuid/v4))
     {:out-edge   nil
      :labels     labels
      :properties properties}}))

(defn node-to-query-data [node & [index]]
  (gen-query-data
    (get-field node :labels)
    (get-field node :properties)
    index))

(defn add-edge-into-query-node [query-node query-edge query-target-node]
  ;(println "query-node index" (get-key query-node))
  (merge
    (assoc-in query-node [(get-key query-node) :out-edge] query-edge)
         {(get-key query-edge) (get-val query-target-node)}))


; match