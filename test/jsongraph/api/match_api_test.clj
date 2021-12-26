(ns jsongraph.api.match-api-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.query-test :refer [query-where-matched
                                               query-where-no-matched
                                               query-where-on-matched-unknown]]
            [jsongraph.api.graph-api :refer [gen-node gen-edge create-graph]]
            [jsongraph.api.match-api :refer :all]))

(use '[clojure.pprint :only (pprint)])

(def prop-A {:money 100 :age 21 :weight 50})
(def prop-B {:money 380 :age 30 :weight 64})
(def prop-C {:money 270 :age 18 :weight 48})

(def nA  (gen-node [:lab-A] prop-A :A))
(def nB  (gen-node [:lab-B] prop-B :B))
(def nC  (gen-node [:lab-C] prop-C :C))


(def query-node-matched-A    (gen-query-node [:lab-A] {} query-where-matched))
(def query-node-no-matched-A (gen-query-node [:lab-A] {} query-where-no-matched))


(def query-edge (gen-query-edge nil nil {:cost [:lt 75] :danger [:gt 5]}))
(def query-edge-no-mch (gen-query-edge nil nil {:cost [:lt 0] :danger [:gt 100]}))
(def query-edge-any (gen-query-edge))


(def query-node-matched-B (gen-query-node [:lab-B] {}
                                          {:money [:gt 200] :age [:gt 25] :weight [:ne 60]}))
(def query-node-no-matched-B (gen-query-node [:lab-B] {}  query-where-on-matched-unknown))


(def query-node-matched-any    (gen-query-node nil {} nil))


(def prop-edge-AB {:cost 68 :danger 10})
(def edge-AB (gen-edge nA nB nil prop-edge-AB))

(def prop-edge-AC {:cost 56 :danger 3})
(def edge-AC (gen-edge nA nC nil prop-edge-AC))

(def prop-edge-CB {:cost 40 :danger 5})
(def edge-CB (gen-edge nC nB nil prop-edge-CB))


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

(def graph-empty (create-graph))
(def graph-nA (create-graph [nA]))
(def graph-nA-nB (create-graph [nA nB]))
(def graph-with-edge (create-graph [nA nB] [edge-AB]))
(def graph-with-edges (create-graph [nA nB nC] [edge-AB edge-AC edge-CB]))

(deftest match-query-graph-wo-edges-test
  (testing "match") (println "match")
  (println (match-query graph-nA query-node-matched-A))
  (println (match-query graph-nA-nB query-node-matched-any))
  (testing "no match") (println "no match")
  (println (match-query graph-empty query-node-matched-A))
  (println (match-query graph-empty query-node-matched-any))
  )


(deftest match-query-graph-with-edges-test
  (println "Match")
  (print " Any nodes\t\t\t\t   ") (println (match-query graph-with-edge query-node-matched-any))
  (print " Source in graph with edge ") (println (match-query graph-with-edge query-node-matched-A))
  (print " Full edge\t\t\t\t   ") (println (match-query graph-with-edge query-node-with-edge-matched-A-B))
  (print " Any nodes in big graph\t   ") (println (match-query graph-with-edges query-node-matched-any))
  (print " Source to any\t\t\t   ") (println (match-query graph-with-edges query-node-with-edge-matched-A-any))
  (print " Any to any\t\t\t\t   ") (println (match-query graph-with-edges query-node-with-edge-matched-any-any))
  (println "NO Match")
  (print " Source\t ") (println (match-query graph-with-edge query-node-with-edge-no-matched-A))
  (print " Target\t ") (println (match-query graph-with-edge query-node-with-edge-no-matched-B))
  (print " Edge\t ") (println (match-query graph-with-edge query-node-with-edge-matched-edge-A-B))
  )


(deftest node-to-query-node-test
  (pprint nA)
  (pprint (node-to-query-node nA query-where-matched))
  )
