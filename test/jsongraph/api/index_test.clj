(ns jsongraph.api.index-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.graph-test :refer [full-graph]]
            [jsongraph.api.graph-api :refer [gen-node create-graph
                                             add-labels-index delete-labels-index]]))

(use '[clojure.pprint :only (pprint)])


(def nA  (gen-node [:lab-1] nil :A))
(def nB  (gen-node [:lab-2] nil :B))
(def nC  (gen-node [:lab-3] nil :C))
(def nD  (gen-node [:lab-1] nil :D))
(def nE  (gen-node [:lab-1] nil :E))
(def nF  (gen-node [:lab-2] nil :F))
(def nG  (gen-node [:lab-3] nil :G))
(def nH  (gen-node [:lab-3] nil :H))
(def nI  (gen-node [] nil :I))
(def nJ  (gen-node [] nil :J))


(def nodes-graph (create-graph [nA nB nC nD nE nF nG nH nI nJ]))
(def indexed-graph (add-labels-index nodes-graph [:lab-1 :lab-2 :lab-3 :lab-t]))

(deftest index-test
  (pprint nodes-graph)
  (pprint indexed-graph)
  (pprint (delete-labels-index indexed-graph [:lab-1 :lab-2 :lab]))
  (pprint (delete-labels-index indexed-graph [:lab-1 :lab-2 :lab-3 ]))
  (pprint (delete-labels-index indexed-graph [:lab-1 :lab-2 :lab-4 :lab-t])))



