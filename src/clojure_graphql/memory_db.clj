(ns clojure-graphql.memory-db)

(def dbs (hash-map))

(def connect [name]
  (let [db (hash-map)]
    (assoc! dbs name db)
    db))
