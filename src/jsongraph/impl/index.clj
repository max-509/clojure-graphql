(ns jsongraph.impl.index
  (:require [clj-uuid :as uuid]
            [jsongraph.impl.utils :refer [wrap assoc-items delete-items keysSet
                                          gen-json-by-keys merge-by-keys list-intersection]]))

(def uuid-v0 (uuid/v0))

(defn add-node-in-index-map [adjacency node-index index-map]
  (let [labels ((adjacency node-index) :labels)]
    (if (empty? labels)
      (assoc index-map uuid-v0 (conj (index-map uuid-v0) node-index))
      (assoc-items (map #(list % node-index) labels) index-map))))

(defn get-index-map [adjacency labels]
  (loop [n-indexes (keys adjacency)
         index-map (gen-json-by-keys (conj labels uuid-v0) '())]
     (if (empty? n-indexes)
       index-map
       (recur (rest n-indexes)
         (add-node-in-index-map adjacency (first n-indexes) index-map)))))

(defn add-labels-in-index [graph labels]
  (let [index-map (get-index-map (graph :adjacency) (wrap labels))
        metadata (graph :metadata)]
    (if (empty? index-map) metadata
      (assoc metadata :index (merge metadata index-map)))))

(defn delete-labels-in-index [metadata labels]
  (if (nil? labels) (dissoc metadata :index)
    (if (empty? labels) metadata
     (let [index-map (metadata :index {})]
          (if (= (disj (keysSet index-map) uuid-v0) (set labels))
            (dissoc metadata :index)     ;uuid-v0 key only
            {:index
             (assoc (delete-items index-map labels) uuid-v0
               (concat (merge-by-keys index-map labels) (index-map uuid-v0)))})))))
