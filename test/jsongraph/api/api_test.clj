(ns jsongraph.api.api-test
  (:require [clojure.test :refer :all]
            [jsongraph.api.api :refer :all]
            [clojure.data.json :as json]))

(use '[clojure.pprint :only (pprint)])

(def node (gen-node [] {}))
(def nA (gen-node [:label-nA] {:energy 6}))
(def nB (gen-node [:label-nB] {:energy 1}))
(def nC (gen-node [:label-nC] {:energy 4}))
(def nD (gen-node [:label-nD] {:energy 35}))

(def g-add-nodes (add-nodes (add-nodes (create-graph) [nA nB nC]) nD))

(def edgeAB  (gen-edge nA nB [:label-edgeAB] {:cost 1}))
(def edgeAC  (gen-edge nA nC [:label-edgeAC] {:cost 4}))
(def edgeBA  (gen-edge nB nA [:label-edgeBA] {:cost 2}))
(def edgeDA  (gen-edge nD nA [:label-edgeDA] {:cost 8}))
(def edgeDC  (gen-edge nD nC [:label-edgeDC] {:cost 34}))

(deftest index-from-many-test
  (println)
  (println "index-from-many-for-single-node-test")
  (pprint nA)
  (println "result")
  (println (index-from-many nA))  ; no efficient (use index)
  (println)
  (println)
  (println "index-from-many-nodes-test")
  (pprint [nA nB])
  (println "result")
  (println (index-from-many [nA nB]))
  )


(deftest create-graph-test
  (json/pprint (create-graph))
  (json/pprint (create-graph [node]))
  (json/pprint (create-graph [nA nB nC]))
  (json/pprint (create-graph [nA nB nC] [edgeAB edgeAC edgeBA]))
  )

(deftest delete-one-node-from-one-node-graph-test
  (println)
  (println "delete-one-node-from-one-node-graph-test")
  (json/pprint (create-graph [nA]))
  (pprint nA)
  (println "result")
  (is (delete-nodes (create-graph [nA]) nA) (create-graph))
  (json/pprint (delete-nodes (create-graph [nA]) nA))
  )


(deftest delete-one-node-test
  (println)
  (println "delete-one-node-test")
  (json/pprint g-add-nodes)
  (pprint nA)
  (println "result")
  (is (delete-nodes g-add-nodes nA) (create-graph [nB nC nD]))
  (json/pprint (delete-nodes g-add-nodes nA))
  )

(deftest delete-nodes-test
  (println)
  (println "delete-edges-test")
  (json/pprint g-add-nodes)
  (pprint [nA nB nC])
  (println "result")
  (is (delete-nodes g-add-nodes nA) (create-graph [nD]))
  (json/pprint (delete-nodes g-add-nodes [nA nB nC]))
  )

(deftest add-one-edge-test
  (println)
  (println "add-one-edge-test")
  (json/pprint g-add-nodes)
  (pprint edgeAB)
  (println "result")
  (json/pprint (add-edges g-add-nodes [edgeAB]))
  )

(deftest add-edges-test
  (println)
  (println "add-edges-test")
  (json/pprint g-add-nodes)
  (pprint [edgeAB edgeAC edgeDA edgeDC edgeAB])
  (println "result")
  (json/pprint (add-edges g-add-nodes [edgeAB edgeAC edgeDA edgeDC edgeAB]))
  )


(def full-graph (add-edges g-add-nodes [edgeAB edgeAC edgeBA edgeDA edgeDC]))

(deftest delete-one-edge-test
  (println)
  (println "delete-one-edges-test")
  (json/pprint full-graph)
  (pprint edgeAB)
  (println "result")
  (json/pprint (delete-edges full-graph [edgeAB]))
  )

(deftest delete-edges-test
  (println)
  (println "delete-edges-test")
  (json/pprint full-graph)
  (pprint [edgeAB edgeBA edgeDC])
  (println "result")
  (json/pprint (delete-edges full-graph [edgeAB edgeBA edgeDC]))
  )