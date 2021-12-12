(ns jsongraph.impl.graph-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.core :refer :all]
            [jsongraph.impl.utils :refer :all]
            [jsongraph.api :refer [gen-node gen-edge]]

            [jsonista.core :as j]
            [clojure.data.json :as json]
            )
  (:import (java.io File))
)

(use '[clojure.pprint :only (pprint)])


(def file_e  (File. "./resources/graph_empty.json"))
(def file_b  (File. "./resources/graph_byte.json"))

(defn get-groud-true-from-file [^String file-name]
  (j/read-value (File. file-name) (j/object-mapper {:decode-key-fn true})))

(def nA (gen-node [] {} :A)) (def kA (get-key nA))
(def nB (gen-node [] {} :B)) (def kB (get-key nB))
(def nC (gen-node [] {} :C)) (def kC (get-key nC))
(def nD (gen-node [] {} :D)) (def kD (get-key nD))

(def g-add-nodes (add-node (add-node (gen-empty-graph) [nA nB nC]) nD))

(def edgeAB  (gen-edge nA nB [] {:cost 1}))
(def edgeAC  (gen-edge nA nC [] {:cost 4}))

(def edgeBA  (gen-edge nB nA [] {:cost 2}))
(def edgeBA- (gen-edge nB nA [] {:cost 3}))

(def edgeDA  (gen-edge nD nA [] {:cost 8}))
(def edgeDC  (gen-edge nD nC [] {:cost 34}))

(json/pprint g-add-nodes)

;(j/write-value (File. "./resources/g.json") (adjacency-from-edges [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC]) (j/object-mapper {:pretty true}))

(deftest adjacency-from-edges-test
  (println)
  (println "adjacency-from-edges-test")
  (pprint [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC])     ;edgeBA- rewrite data after edgeBA
  (println "result")
  (json/pprint (adjacency-from-edges [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC]))
  (is (= (get-groud-true-from-file "./resources/adjacency-from-edges-test.json")
         (adjacency-from-edges [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC])))
  )

(deftest add-out-edges-to-adjacency-test
  (println)
  (println "add-out-edges-to-adjacency-test")
  (json/pprint (g-add-nodes :adjacency))
  (pprint [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC])
  (println "result")
  (json/pprint (add-out-edges (g-add-nodes :adjacency) [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC]))
  ;(is (= (get-groud-true-from-file "./resources/add-out-edges-to-adjacency-test.json")
  ;            (add-out-edges (g-add-nodes :adjacency) [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC])))
  )


(deftest add-edge-test
  (println)
  (println "add-edge-test")
  (json/pprint g-add-nodes)
  (pprint [edgeAB edgeAC edgeDA edgeDC edgeAB])
  (println "result")
  (json/pprint (add-edge g-add-nodes [edgeAB edgeAC edgeDA edgeDC edgeAB]))
  )

(deftest add-edge-test
  (println)
  (println "add-edge-test")
  (json/pprint g-add-nodes)
  (pprint [edgeAB edgeAC edgeDA edgeDC edgeAB])
  (println "result")
  (json/pprint (add-edge g-add-nodes [edgeAB edgeAC edgeDA edgeDC edgeAB]))
  )

(def full-graph (add-edge g-add-nodes [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC]))
(j/write-value (File. "./resources/graph_2.json") full-graph)

(deftest delete-adjacency-edge-test
  (println)
  (println "delete-adjacency-edge-test")
  (json/pprint (full-graph :adjacency))
  (println  kA [kB])
  (json/pprint (delete-adjacency-edge (full-graph :adjacency) kA [kB]))
  )


(deftest delete-in-edges-test
  (println)
  (println "delete-in-edge-test")
  (json/pprint (full-graph :adjacency))
  (println [kB kC] [kA])
  (json/pprint (delete-in-edges (full-graph :adjacency) [kB kC] [kA]))
  )

(deftest delete-edges-from-adjacency-test
  (println)
  (println "delete-edges-from-adjacency-test")
  (json/pprint full-graph)
  (pprint [edgeAB edgeBA edgeBA-])
  (println "result")
  (json/pprint (delete-edges-from-adjacency (full-graph :adjacency) [edgeAB edgeBA edgeBA-]))
  )

(deftest delete-edges-in-all-nodes-test
  (println)
  (println "delete-edges-in-all-nodes-test")
  (json/pprint full-graph)
  (pprint [kA kC])
  (println "result")
  (json/pprint (delete-edges-in-all-nodes (full-graph :adjacency) [kA kC]))
  )

(deftest delete-node-test
  (println)
  (println "delete-node-test")
  (json/pprint full-graph)
  (println "nodes" [kA kC])
  (json/pprint (delete-node full-graph [kA kC]))
  )


(deftest delete-edges-test
  (println)
  (println "delete-edges-test")
  (json/pprint full-graph)
  (pprint [edgeAB edgeBA edgeBA-])
  (println "result")
  (json/pprint (delete-edges full-graph [edgeAB edgeBA edgeBA-]))
  )

(def small-graph (add-edge g-add-nodes [edgeAC edgeBA  edgeDC]))

(deftest match-adjacency-item-test
  (println)
  (println "match-adjacency-item-test")
  (json/pprint ((small-graph :adjacency) kA))
  (println "in")
  (json/pprint ((full-graph :adjacency) kA))
  (println "result")
  (json/pprint (match-adjacency-item ((full-graph :adjacency) kA) ((small-graph :adjacency) kA)))
  )

(deftest match-adjacency-test
  (println)
  (println "match-adjacency-test")
  (json/pprint (small-graph :adjacency))
  (println "in")
  (json/pprint (full-graph  :adjacency))
  (println "result")

  (println (match-adjacency (full-graph :adjacency) (small-graph :adjacency)))
  )