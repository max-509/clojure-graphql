(ns clojure-graphql.impl.query-extracter)

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
  (rest (first clause-data)))

(defn extract-variable [graph-element-data]
  (nth graph-element-data 0))

(defn extract-variable-name-data [variable]
  (let [var-name-data (rest variable)]
    (if (seq var-name-data)
      (first var-name-data)
      "")))

(defn extract-labels-data [graph-element-data]
  (rest (nth graph-element-data 1)))

(defn extract-properties-data [graph-element-data]
  (second (nth graph-element-data 2)))

(defn extract-properties-data-type [properties-data]
  (first properties-data))

(defn extract-internal-properties [properties-data]
  (rest properties-data))

(defn extract-external-properties [properties-data]
  (second properties-data))

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

(defn extract-return-params [clause-data]
  (let [return-params (rest (first clause-data))]
    (if (= (first (first return-params)) :all)
      :all
      return-params)))

(defn extract-return-param-data [return-param]
  (let [return-param-type (first (second return-param))
        return-param-val (rest (second return-param))]
    (cond
      (= :return-param-field return-param-type) {:var-name (second (first return-param-val))
                                                 :field    (keyword (second (second return-param-val)))}
      (= :return-param-var return-param-type) {:var-name (second (first return-param-val))}
      :default (throw (RuntimeException. (str "Error: Not supported return param type" (name return-param-type)))))))

(defn extract-set-params [clause-data]
  (rest (first clause-data)))

(defn extract-set-param-data [set-param]
  (second set-param))

(defn extract-set-param-data-command [set-param-data]
  (first set-param-data))

(defn extract-set-param-data-params [set-param-data]
  (rest set-param-data))

(defn extract-set-param-var-name [set-param-data-params]
  (second (first set-param-data-params)))

(defn extract-set-param-assign-field [set-param-assign]
  (keyword (second (second set-param-assign))))

(defn extract-set-param-assign-value [set-param-assign]
  (nth set-param-assign 2))

(defn extract-set-param-assign-value-type [set-param-assign-value]
  (first set-param-assign-value))

(defn extract-set-param-assign-value-val [set-param-assign-value]
  (second set-param-assign-value))

(defn extract-set-param-labels-data [set-param-labels]
  (rest (second set-param-labels)))
