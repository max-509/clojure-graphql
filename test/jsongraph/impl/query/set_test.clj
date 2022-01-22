(ns jsongraph.impl.query.set-test
  (:require [clojure.test :refer :all]
            [jsongraph.impl.query.match-test :refer [graph-with-edges
                                                     query-node-matched-A
                                                     query-node-matched-any]]
            [jsongraph.impl.query.match :refer :all]
            [jsongraph.impl.query.set :refer :all]))
(use '[clojure.pprint :only (pprint)])


(def set-query-A  {:labels [:set-only-A] :properties {:age 100 :weight nil}})
(def set-query-any  {:labels [:set-ALL] :properties {:money 0 :new "str"}})

(deftest set-node-demo
  (let [adjacency (graph-with-edges :adjacency)
        ways-A   (get-matched-ways adjacency query-node-matched-A)
        ways-any (get-matched-ways adjacency query-node-matched-any)]
    (pprint adjacency) (println)

    (println "\n>SET only node :A")
    (println "query set:" set-query-A)
    (print "matched ways: ") (println ways-A)
    (println "!! delete :weight in :A!!")
    (println "result:") (pprint (SET adjacency ways-A set-query-A))

    (println "\n>SET all nodes")
    (println "query set:" set-query-any)
    (print "matched ways: ") (println ways-any)
    (println "!! add :new = \"str\" for all!!")
    (println "result:") (pprint (SET adjacency ways-any set-query-any))))

