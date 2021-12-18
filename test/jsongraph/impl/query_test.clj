(ns jsongraph.impl.query-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.query :refer :all]
            [jsongraph.api :refer [gen-node]]))

(def prop {:money 100 :age 21 :weight 50})
;(def query-prop {:money 100 :age 21})

(def query-where-matched {:money [:gt 70] :age [:lt 30] :weight [:eq 50]})

(def node (gen-node [:lab-A] prop :A))

(def query-node-matched (gen-query-node [:lab-A] {} query-where-matched))
(def query-node-matched-2 (gen-query-node [:lab-A] {}
                               {:age [:gt 18] :weight [:ne 100]}))

(def query-node-no-matched         (gen-query-node [:lab-A] {}
                                        {:money [:lt 70] :age [:gt 30] :weight [:eq 50]}))
(def query-node-no-matched-unknown (gen-query-node [:lab-A] {}
                                        {:age [:gt 18] :cost [:eq 45]}))
(def query-node-no-matched-lab (gen-query-node [:lab-B] {} query-where-matched))


(deftest match-node-where-properties-test
  (testing "match")
  (is (match-node-where-properties node query-node-matched))
  (is (match-node-where-properties node query-node-matched-2))
  (testing "no match")
  (is (not (match-node-where-properties node query-node-no-matched)))
  (testing "unknown keys")
  (is (not (match-node-where-properties node query-node-no-matched-unknown)))
  )

(deftest match-node-test
  (testing "match")
  (is (match-node node query-node-matched))
  (testing "no match")
  (is (not (match-node node query-node-no-matched-lab)))
  (is (not (match-node node query-node-no-matched)))
  )