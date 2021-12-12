(ns clojure-graphql.core-test
  (:require [clojure.test :refer :all]
            [clojure-graphql.core :refer :all]
            [clojure-graphql.memory-db :refer :all]
            [clojure-graphql.impl.core-impl :refer :all]))

(use '[clojure.pprint :only (pprint)])

;(println (rest [1]))

(defquery get-persons "create (a:Person:Manager {name: \"Emil\" from: \"Sweden\" klout: 99 list: [99 0 1] listt: []})-->()
                      match (a) WHERE a:Person AND a.condition = \"ASd\" AND a.salary > 10")
;(defquery get-persom "match (a) WHERE a:Person AND a.name = a.family AND a.condition = '> 10' AND a.salary > 10")

;(pprint (create-rule "match (a) WHERE a:Person AND a.condition = \"ASd\" AND a.salary > 10"))

;"(match {:labels: [], :properties: []})"
(get-persons (connect "local-db"))

;(with-session [session (get-session local_db)]
;              (get-persons session))

;(with-session [session (get-session (connect "db"))]
;              (run-query session get-persons)
;              (run-query session  create-person))


;(defquery test-query "create (:Person :Manager {:name \"Emil\", :from \"Sweden\", :klout 99}) - [:FRIEND {:duration \"Forever\"}] -> (:Person :Director {:name \"Frank\", :from \"USA\"})")

;(test-query 1)
