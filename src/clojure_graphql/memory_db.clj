(ns clojure-graphql.memory-db
  (:require [clojure-graphql.impl.memory-db-impl :as impl]
            [jsongraph.graph :as graph-mem]))

(def schema (atom {}))

; древовидная структура версий
; запросы к деревьям по матч или чему-нибудь

(defn connect [db_name]
  (let [db_by_name (get schema db_name)
        session (if (nil? db_by_name)
          (let [new_db (atom [(graph-mem/gen-empty-graph)])]
            (swap! schema #(assoc % db_name new_db))
            new_db)
          db_by_name)]
    (fn runner [query params] (impl/runner session query params))))

(defn get-session [db]
  (let [last-version (last @db)]))

;(defn close-session [session-wrapper]
;  (let [[db last-version] session-wrapper]
;    (swap! db #(conj % (last-version)))))

;(defmacro with-session [[session-name resource] & body]
;  `(let [~session-name ~resource]
;     ~@body))

