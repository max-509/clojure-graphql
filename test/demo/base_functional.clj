(ns demo.base-functional
  (:require [clojure-graphql.core :refer :all]))

(defquery create-two-families "create
                                (:MALE {name: 'Dima'})-[:SON]->(p1 {name: 'Oleg'})<-[:DAUGHTER]-(:FEMALE {name: 'Dasha'}),
                                (:FEMALE {name: 'Lyuba'})-[:DAUGHTER]->(p1)
                               create
                                (:MALE {name: 'Kirill'})-[:SON]->(p2 {name: 'Roman'})<-[:DAUGHTER]-(:FEMALE {name: 'Katya'}),
                                (:FEMALE {name: 'Lera'})-[:DAUGHTER]->(p2)

                                savejson two-families.json
                                saveviz two-families")

(defquery match-parents "
                        loadjson two-families.json
                        create (:GRANDFATHER $G)
                        match (c1)-[e1:SON]->(p)<-[e2:DAUGHTER]-(c2),(g:GRANDFATHER) link (c2)-[:SISTER]->(c1),(g)-[:PARENT]->(p)
                        saveviz full-family")

(defquery change-gender "match (g) where g.name = 'Svyatoslav'
                        set g:GRANDMOTHER
                        saveviz changed")

(defquery grandmother-death "match (g:GRANDMOTHER)
                              delete g
                              saveviz death")

(defquery delete-sister-links "match ()-[l:SISTER]->()
                              delete l
                              saveviz without-links")

(def db1 (init-db))
(def db2 (init-db))

(create-two-families db1  {:G {:name "Svyatoslav" :age 100}})

(match-parents db2 {:G {:name "Svyatoslav" :age 100}})

(change-gender db2)

(grandmother-death db2)

(delete-sister-links db2)


