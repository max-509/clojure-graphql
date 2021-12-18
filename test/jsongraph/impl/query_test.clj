(ns jsongraph.impl.query-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.query :refer :all]
            [jsongraph.api :refer [gen-node]]))

(def prop {:money 100 :age 21 :weight 50})
(def query-prop {:money [:gt 300] :age [:lt 30] :weight [:eq 50]})

(def node (gen-node [:lab-A] prop :A))
(def query-node (gen-query-node [:lab-A] query-prop))

(deftest match-node-where-properties-test
  (println (match-node-where-properties prop query-prop))
  )

(deftest match-node-properties-test
  (println (match-node-properties node query-node))
  )