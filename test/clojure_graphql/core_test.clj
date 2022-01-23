(ns clojure-graphql.core-test
  (:require [clojure.test :refer :all]
            [clojure-graphql.core :refer :all]
            [clojure-graphql.impl.language-parser :refer :all]))

(use '[clojure.pprint :only (pprint)])

;(println (rest [1]))

(defquery get-persons "create (a:Person:Manager {name: 'Emil' from: 'Sweden' klout: 99 list: [99 0 1] listt: []})-[b:FRIEND $A]->(c:Person)-[d:VRAG {god: 1999}]->(e)
                      set a:EMIL, b.cost = 5000, d.god = null, e.name = $A
                      delete a, b, c, d, e
                      return a, b, c, d, e")
;(defquery get-persons "create (a:Person:Manager $A)")

;(pprint (create-rule "match (a) WHERE a:Person AND a.condition = \"ASd\" AND a.salary > 10"))

(def db (init-db))

;(pprint db)

(def return-persons (get-persons db {:A {:name "Egor" :cost 1000}}))
(clojure.pprint/pprint return-persons)
;
;(pprint db)
;;;
;(print-last-version db)

;(with-session [session (get-session local_db)]
;              (get-persons session))

;(with-session [session (get-session (connect "db"))]
;              (run-query session get-persons)
;              (run-query session  create-person))


;(defquery test-query "create (:Person :Manager {:name \"Emil\", :from \"Sweden\", :klout 99}) - [:FRIEND {:duration \"Forever\"}] -> (:Person :Director {:name \"Frank\", :from \"USA\"})")

;(test-query 1)

;(def node (transient {:labels [:a :b :c] :props {:cost 100}}))
;(def adj-list {:node1 node})
;(pprint "Before assoc:")
;(pprint (:labels node))
;(pprint (:labels (:node1 adj-list)))
;(assoc! node :labels (conj (:labels node) :d))
;(pprint "After assoc:")
;(pprint (:labels node))
;(pprint (:labels (:node1 adj-list)))
