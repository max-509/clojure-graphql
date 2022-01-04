(ns clojure-graphql.impl.query_processing.where-processing
  (:require [clojure-graphql.impl.predicates-extracter :as pextr])
  (:require [clojure-graphql.impl.lang2cloj :as l2cloj]))

(use '[clojure.pprint :only (pprint)])

(def type-of-op
  {:negation-command :unary-op
   :and-command      :binary-op
   :or-command       :binary-op
   :xor-command      :binary-op})

(defn get-type-of-op [op]
  (get type-of-op op))

(defn above-priority? [op1 op2]
  (if (= :left-bracket op2)
    false
    (if (= :negation-command op2)
      true
      (if (and (= :and-command op2) (not= :negation-command op1))
        true
        (not (and (or (= :or-command op2) (= :xor-command op2)) (= :and-command op1)))))))

(defn add-op-to-rpn [rpn op]
  (conj rpn [(get-type-of-op op) op]))

(defn add-pred-to-rpn [rpn pred]
  (conj rpn [:pred pred]))

(defn rpn-expr-predicate? [expr]
  (= :pred (first expr)))

(defn rpn-expr-unary-op? [expr]
  (= :unary-op (first expr)))

(defn rpn-expr-binary-op? [expr]
  (= :binary-op (first expr)))

(defn rpn-expr-value [expr]
  (second expr))

(defn processing-token-binary-operator [op-stack rpn bin-op]
  (loop [op-stack op-stack
         rpn rpn
         bin-op bin-op]
    (if (empty? op-stack)
      [op-stack (add-op-to-rpn rpn bin-op)]
      (let [last-op (first op-stack)]
        (if (above-priority? bin-op last-op)
          (recur (rest op-stack) (add-op-to-rpn rpn last-op) bin-op)
          [(cons bin-op op-stack) rpn])))))

(defn pop-ops-while-no-left-bracket [op-stack rpn]
  (loop [op-stack op-stack
         rpn rpn]
    (if (empty? op-stack)
      (throw (RuntimeException. "Error: cannot find left bracket"))
      (let [op (first op-stack)]
        (if (= :left-bracket op)
          [(rest op-stack) rpn]
          (recur (rest op-stack) (add-op-to-rpn rpn op)))))))

(defn inf-predicates-to-rpn
  ([predicates] (inf-predicates-to-rpn predicates (list :left-bracket) []))
  ([predicates op-stack rpn]
   (pprint "inf-predicates-to-rpn")
   (pprint predicates)
   (pprint op-stack)
   (pprint rpn)
   (let [[new-op-stack new-rpn] (reduce
                                  (fn [[op-stack rpn] token]
                                    (if (pextr/predicate? token)
                                      (let [predicate (pextr/extract-token-value token)
                                            predicate-value (pextr/extract-predicate-value predicate)
                                            predicate-expression (pextr/extract-predicate-expression predicate-value)
                                            [op-stack rpn] (if (pextr/negation-predicate-value? predicate-value)
                                                             [(cons :negation-command op-stack) rpn]
                                                             [op-stack rpn])]
                                        (if (pextr/brackets-predicates? predicate)
                                          (let [[op-stack rpn] [(cons :left-bracket op-stack) rpn]]
                                            (inf-predicates-to-rpn predicate-expression op-stack rpn))
                                          [op-stack (add-pred-to-rpn rpn predicate-expression)]))
                                      (let [bin-op (pextr/extract-token-value token)]
                                        (processing-token-binary-operator op-stack rpn bin-op))))
                                  [op-stack rpn]
                                  (pextr/extract-tokens predicates))]
     (pop-ops-while-no-left-bracket new-op-stack new-rpn))))

(defmulti check-processing (fn [check] (pextr/extract-check-type check)))
(defmethod check-processing :field-check [check]
  (let [field-check-value (pextr/extract-check-value check)
        var-name (pextr/extract-field-check-var-name field-check-value)
        field-name (keyword (pextr/extract-field-check-field-name field-check-value))
        command (pextr/extract-field-check-command field-check-value)
        comp-value (l2cloj/convert-prop-value (pextr/extract-field-check-comp-value field-check-value))]
    [:pred
     {:type :field-check
      :val  [{:name var-name :property field-name} (l2cloj/convert-command command) comp-value]}]))
(defmethod check-processing :label-check [check]            ;TODO: for future
  nil)
(defmethod check-processing :pattern-check [check]          ;TODO: for future
  nil)

(defn operator-processing [operator]
  [(get-type-of-op operator) (l2cloj/convert-operator operator)])

(defn rpn-predicates-to-bin-expr-tree [rpn]
  (pprint "rpn")
  (pprint rpn)
  (let [trees-stack (reduce (fn [trees-stack expr]
                              (let [expr-value (rpn-expr-value expr)]
                                (cond
                                  (rpn-expr-predicate? expr) (let [check-value (check-processing expr-value)]
                                                               (cons (list check-value) trees-stack))
                                  (rpn-expr-unary-op? expr) (let [trees-stack-head (first trees-stack)
                                                                  op-value (operator-processing expr-value)]
                                                              (cons (list op-value trees-stack-head) (rest trees-stack)))
                                  (rpn-expr-binary-op? expr) (let [l-operand (first trees-stack)
                                                                   r-operand (second trees-stack)
                                                                   op-value (operator-processing expr-value)]
                                                               (cons (list op-value l-operand r-operand) (rest (rest trees-stack))))
                                  :default (throw (RuntimeException. "Error: Not supporting type of operation")))))
                            (list)
                            rpn)]
    (if (empty? (rest trees-stack))
      (first trees-stack)
      (throw (RuntimeException. "Error: Stack with tree's expression must has one element at end, bad rpn-expression")))))

(defn where-processing [predicates]
  (pprint "where")
  (pprint predicates)
  (if (empty? predicates)
    nil
    (let [[op-stack rpn] (inf-predicates-to-rpn predicates)]
      (if (seq op-stack)
        (throw (RuntimeException. "Error: Stack operations not empty - infix expression is bad"))
        (rpn-predicates-to-bin-expr-tree rpn)))))
