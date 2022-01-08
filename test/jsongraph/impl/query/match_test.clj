(ns jsongraph.impl.query.match-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.utils :refer [split-json pprint-list]]
            [jsongraph.impl.query.match :refer :all]
            [jsongraph.api.graph-api :refer [gen-node gen-edge-data
                                             create-graph create-one-edge-adjacency
                                             get-nodes-from-graph]]
            [jsongraph.api.match-api :refer :all]))
(use '[clojure.pprint :only (pprint)])

(def prop-A {:money 100 :age 21 :weight 50})
(def prop-B {:money 380 :age 30 :weight 64})
(def prop-C {:money 270 :age 18 :weight 48})
(def prop-D {:money 55  :age 30 :weight 39})

(def nA  (gen-node [:lab-A] prop-A :A))
(def nB  (gen-node [:lab-B] prop-B :B))
(def nC  (gen-node [:lab-C] prop-C :C))
(def nD  (gen-node [:lab-D] prop-D :D))

(def prop-edge-AB {:cost 68 :danger 10})
(def edge-AB (gen-edge-data nA nB [:AB] prop-edge-AB))

(def prop-edge-AC {:cost 56 :danger 3})
(def edge-AC (gen-edge-data nA nC [:AC] prop-edge-AC))

(def prop-edge-CB  {:cost 40 :danger 5})
(def edge-CB (gen-edge-data nC nB [:CB] prop-edge-CB))

(def prop-edge-BC prop-edge-AC)
(def edge-BC (gen-edge-data nB nC [:AC] prop-edge-BC))

(def prop-edge-BD prop-edge-AC)
(def edge-BD (gen-edge-data nB nD [:AC] prop-edge-BD))

(def prop-edge-DA prop-edge-AB)
(def edge-DA (gen-edge-data nD nA [:AB] prop-edge-DA))

(def graph-empty (create-graph))
(def graph-nA (create-graph [nA]))
(def graph-nA-nB (create-graph [nA nB]))
(def graph-with-edge (create-graph [nA nB] [edge-AB]))
(def graph-with-edges (create-graph [nA nB nC] [edge-AB edge-AC edge-CB]))
(def big-graph (create-graph [nA nB nC nD] [edge-AB edge-AC edge-CB edge-BC edge-BD edge-DA]))

;; Match query

(def prop-unknown  {:money 100 :cost 21})
(def prop-A-no-mch {:money 100 :age 40 :weight 50})
(def prop-B-no-mch {:money 380 :age 30 :weight 70})

;(def query-where-matched    {:money [:gt 70] :age [:lt 30] :weight [:eq 50]})
;(def query-where-no-matched {:money [:lt 70] :age [:gt 30] :weight [:eq 50]})
;(def query-where-on-matched-unknown {:age [:gt 18] :cost [:eq 45]})

(def query-node-no-matched         (gen-node [:lab-A] prop-C))
(def query-node-no-matched-unknown (gen-node [:lab-A] prop-unknown))
(def query-node-no-matched-lab     (gen-node [:lab-B] prop-A))


(def query-node-matched-A    (gen-node [:lab-A] prop-A))
(def query-node-no-matched-A (gen-node [:lab-A] prop-A-no-mch))


(def query-node-matched-B    (gen-node [:lab-B] prop-B))
(def query-node-no-matched-B (gen-node [:lab-B] prop-B-no-mch))


(def query-node-matched-any (gen-node nil nil))

(def query-node-with-edge-matched-AB
  (create-one-edge-adjacency
    query-node-matched-A query-node-matched-B
    nil prop-edge-AB))


(def query-node-with-edge-matched-A-any
  (create-one-edge-adjacency
    query-node-matched-A query-node-matched-any
    nil nil))

(def query-node-with-edge-matched-any-any
  (create-one-edge-adjacency
    query-node-matched-any (gen-node nil nil)
    nil nil))

(def query-n-any (gen-node nil nil :n))
(def query-k-any (gen-node nil nil :k))
(def query-any (gen-node nil nil))

(def query-node-with-AB-AC-edges
  (:adjacency (create-graph [query-n-any query-k-any query-any]
                [(gen-edge-data query-n-any query-k-any [:AB] prop-edge-AB)
                 (gen-edge-data query-k-any query-any  [:AC] prop-edge-AC)])))
;; no matched

(def query-node-with-edge-no-matched-A
  (create-one-edge-adjacency
    query-node-no-matched-A query-node-matched-B
    nil prop-edge-AB))

(def query-node-with-edge-no-matched-B
  (create-one-edge-adjacency
    query-node-matched-A query-node-no-matched-B
    nil prop-edge-AB))

(def query-node-with-edge-no-matched-edge-AB
  (create-one-edge-adjacency
    query-node-matched-A query-node-matched-B
    nil (assoc prop-edge-AB :cost -1)))


;Tests

(deftest match-data-test
  (testing "match")
  (is (match-data nA query-node-matched-A))
  (is (match-data nA query-node-matched-any))
  (testing "no match")
  (is (not (match-data nA query-node-no-matched-lab)))
  (is (not (match-data nA query-node-no-matched))))


(deftest match-json-test
  (is (match-json (graph-with-edges :adjacency) query-node-matched-any) '(:A :B :C)))

(deftest match-edges-query-test
  (is (match-adj-edges-list (graph-with-edges :adjacency) query-node-with-edge-matched-A-any) '({:A (:B :C)})))

;==========================================================================

(deftest get-match-adj-edges-graph-wo-edges-test
  (testing "match")
  (is (get-matched-nodes (graph-nA :adjacency) query-node-matched-A) '(:A))
  (is (get-matched-nodes (graph-nA-nB :adjacency) query-node-matched-any) '(:A :B))
  (testing "no match")
  (is (get-matched-nodes (graph-empty :adjacency) query-node-matched-A) '())
  (is (match-adj-edges-list (graph-empty :adjacency) query-node-matched-any) '()))


(deftest get-match-adj-edges-graph-with-edges-demo
  (testing "match")(println "Match")
  (print " Any nodes\t\t\t\t   ") (println (get-matched-nodes (graph-with-edge :adjacency) query-node-matched-any))
  (print " Source in graph with edge ") (println (get-matched-nodes (graph-with-edge :adjacency) query-node-matched-A))
  (print " Full edge\t\t\t\t   ") (println (match-adj-edges-list (graph-with-edge :adjacency) query-node-with-edge-matched-AB))
  (print " Any nodes in big graph\t   ") (println (get-matched-nodes (graph-with-edges :adjacency) query-node-matched-any))
  (print " Source to any\t\t\t   ") (println (match-adj-edges-list (graph-with-edges :adjacency) query-node-with-edge-matched-A-any))
  (print " Any to any\t\t\t\t   ") (println (match-adj-edges-list (graph-with-edges :adjacency) query-node-with-edge-matched-any-any))
  (testing "no match") (println "NO Match")
  (print " Source\t ") (println (match-adj-edges-list (graph-with-edge :adjacency) query-node-with-edge-no-matched-A))
  (print " Target\t ") (println (match-adj-edges-list (graph-with-edge :adjacency) query-node-with-edge-no-matched-B))
  (print " Edge\t ") (println (match-adj-edges-list (graph-with-edge :adjacency) query-node-with-edge-no-matched-edge-AB)))

(deftest match-adj-edges-query-big-graph-demo
  (pprint (match-adj-edges-list (big-graph :adjacency) query-node-with-AB-AC-edges)))

;==========================================================================

(deftest merge-by-keys-test
  (is (merge-by-keys
        {:A '((:A :B) (:A :C)) ; <-
         :B '((:B :A))         ; <-
         :C '((:C :A) (:C :D) (:C :B))
         }   [:A :B]
        )
      '((:A :B) (:A :C) (:B :A))))

(deftest get-matched-ways-graph-wo-edges-test
  (testing "match") (println "match")
  (is (get-matched-ways (graph-nA :adjacency) query-node-matched-A) '((:A)))
  (is (get-matched-ways (graph-nA-nB :adjacency) query-node-matched-any) '((:A) (:B)))
  (testing "no match") (println "no match")
  (is (get-matched-ways (graph-empty :adjacency) query-node-matched-A) '())
  (is (get-matched-ways (graph-empty :adjacency) query-node-matched-any) '()))


(deftest get-matched-ways-graph-with-edges-demo
  (testing "match")(println "Match")
  (print " Any nodes\t\t\t\t   ") (println (get-matched-ways (graph-with-edge :adjacency) query-node-matched-any))
  (print " Source in graph with edge ") (println (get-matched-ways (graph-with-edge :adjacency) query-node-matched-A))
  (print " Full edge\t\t\t\t   ") (println (get-matched-ways (graph-with-edge :adjacency) query-node-with-edge-matched-AB))
  (print " Any nodes in big graph\t   ") (println (get-matched-ways (graph-with-edges :adjacency) query-node-matched-any))
  (print " Source to any\t\t\t   ") (println (get-matched-ways (graph-with-edges :adjacency) query-node-with-edge-matched-A-any))
  (print " Any to any\t\t\t\t   ") (println (get-matched-ways (graph-with-edges :adjacency) query-node-with-edge-matched-any-any))
  (testing "no match") (println "NO Match")
  (print " Source\t ") (println (get-matched-ways (graph-with-edge :adjacency) query-node-with-edge-no-matched-A))
  (print " Target\t ") (println (get-matched-ways (graph-with-edge :adjacency) query-node-with-edge-no-matched-B))
  (print " Edge\t ") (println (get-matched-ways (graph-with-edge :adjacency) query-node-with-edge-no-matched-edge-AB)))

(deftest get-matched-ways-big-graph-demo
  (println (get-matched-ways (big-graph :adjacency) query-node-with-AB-AC-edges)))
