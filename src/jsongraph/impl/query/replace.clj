(ns jsongraph.impl.query.replace
    (:require [jsongraph.impl.utils :refer :all]))


(defn construct-template-graph [graph & [nodes]]
   (let [adjacency (graph :adjacency)
         nodes (if (some? nodes)
                 (if (subvec? nodes (keys adjacency))
                   nodes nil)
                 (keys adjacency))]
    (if (some? nodes)
      (loop [real-nodes nodes
             template (transient {})]
        (if-let [n-uuid (first real-nodes)]
          (recur
            (rest real-nodes)
            (assoc! template n-uuid
                    {:in  ((adjacency n-uuid) :in-edges)
                     :out (keysv ((adjacency n-uuid) :out-edges))}))
          (persistent! template))))))
