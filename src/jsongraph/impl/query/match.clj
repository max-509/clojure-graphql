(ns jsongraph.impl.query.match
  (:require [jsongraph.impl.utils :refer :all]))

(use '[clojure.pprint :only (pprint)])

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
        (do (print (str "/Keys " (vec d) " not found/ ")) false)))))


(defn match-properties [node query-node]
  (if-let [query-prop (get-field query-node :properties)]
      (subvec? query-prop (get-field node :properties))
      true))

(defn match-labels [node query-node]
  (if-let [query-lab (get-field query-node :labels)]
      (subvec? query-lab (get-field node :labels))
      true))


;; match

(defn match-data [data query-data]
  ;(print "match-data" (get-key data) " ")
  ;(println "data") (pprint data)
  ;(println "query-data") (pprint query-data)

  ;(print-and-pass
    (and
      (match-labels data query-data)
      (match-properties data query-data)));)

(defn get-matched-edges [node query-node]
  ;(println "\n\nget-matched-edges")
  ;(print "node ") (pprint node)
  ;(print "query-node ") (pprint query-node)
  (if-let [query-out-edge (get-field query-node :out-edge)]
    (loop [out-edges (split-json (get-field node :out-edges))
           matched-edges-targets []]
      ;(print "query-out-edge ") (pprint query-out-edge)
      ;(println "matched-edges-targets-" matched-edges-targets)
      ;(println "first out-edges" (first out-edges))
      (if (empty? out-edges)
        matched-edges-targets
        (recur
          (rest out-edges)
          (if (match-data (first out-edges) query-out-edge)
            (conj matched-edges-targets [(get-key (first out-edges)) (get-key query-out-edge)])
            matched-edges-targets))))
    []))


(defn get-matched-targets [adjacency query matched-edges-targets]
  ;(println "\n\nget-matched-targets")
  ;(print "adjacency ") (pprint adjacency)
  ;(print "query ") (pprint query)
  ;(print "matched-edges-targets ") (pprint matched-edges-targets)
  (mapv first
    (filterv
      #(match-data
         (get-items adjacency (first %))
         (get-items query (second %)))
      matched-edges-targets)))

(defn get-match-answer [graph query]

  (let [adjacency (graph :adjacency)]
  (loop [nodes (split-json adjacency)
         query-nodes (split-json query)
         matched-nodes (transient {})]

    (if (empty? nodes)
      (persistent! matched-nodes)
      (recur
        (rest nodes)
        query-nodes
        (if (match-data (first nodes) (first query-nodes))
          (assoc!
             matched-nodes
             (get-key (first nodes))
             (get-matched-targets
               adjacency query
               (get-matched-edges (first nodes) query)))
          matched-nodes))))))