(ns clojure-graphql.core-test
  (:require [clojure.test :refer :all]
            [clojure-graphql.core :refer :all]
            [clojure-graphql.impl.language-parser :refer :all]))


;(defquery test-query "create (g:GRANDFATHER)
;                      create (:MALE {name: 'c1'})-[:SON]->(p1 {name: 'p1'})<-[:DAUGHTER]-(:FEMALE {name: 'c2'})
;                      create (:FEMALE {name: 'c3'})-[:DAUGHTER]->(p1)
;                      create (:MALE {name: 'c4'})-[:SON]->(p2 {name: 'p2'})<-[:DAUGHTER]-(:FEMALE {name: 'c5'})
;                      create (:FEMALE {name: 'c6'})-[:DAUGHTER]->(p2)
;                      match (c1)-[e1:SON]->(p)<-[e2:DAUGHTER]-(c2),(g:GRANDFATHER) link (c2)-[:SISTER]->(c1),(g)-[:PARENT]->(p)
;                      set g:GRANDMOTHER
;                      saveviz sisters
;                      return *" )
;
(def db (init-db))
;(clojure.pprint/pprint (test-query db))


(defquery create+return "create (a:Person:Manager {name: 'Emil' klout: 99 list: [99 0 1] listt: []})-[b:FRIEND $A]->(c:Person {from: 'Sweden'})-[d:ENEMY {year: 1999}]->(e)
                        return a, b, c, d, e")

(defquery match "match (a)-[b]->(c)
                return a, b, c")

(defquery delete "match (a) where a.name = 'Emil'
                delete a
                match (a) where a.name = 'Emil'
                return a")

(defquery undo "undo")

(defquery SET "match ()-[b:FRIEND]->()
               set b.name = 'Vladimir'
               set b:BEST_FRIEND
               return *")

(defquery create-between-exists "match (a:Manager)
                                 match (b {from: 'Sweden'})
                                 create (a)-[c:NEW]->(b)
                                 match (a)-[c]->(b)
                                 return *")

(def db (init-db))

(def return+create-persons (create+return db {:A {:name "Egor" :cost 1000}}))

(clojure.pprint/pprint "create+return")
(clojure.pprint/pprint return+create-persons)

(def match-persons (match db))

(clojure.pprint/pprint "match")
(clojure.pprint/pprint match-persons)

(def delete-persons (delete db))

(clojure.pprint/pprint "delete")
(clojure.pprint/pprint delete-persons)

(undo db)

(def set-persons (SET db))

(clojure.pprint/pprint "SET")
(clojure.pprint/pprint set-persons)

(def create-between-exists-persons (create-between-exists db))

(clojure.pprint/pprint "create-between-exists")
(clojure.pprint/pprint create-between-exists-persons)
