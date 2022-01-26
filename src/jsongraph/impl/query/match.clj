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


(defn match-properties? [node query-node]
  (if-let [query-prop (get-field query-node :properties)]
      (subvec? query-prop (get-field node :properties))
      true))

(defn match-labels? [node query-node]
  (if-let [query-lab (get-field query-node :labels)]
      (subvec? query-lab (get-field node :labels))
      true))


;; match
;& [labels-checked?]     (if (boolean labels-checked?) true  ;labels?

(defn match-data? [data query-data & [labels-checked?] ]
  ;(print "match-data" (get-key data) " ")
  ;(println "data") (pprint data)
  ;(println "query-data") (pprint query-data)

  ;(print-and-pass
    (and
      (= (count data) (count query-data))
      (if (boolean labels-checked?) true  ;labels?
         (match-labels? data query-data))
      (match-properties? data query-data)));)

(defn match-json [json query-data & [labels-checked?]]
  (if (empty? json) '()
   (let [ks (keys json)]
     ;(println "labels-checked?" labels-checked?)
    ;(vec
    (if (nil? query-data) ks
       (filter #(match-data? (select-keys json [%]) query-data labels-checked?) ks)))));)

(defn get-matched-arrows [adjacency source query-edge query-node-target]
  ;(println "get-matched-arrows")
  ;(print "query-edge ") (println query-edge)
  ;(print "source ") (println source)
  ;(println "source-edges") (pprint ((adjacency source) :out-edges) )
  (let [targets (match-json ((adjacency source) :out-edges) query-edge)]
     (match-json (select-keys adjacency targets) query-node-target)))

(defn get-matched-nodes [graph query-node]
  (if-let [index-map ((graph :metadata) :index)]
    (match-json ;use labels index
      (select-keys (graph :adjacency)
          (intersection-by-keys index-map (get-field query-node :labels)))
      query-node true)
    (match-json (graph :adjacency) query-node)))

(defn get-matched-adj-edges [graph query-node-source q-edge-data query-node-target]
  ;(println "query-node-source") (pprint query-node-source)
  ;(println "graph") (pprint graph)
  ;(println)
  ;(println "q-edge-data")(pprint q-edge-data)
  ;(println "query-node-target") (pprint query-node-target)
  (loop [sources (get-matched-nodes graph query-node-source)
         matched-edges (transient {})]
    ;(print "matched-edges: ") (println matched-edges)
    (if-let [source (first sources)]
      (recur (rest sources)
             (let [arrows (get-matched-arrows (graph :adjacency) source q-edge-data query-node-target)]
               (if (empty? arrows) matched-edges
                 (assoc! matched-edges source arrows))))
      (persistent! matched-edges))))


(defn match-adj-edges-list [adjacency query]
  ;(println "match-edges-query")
  ;(pprint query)
  ;(println "adjacency-to-edges-data") (pprint (adjacency-to-edges-data query))
  (map
    #(get-matched-adj-edges adjacency
        (select-keys query [(get-edge-source %)])
        {(get-edge-target %) (get-edge-data %)}
        (select-keys query [(get-edge-target %)]))
    (adjacency-to-edges-data query)))

(defn merge-tail-ways [adj-edges conj-adj-edges]
 (add-items {} (map (fn [[k v]] {k (merge-by-keys conj-adj-edges v)}) adj-edges)))

(defn loop? [adjacency loop-data n-index]
  (match-data? (((adjacency n-index) :out-edges) n-index) loop-data))

(defn get-matched-single-node [graph query-sn]
  (let [nodes (get-matched-nodes graph query-sn)
        loop-data (get-field query-sn :out-edges)]
    (if (empty? loop-data) nodes
      (map #(list % %) (filter #(loop? (graph :adjacency) loop-data %) nodes)))))

(defn get-matched-ways [graph query]
  (case (count (keys query))
    0 (println "empty query")                           ; deep = 0
    1 (map wrap (get-matched-single-node graph query))  ; deep = 1
    (filter                                             ; deep > 1
      #(= (count (keys query)) (count %))
      (merge-by-keys
        (loop [adj-edges-list (match-adj-edges-list graph query)
               ways (conj-key-in-vals (first adj-edges-list))]
          (if (some? (second adj-edges-list))
            (recur
              (rest adj-edges-list)
              (conj-key-in-vals (merge-tail-ways (second adj-edges-list) ways)))
             ways))))))

; IDEA: do universal map ways to json like format
; for node it is just set {:A :B} (interpreted as list)
; adj-edges format usage only when ways are represent edges
; what about ((:B :C :A) (:A :C :B)) ways
(defn ways-to-adj-edges [ways]
  "edge ways is list of two elements list items
   example ((:A :B) (:A :C) (:C :A))
   adj-edges example
   {:A (:B :C)
    :C (:A)}"
  (if (= (count (first ways)) 2)
    (assoc-items ways)
    (println "ways is not edge ways")))
