(ns clojure-graphql.impl.query_extracter)

; With postfix data - args type of [:elem data]
; Without postfix data - args type of [data]

(defn extract-clauses [query]
  (rest query))

(defn extract-clause-name [clause]
  (first (second clause)))

(defn extract-clause-data [clause]
  (rest (second clause)))

(defn extract-patterns [clause-params]
  (rest (first clause-params)))

(defn extract-pattern-data [pattern]
  (rest pattern))

(defn extract-first-pattern-elem [pattern-data]
  (first pattern-data))

(defn extract-second-pattern-elem [pattern-data]
  (second pattern-data))

(defn extract-rest-pattern-elems [pattern-data]
  (rest pattern-data))

(defn extract-node-data [node]
  (rest node))

(defn extract-relation-data [relation]
  (rest relation))

(defn extract-left-dash [relation-data]
  (first (nth relation-data 0)))

(defn extract-edge [relation-data]
  (nth relation-data 1))

(defn extract-edge-data [edge]
  (rest edge))

(defn extract-right-dash [relation-data]
  (first (nth relation-data 2)))

(defn extract-variables [clause-data]
  (rest clause-data))

(defn extract-variable [graph-element-data]
  (nth graph-element-data 0))

(defn extract-variable-name-data [variable]
  (let [var-name-data (rest variable)]
    (if (seq var-name-data)
      (first var-name-data)
      nil)))

(defn extract-labels-data [graph-element-data]
  (rest (nth graph-element-data 1)))

(defn extract-properties-data [graph-element-data]
  (rest (nth graph-element-data 2)))

(defn extract-label-data [label]
  (second label))

(defn extract-property-data [property]
  (rest property))

(defn extract-property-key-data [property-data]
  (second (nth property-data 0)))

(defn extract-property-val [property-data]
  (nth property-data 1))

(defn extract-property-val-type [val]
  (first val))

(defn extract-property-val-data [val]
  (let [val-data (rest val)
        val-type (extract-property-val-type val)]
    (if (= val-type :list)
      val-data
      (first val-data))))

(defn extract-predicates [clause-params]
  (second (second clause-params)))