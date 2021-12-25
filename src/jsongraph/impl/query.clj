(ns jsongraph.impl.query
  (:require [jsongraph.impl.utils :refer :all]
            [clj-uuid :as uuid]))

(use '[clojure.pprint :only (pprint)])

;;generators
(defn gen-query-edge
  [target labels properties & [where]]
  {target
   {:labels     labels
    :properties properties
    :where      where}})

(defn gen-query-node
  [labels properties where & [index]]
  {(if index index (uuid/v0))
     {:out-edge   nil
      :labels     labels
      :properties properties
      :where      where}})


(defn add-edge-into-query-node [query-node query-edge query-target-node]
  (merge
    (assoc-in query-node [(get-key query-node) :out-edge] query-edge)
         query-target-node))


;;comparison signs map
(def cs-map
  {
   :eq =  :ne not=
   :gt >  :lt <
   :ge >= :le <=
   }
  )

(defn query-where-cs [query-prop-item]
  (first (second query-prop-item)))

(defn query-where-val [query-prop-item]
  (second (second query-prop-item)))

(defn query-where-key [query-prop-item]
    (first query-prop-item))


;; match utils

(defn match-each-where [node-prop query-where]
  (every?
    #((cs-map (query-where-cs %))
      (node-prop (query-where-key %))
      (query-where-val %))
    (get-items query-where)))


(defn match-where-properties [node query-node]
  (let [node-prop   (get-field node :properties)
        query-where (get-field query-node :where)  ; maybe nil
        d (list-difference (keys query-where) (keys node-prop))]
    (if (nil? d) true  ; query-where is any
      (if (empty? d)
        (match-each-where node-prop query-where)
        (do (println (str "Keys " (vec d) " not found")) false)))))


(defn match-properties [node query-node]
  (if-let [query-prop (get-field query-node :properties)]
      (empty? (json-difference
                query-prop (get-field node :properties)))
      true))

(defn match-labels [node query-node]
  (if-let [query-lab (get-field query-node :labels)]
      (empty? (list-difference
                query-lab (get-field node :labels)))
      true))


;; match

(defn match-node [node query-node]
  ;(print "match-node" (get-key node) " ")
  ;(print-and-pass
    (and
      (match-labels node query-node)
      ;(match-properties node query-node)
      (match-where-properties node query-node)));)


(defn match-edge [node-out-edge query-out-edge]
  ;(print "match-edge" node-out-edge query-out-edge" ")
  ;(print-and-pass
  (and
    (match-labels node-out-edge query-out-edge)
    (match-where-properties node-out-edge query-out-edge)));)


(defn get-matched-edges [node query-node]
  ;(println "get-matched-edges")
  (if-let [query-out-edge (get-field query-node :out-edge)]

    (loop [out-edges (split-json (get-field node :out-edges))
           matched-edges-targets []]
      ;(println "matched-edges-targets" matched-edges-targets)
      ;(println "first out-edges" (first out-edges))
      (if (empty? out-edges)
        matched-edges-targets
        (recur
          (rest out-edges)
          (if (match-edge (first out-edges) query-out-edge)
            (conj matched-edges-targets (get-key (first out-edges)))
            matched-edges-targets))))
    true))


(defn get-matched-targets [adjacency query matched-edges-targets]
  (filterv
    #(match-node (get-items adjacency %) (get-items query %))
    matched-edges-targets
    )
  )

(defn match-query [graph query]

  (let [adjacency (graph :adjacency)]
  (loop [nodes (split-json adjacency)
         query-nodes (split-json query)
         matched-nodes (transient {})]

    (if (empty? nodes)
      (persistent! matched-nodes)
      (recur
        (rest nodes)
        query-nodes
        (if (match-node (first nodes) (first query-nodes))
          (assoc!
             matched-nodes
             (get-key (first nodes))
             (get-matched-targets
               adjacency query
               (get-matched-edges (first nodes) query)))
          matched-nodes))))))
