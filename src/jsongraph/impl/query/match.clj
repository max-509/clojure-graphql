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
   (-> {}
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
  ;(print "match-json json ") (pprint json)
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
  ;(println "graph" graph)
  ;(println "index-map" ((graph :metadata) :index))
  ;(println "labels" (get-field query-node :labels))
  (let [index-map ((graph :metadata) :index)
        labels (get-field query-node :labels)]
    (if (and (some? index-map) (some? labels))
      (match-json ;use labels index
        (select-keys (graph :adjacency)
          (intersection-by-keys index-map labels))
        query-node true)

        (match-json (graph :adjacency) query-node))))

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

; New version of matching: classical Ullmann's algorithm, but for adjacency list
; Possible improve performance for large graphs by using DualSimulation instead SimpleSimulation for labeled graphs
; For details see: https://getd.libs.uga.edu/pdfs/saltz_matthew_w_201308_ms.pdf

(defn- node-labels-props-corresponding? [qlabels qprops nlabels nprops]
  (let [qlabels-nil? (nil? qlabels)
        qlabels (if qlabels-nil? nil (into #{} qlabels))

        qprops-nil? (nil? qprops)
        qprops (if qprops-nil? nil (into #{} qprops))]
    (and
      (or qlabels-nil? (clojure.set/subset? qlabels (into #{} nlabels)))
      (or qprops-nil? (clojure.set/subset? qprops (into #{} nprops))))))

(defn match-candidates [adj query-graph]
  (let [adj-list (seq adj)]
    (apply merge (map (fn [[qnode-key qnode-value]]
                        (let [count-out-edges-qnode (count (:out-edges qnode-value))
                              labels-qnode (:labels qnode-value)
                              props-qnode (:properties qnode-value)]
                          {qnode-key (into {} (filter (fn [[adjnode-key adjnode-value]]
                                                        (and (>= (count (:out-edges adjnode-value)) count-out-edges-qnode)
                                                             (node-labels-props-corresponding? labels-qnode props-qnode
                                                                                               (:labels adjnode-value) (:properties adjnode-value))))
                                                      adj-list))}))
                      (seq query-graph)))))

(defn- child-nodes-corresponding? [qnode-out-edge-values node-candidate-children candidate-child-nodes]
  (not (some (fn [[node-cand-child-key node-cand-child-value]]
               (let [node-cand-child-labels (:labels node-cand-child-value)
                     node-cand-child-props (:properties node-cand-child-value)
                     corresponding-edge? (node-labels-props-corresponding? (:labels qnode-out-edge-values)
                                                                           (:properties qnode-out-edge-values)
                                                                           node-cand-child-labels
                                                                           node-cand-child-props)]
                 (if corresponding-edge?
                   (some (fn [[cand-child-node-key cand-child-node-value]]
                           (= cand-child-node-key node-cand-child-key))
                         (seq candidate-child-nodes))
                   false)))
             (seq node-candidate-children))))

(defn- get-candidates-by-index [Phi idx]
  (nth Phi idx))

(defn- get-candidates-by-node [Phi Phi-map node]
  (get-candidates-by-index Phi (Phi-map node)))

(defn- set-candidates-by-index [Phi index new-candidates]
  (assoc Phi index new-candidates))

(defn- set-candidates-by-node [Phi Phi-map node new-candidates]
  (set-candidates-by-index Phi (Phi-map node) new-candidates))

(defn- simple-graph-simulation [Q Phi Phi-map]
  (loop [Phi Phi
         changed true]
    (if changed
      (let [[Phi changed] (reduce (fn [[Phi changed] [u-key u-val]]
                                    (if (empty? Phi)
                                      (reduced [[] false])
                                      (let [u-adjs (:out-edges u-val)]
                                        (reduce (fn [[Phi changed] [u-adj-key u-adj-val]]
                                                  (if (empty? Phi)
                                                    (reduced [[] false])
                                                    (let [Phi_u (get-candidates-by-node Phi Phi-map u-key)]
                                                      (reduce (fn [[Phi changed] [v-key v-val]]
                                                                (let [v_ajds (:out-edges v-val)
                                                                      Phi_u_adjs (get-candidates-by-node Phi Phi-map u-adj-key)]
                                                                  (if (child-nodes-corresponding? u-adj-val v_ajds Phi_u_adjs)
                                                                    (let [Phi (set-candidates-by-node Phi Phi-map
                                                                                                      u-key (dissoc Phi_u v-key))]
                                                                      (if (empty? (get-candidates-by-node Phi Phi-map u-key))
                                                                        (reduced [[] false])
                                                                        [Phi true]))
                                                                    [Phi changed])))
                                                              [Phi changed]
                                                              (seq Phi_u)))))
                                                [Phi changed]
                                                (seq u-adjs)))))
                                  [Phi false]
                                  (seq Q))]
        (recur Phi changed))
      Phi)))

(defn- match-search [Q Phi Phi-map depth matches]
  (if (= depth (count Q))
    (cons Phi matches)
    (let [Phi_before_depth (take (+ depth 1) Phi)
          Phi_depth_v (last Phi_before_depth)]
      (reduce (fn [matches [v_key v_value]]
                (if (or
                      (= 0 depth)
                      (not (contains? (apply merge (take depth Phi_before_depth)) v_key)))
                  (let [Phi (set-candidates-by-index Phi depth {v_key v_value})
                        Phi (simple-graph-simulation Q Phi Phi-map)]
                    (if (empty? Phi)
                      matches
                      (match-search Q Phi Phi-map (inc depth) matches)))
                  matches))
              matches
              (seq Phi_depth_v)))))

(defn- make-Phi-ordered [Phi]
  (let [[Phi-map Phi] (apply map vector
                             (map-indexed (fn [idx [u candidates]]
                                            [{u idx} candidates])
                                          (into [] (seq Phi))))]
    [Phi (apply merge Phi-map)]))

(defn ullmann-match [adj query]
  (let [Phi (match-candidates adj query)
        [Phi Phi-map] (make-Phi-ordered Phi)
        Phi (simple-graph-simulation query Phi Phi-map)
        matches (match-search query Phi Phi-map 0 [])]
    (mapv (fn [founded-group]
            (apply merge (map (fn [[u-key idx]]
                                {u-key (nth founded-group idx)})
                              Phi-map)))
          matches)))
