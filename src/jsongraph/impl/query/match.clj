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
      (= (count data) (count query-data))
      (match-labels data query-data)
      (match-properties data query-data)));)

(defn match-json [json query-data]
  (let [ks (keys json)]
    ;(vec
      (if (nil? query-data) ks
      (filter #(match-data (get-item json %) query-data) ks))));)

(defn get-matched-arrows [adjacency source query-edge query-node-target]
   (let [targets (match-json ((adjacency source) :out-edges) query-edge)]
     (match-json (select-keys adjacency targets) query-node-target)))

(defn get-matched-nodes [adjacency query-node]
  (match-json adjacency query-node))

(defn get-matched-edges [adjacency query-node-source q-edge-data query-node-target]
  (loop [sources (get-matched-nodes adjacency query-node-source)
         matched-edges (transient {})]
    (if-let [source (first sources)]
       (recur (rest sources)
              (assoc! matched-edges source
                      (get-matched-arrows adjacency source q-edge-data query-node-target)))
       (persistent! matched-edges))))

(defn get-matched-query [adjacency query]
  (let [[qnS qnT] (split-json query)   ;Node and edge only
        query-edge (get-field qnS :out-edges)]
    (if (nil? qnT) (get-matched-nodes adjacency qnS)
      (get-matched-edges adjacency qnS query-edge qnT))))


(defn get-edges-by-match-adj-item [adjacency match-adj-item]
  (map #(merge
          (get-item adjacency (get-key match-adj-item))
          (get-item adjacency %))
    (get-val match-adj-item)))