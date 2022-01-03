(ns jsongraph.impl.query.match-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.query.match :refer :all]
            [jsongraph.api.graph-api :refer [gen-node gen-edge create-graph get-nodes-from-graph]]
            [jsongraph.api.match-api :refer :all]))
(use '[clojure.pprint :only (pprint)])

(def prop-A {:money 100 :age 21 :weight 50})
(def prop-B {:money 380 :age 30 :weight 64})
(def prop-C {:money 270 :age 18 :weight 48})

(def prop-unknown  {:money 100 :cost 21})
(def prop-A-no-mch {:money 100 :age 40 :weight 50})
(def prop-B-no-mch {:money 380 :age 30 :weight 70})

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


;(def query-where-matched    {:money [:gt 70] :age [:lt 30] :weight [:eq 50]})
;(def query-where-no-matched {:money [:lt 70] :age [:gt 30] :weight [:eq 50]})
;(def query-where-on-matched-unknown {:age [:gt 18] :cost [:eq 45]})

(def query-node-matched   (gen-query-data [:lab-A] prop-A))
(def query-node-matched-2 (gen-query-data [:lab-A] prop-A))

(def query-node-no-matched         (gen-query-data [:lab-A] prop-C))
(def query-node-no-matched-unknown (gen-query-data [:lab-A] prop-unknown))
(def query-node-no-matched-lab     (gen-query-data [:lab-B] prop-B))

(def query-node-with-any-data (gen-query-data [:lab-A] nil nil))


(def query-node-matched-A    (gen-query-data [:lab-A] prop-A))
(def query-node-no-matched-A (gen-query-data [:lab-A] prop-A-no-mch))


(def query-edge-AB (gen-query-data nil prop-edge-AB))
(def query-edge-no-mch (gen-query-data nil (assoc prop-edge-AB :cost -1)))
(def query-edge-any (gen-query-data))


(def query-node-matched-B (gen-query-data [:lab-B] prop-B))
(def query-node-no-matched-B (gen-query-data [:lab-B] prop-B-no-mch))


(def query-node-matched-any    (gen-query-data nil nil nil))

(def query-node-with-edge-matched-A-B
  (add-edge-into-query-node query-node-matched-A query-edge-AB query-node-matched-B))

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


(deftest match-node-test
  (testing "match")
  (is (match-data nA query-node-matched))
  (is (match-data nA query-node-with-any-data))
  (testing "no match")
  (is (not (match-data nA query-node-no-matched-lab)))
  (is (not (match-data nA query-node-no-matched)))
  )

(deftest get-matched-edges-from-node-test
  (pprint (first nodes))
  (pprint query-node-with-edge-matched-A-any)
  (println (get-matched-edges (first nodes) query-node-with-edge-matched-A-any)))


(deftest match-query-graph-wo-edges-test
  (println "Format: match-answer")
  (testing "match") (println "match")
  (println (get-match-answer graph-nA query-node-matched-A))
  (println (get-match-answer graph-nA-nB query-node-matched-any))
  (testing "no match") (println "no match")
  (println (get-match-answer graph-empty query-node-matched-A))
  (println (get-match-answer graph-empty query-node-matched-any)))


(deftest match-query-graph-with-edges-test
  (println "Format: match-answer")
  (testing "match")(println "Match")
  (print " Any nodes\t\t\t\t   ") (println (get-match-answer graph-with-edge query-node-matched-any))
  (print " Source in graph with edge ") (println (get-match-answer graph-with-edge query-node-matched-A))
  (print " Full edge\t\t\t\t   ") (println (get-match-answer graph-with-edge query-node-with-edge-matched-A-B))
  (print " Any nodes in big graph\t   ") (println (get-match-answer graph-with-edges query-node-matched-any))
  (print " Source to any\t\t\t   ") (println (get-match-answer graph-with-edges query-node-with-edge-matched-A-any))
  (print " Any to any\t\t\t\t   ") (println (get-match-answer graph-with-edges query-node-with-edge-matched-any-any))
  (testing "no match") (println "NO Match")
  (print " Source\t ") (println (get-match-answer graph-with-edge query-node-with-edge-no-matched-A))
  (print " Target\t ") (println (get-match-answer graph-with-edge query-node-with-edge-no-matched-B))
  (print " Edge\t ") (println (get-match-answer graph-with-edge query-node-with-edge-matched-edge-A-B)))


(comment
(deftest match-node-where-properties-test
  (testing "match")
  (is (match-where-properties nA query-node-matched))
  (is (match-where-properties nA query-node-matched-2))
  (is (match-where-properties nA query-node-with-any-data))
  (testing "no match")
  (is (not (match-where-properties nA query-node-no-matched)))
  (testing "unknown keys")
  (is (not (match-where-properties nA query-node-no-matched-unknown)))
  )

)