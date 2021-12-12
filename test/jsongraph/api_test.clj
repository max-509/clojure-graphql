(ns jsongraph.api-test
  (:require [clojure.test :refer :all]
            [jsongraph.api :refer :all]
            [clojure.data.json :as json]))
(use '[clojure.pprint :only (pprint)])
(comment
  An array-map maintains the insertion order of the keys.
  Look up is linear, which is not a problem for small maps (say less than 10 keys).
  If your map is large, you should use hash-map instead. !!!
  )

(def nA (gen-node [:label-A] {}))
(def nB (gen-node [:label-B] {}))
(def nC (gen-node [:label-C] {}))
(def nD (gen-node [:label-D] {}))

(def g-add-nodes (add-nodes (add-nodes (create-graph) [nA nB nC]) nD))

(def edgeAB  (gen-edge nA nB [] {:cost 1}))
(def edgeAC  (gen-edge nA nC [] {:cost 4}))
(def edgeBA  (gen-edge nB nA [] {:cost 2}))
(def edgeDA  (gen-edge nD nA [] {:cost 8}))
(def edgeDC  (gen-edge nD nC [] {:cost 34}))



(deftest create-graph-test
  (json/pprint (create-graph))
  (json/pprint (create-graph [nA nB nC]))
  (json/pprint (create-graph [nA nB nC] [edgeAB edgeAC edgeBA]))
  )

(deftest add-edge-test
  (println)
  (println "add-edge-test")
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
