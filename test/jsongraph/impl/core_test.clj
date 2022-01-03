(ns jsongraph.impl.core-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.graph :refer :all]
            [jsongraph.impl.utils :refer :all]
            [jsongraph.api.graph-api :refer [gen-node add-nodes gen-edge add-edges save-graph load-graph]]
            [clojure.data.json :as json]))

(use '[clojure.pprint :only (pprint)])


(def nA (gen-node [] {} :A)) (def kA (get-key nA))
(def nB (gen-node [] {} :B)) (def kB (get-key nB))
(def nC (gen-node [] {} :C)) (def kC (get-key nC))
(def nD (gen-node [] {} :D)) (def kD (get-key nD))

(def g-add-nodes (add-nodes (add-nodes (gen-empty-graph) [nA nB nC]) nD))

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
  (is (= (load-graph "./resources/adjacency-from-edges-test.json")
         (adjacency-from-edges [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC])))
  )

(deftest add-out-edges-to-adjacency-test
  (println)
  (println "add-out-edges-to-adjacency-test")
  (json/pprint (g-add-nodes :adjacency))
  (pprint [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC])
  (println "result")
  (json/pprint (add-out-edges! (g-add-nodes :adjacency) [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC]))
  ;(is (= (load-graph "./resources/add-out-edges-to-adjacency-test.json")
  ;            (add-out-edges (g-add-nodes :adjacency) [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC])))
  )


(def full-graph (add-edges g-add-nodes [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC]))
(save-graph full-graph "./resources/graph_2.json")

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

(deftest delete-edges-by-target-uuids-test
  (println)
  (println "delete-edges-by-target-uuids-test")
  (json/pprint full-graph)
  (pprint [kA kC])
  (println "result")
  (json/pprint (delete-edges-by-target-uuids (full-graph :adjacency) [kA kC]))
  )

(deftest delete-node-by-uuid-test
  (println)
  (println "delete-node-by-uuid-test")
  (json/pprint full-graph)
  (println "nodes" [kA kC])
  (json/pprint (delete-node-by-uuid full-graph [kA kC]))
  )