(ns jsongraph.graph-test
  (:require [clojure.test :refer :all]
            [jsongraph.graph :refer :all]
            [jsongraph.utils :refer :all]
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

(def nA (gen-node :A nil [] {}))
(def nB (gen-node :B nil [] {}))
(def nC (gen-node :C nil [] {}))
(def nD (gen-node :D nil [] {}))

(def g-add-nodes (add-node (add-node (gen-empty-graph) [nA nB nC]) nD))

(def edgeAB  '([:A :B] {:cost 1}))   ;edge format '([source target] data)
(def edgeAC  '([:A :C] {:cost 4}))

(def edgeBA  '([:B :A] {:cost 2}))
(def edgeBA- '([:B :A] {:cost 3}))

(def edgeDA  '([:D :A] {:cost 8}))
(def edgeDC  '([:D :C] {:cost 34}))



(deftest adjacency-from-edges-test
  (println)
  (println "adjacency-from-edges-test")
  (pprint [edgeAB edgeAC edgeBA edgeBA- edgeDA edgeDC])     ;edgeBA- rewrite data after edgeBA
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
(def small-graph (add-edge g-add-nodes [edgeAC edgeBA  edgeDC]))
(j/write-value (File. "./resources/graph_2.json") full-graph)

(deftest delete-adjacency-edge-test
  (println)
  (println "delete-adjacency-edge-test")
  (json/pprint (full-graph :adjacency))
  (println :A [:B])
  (json/pprint (delete-adjacency-edge (full-graph :adjacency) :A [:B]))
  )


(deftest delete-in-edges-test
  (println)
  (println "delete-in-edge-test")
  (json/pprint (full-graph :adjacency))
  (println [:B :C] [:A])
  (json/pprint (delete-in-edges (full-graph :adjacency) [:B :C] [:A]))
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
  (pprint [:A :C])
  (println "result")
  (json/pprint (delete-edges-in-all-nodes (full-graph :adjacency) [:A :C]))
  )

(deftest delete-node-test
  (println)
  (println "delete-node-test")
  (json/pprint full-graph)
  (println "nodes" [:A :C])
  (json/pprint (delete-node full-graph [:A :C]))
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
  (println "delete-edges-test")
  (json/pprint ((full-graph :adjacency) :A))
  (json/pprint ((small-graph :adjacency) :A))
  (println "result")
  (json/pprint (match-adjacency-item ((full-graph :adjacency) :A) ((small-graph :adjacency) :A)))
  )

(deftest match-adjacency-item-test
  (println)
  (println "delete-edges-test")
  (json/pprint ((full-graph :adjacency) :A))
  (json/pprint ((small-graph :adjacency) :A))
  (println "result")
  (json/pprint (match-adjacency-item ((full-graph :adjacency) :A) ((small-graph :adjacency) :A)))
  )