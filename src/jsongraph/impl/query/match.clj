(ns jsongraph.impl.query.match
  (:require [jsongraph.impl.utils :refer :all]
            [jsongraph.impl.graph :refer [adjacency-to-edges-data
                                          get-edge-source get-edge-target
                                          get-edge-data gen-edge
                                          add-out-edges!]]))

(use '[clojure.pprint :only (pprint)])

(defn single-edge-query
  ([n-source n-target
   e-labels e-properties]
  (->  {}
       (add-items [n-source n-target])
       (add-out-edges!
         [(gen-edge
            (get-key n-source) (get-key n-target)
            e-labels e-properties)])))
  ([n-source n-target edge-data]
    (single-edge-query
      n-source n-target
      ((second edge-data) :labels)
      ((second edge-data) :properties))))




;; match utils


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
  ;(println "get-matched-arrows")
  ;(print "query-edge ") (println query-edge)
  ;(print "source ") (println source)
  ;(println "source-edges") (pprint ((adjacency source) :out-edges) )
  (let [targets (match-json ((adjacency source) :out-edges) query-edge)]
     (match-json (select-keys adjacency targets) query-node-target)))

(defn get-matched-nodes [adjacency query-node]
  (match-json adjacency query-node))

(defn get-matched-adj-edges [adjacency query-node-source q-edge-data query-node-target]
  ;(println "query-node-source") (pprint query-node-source)
  ;(println "adjacency") (pprint adjacency)
  ;(println)
  ;(println "q-edge-data")(pprint q-edge-data)
  ;(println "query-node-target") (pprint query-node-target)
  (loop [sources (get-matched-nodes adjacency query-node-source)
         matched-edges (transient {})]
    ;(print "matched-edges: ") (println matched-edges)
    (if-let [source (first sources)]
      (recur (rest sources)
             (let [arrows (get-matched-arrows adjacency source q-edge-data query-node-target)]
               (if (empty? arrows) matched-edges
                 (assoc! matched-edges source arrows))))
      (persistent! matched-edges))))


(defn match-adj-edges-list [adjacency query]
  ;(println "match-edges-query")
  ;(pprint query)
  ;(println "adjacency-to-edges-data") (pprint (adjacency-to-edges-data query))
  (map
    #(get-matched-adj-edges adjacency
        (get-item query (get-edge-source %))
        {(get-edge-target %) (get-edge-data %)}
        (get-item query (get-edge-target %)))
    (adjacency-to-edges-data query)))

(defn merge-by-keys [adj & [-keys]]
 (apply concat (select-vals adj (if (some? -keys) -keys (keys adj)))))

(defn merge-tail-ways [adj-edges conj-adj-edges]
 (add-items {} (map (fn [[k v]] {k (merge-by-keys conj-adj-edges v)}) adj-edges)))


;(merge-by-keys (conj-key-in-vals adj-edges)) deep = 1
;(let [[adj-edges-1 adj-edges-2] (match-adj-edges-list adjacency query)] deep = 2
;  (merge-by-keys (conj-key-in-vals (merge-tail-ways adj-edges-1 (conj-key-in-vals adj-edges-2)))))

(defn get-matched-ways [adjacency query]
  (case (count (keys query))
    0 (println "empty query")                               ; deep = 0
    1 (map wrap (get-matched-nodes adjacency query))        ; deep = 1
    (merge-by-keys                                          ; deep > 1
        (loop [adj-edges-list (match-adj-edges-list adjacency query)
               ways (conj-key-in-vals (first adj-edges-list))]
          (if (some? (second adj-edges-list))
            (recur
              (rest adj-edges-list)
              (conj-key-in-vals (merge-tail-ways (second adj-edges-list) ways)))
             ways)))))