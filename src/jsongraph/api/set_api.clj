(ns jsongraph.api.set-api
  (:require [jsongraph.impl.query.set :refer [set_impl]]))

(defn SET [entities-for-change graph]
  (reduce (fn [updated-graph entity]
            (let [way [(first entity)]
                  query (second entity)]
              (assoc updated-graph
                :adjacency (set_impl (:adjacency updated-graph) way query))))
          graph
          entities-for-change))
