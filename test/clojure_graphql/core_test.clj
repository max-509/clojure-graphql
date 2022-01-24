(ns clojure-graphql.core-test
  (:require [clojure.test :refer :all]
            [clojure-graphql.core :refer :all]
            [clojure-graphql.impl.language-parser :refer :all]))


(defquery test-query "create (a:Person:Manager {name: 'Emil' klout: 99 list: [99 0 1] listt: []})-[b:FRIEND]->(c:Person {from: 'Sweden'})-[d:ENEMY {year: 1999}]->(e)
                      saveviz test" )

(def db (init-db))
(test-query db)


;(defquery create+return "create (a:Person:Manager {name: 'Emil' klout: 99 list: [99 0 1] listt: []})-[b:FRIEND $A]->(c:Person {from: 'Sweden'})-[d:ENEMY {year: 1999}]->(e)
;                        return a, b, c, d, e")
;
;(defquery match "match (a)-[b]->(c)
;                return a, b, c")
;
;(defquery delete "match (a) where a.name = 'Emil'
;                delete a
;                match (a) where a.name = 'Emil'
;                return a")
;
;(defquery undo "undo")
;
;(defquery SET "match ()-[b:FRIEND]->()
;               set b.name = 'Vladimir'
;               set b:BEST_FRIEND
;               return *")
;
;(defquery create-between-exists "match (a:Manager)
;                                 match (b {from: 'Sweden'})
;                                 create (a)-[c:NEW]->(b)
;                                 match (a)-[c]->(b)
;                                 return *")
;
;(def db (init-db))
;
;(def return+create-persons (create+return db {:A {:name "Egor" :cost 1000}}))
;
;(clojure.pprint/pprint "create+return")
;(clojure.pprint/pprint return+create-persons)
;
;(def match-persons (match db))
;
;(clojure.pprint/pprint "match")
;(clojure.pprint/pprint match-persons)
;
;(def delete-persons (delete db))
;
;(clojure.pprint/pprint "delete")
;(clojure.pprint/pprint delete-persons)
;
;(undo db)
;
;(def set-persons (SET db))
;
;(clojure.pprint/pprint "SET")
;(clojure.pprint/pprint set-persons)
;
;(def create-between-exists-persons (create-between-exists db))
;
;(clojure.pprint/pprint "create-between-exists")
;(clojure.pprint/pprint create-between-exists-persons)
