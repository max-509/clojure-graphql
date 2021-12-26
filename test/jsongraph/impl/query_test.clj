(ns jsongraph.impl.query-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.query :refer :all]
            [jsongraph.api.graph-api :refer [gen-node gen-edge create-graph get-nodes-from-graph]]
            [jsongraph.api.match-api :refer :all]))
(use '[clojure.pprint :only (pprint)])

(def prop-A {:money 100 :age 21 :weight 50})
(def prop-B {:money 380 :age 30 :weight 64})
(def prop-C {:money 270 :age 18 :weight 48})

(def nA  (gen-node [:lab-A] prop-A :A))
(def nB  (gen-node [:lab-B] prop-B :B))
(def nC  (gen-node [:lab-C] prop-C :C))

(def prop-edge-AB {:cost 68 :danger 10})
(def edge-AB (gen-edge nA nB nil prop-edge-AB))

(def prop-edge-AC {:cost 56 :danger 3})
(def edge-AC (gen-edge nA nC nil prop-edge-AC))

(def prop-edge-CB {:cost 40 :danger 5})
(def edge-CB (gen-edge nC nB nil prop-edge-CB))


(def graph-empty (create-graph))
(def graph-nA (create-graph [nA]))
(def graph-nA-nB (create-graph [nA nB]))
(def graph-with-edge (create-graph [nA nB] [edge-AB]))
(def graph-with-edges (create-graph [nA nB nC] [edge-AB edge-AC edge-CB]))

(def nodes (get-nodes-from-graph graph-with-edges))


(def query-where-matched {:money [:gt 70] :age [:lt 30] :weight [:eq 50]})
(def query-where-no-matched {:money [:lt 70] :age [:gt 30] :weight [:eq 50]})
(def query-where-on-matched-unknown {:age [:gt 18] :cost [:eq 45]})

(def query-node-matched   (gen-query-node [:lab-A] {} query-where-matched))
(def query-node-matched-2 (gen-query-node [:lab-A] {} {:age [:gt 18] :weight [:ne 100]}))

(def query-node-no-matched         (gen-query-node [:lab-A] {} query-where-no-matched))
(def query-node-no-matched-unknown (gen-query-node [:lab-A] {} query-where-on-matched-unknown))
(def query-node-no-matched-lab     (gen-query-node [:lab-B] {} query-where-matched))

(def query-node-with-any-where (gen-query-node [:lab-A] {} nil))


(def query-node-matched-A    (gen-query-node [:lab-A] {} query-where-matched))
(def query-node-no-matched-A (gen-query-node [:lab-A] {} query-where-no-matched))


(def query-edge (gen-query-edge nil nil {:cost [:lt 75] :danger [:gt 5]}))
(def query-edge-no-mch (gen-query-edge nil nil {:cost [:lt 0] :danger [:gt 100]}))
(def query-edge-any (gen-query-edge))


(def query-node-matched-B (gen-query-node [:lab-B] {}
                                          {:money [:gt 200] :age [:gt 25] :weight [:ne 60]}))
(def query-node-no-matched-B (gen-query-node [:lab-B] {}  query-where-on-matched-unknown))


(def query-node-matched-any    (gen-query-node nil {} nil))


(def query-node-with-edge-matched-A-B
  (add-edge-into-query-node query-node-matched-A query-edge query-node-matched-B))

(def query-node-with-edge-matched-A-any
  (add-edge-into-query-node query-node-matched-A query-edge-any query-node-matched-any))

(def query-node-with-edge-matched-any-any
  (add-edge-into-query-node query-node-matched-any query-edge-any query-node-matched-any))

(def query-node-with-edge-no-matched-A
  (add-edge-into-query-node query-node-no-matched-A query-edge-any query-node-matched-B))
(def query-node-with-edge-no-matched-B
  (add-edge-into-query-node query-node-matched-A query-edge-any query-node-no-matched-B))

(def query-node-with-edge-matched-edge-A-B
  (add-edge-into-query-node query-node-matched-A query-edge-no-mch query-node-matched-B))


(def nodes (get-nodes-from-graph graph-with-edges))


(deftest match-node-where-properties-test
  (testing "match")
  (is (match-where-properties nA query-node-matched))
  (is (match-where-properties nA query-node-matched-2))
  (is (match-where-properties nA query-node-with-any-where))
  (testing "no match")
  (is (not (match-where-properties nA query-node-no-matched)))
  (testing "unknown keys")
  (is (not (match-where-properties nA query-node-no-matched-unknown)))
  )

(deftest match-node-test
  (testing "match")
  (is (match-node nA query-node-matched))
  (is (match-node nA query-node-with-any-where))
  (testing "no match")
  (is (not (match-node nA query-node-no-matched-lab)))
  (is (not (match-node nA query-node-no-matched)))
  )

(deftest get-matched-edges-test
  (pprint (first nodes))
  (pprint query-node-with-edge-matched-A-any)
  (println (get-matched-edges (first nodes) query-node-with-edge-matched-A-any)))