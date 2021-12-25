(ns clojure-graphql.impl.versions-tree
  (:require [jsongraph.api :as jgraph]))

(defn init-versions-tree
  ([] (init-versions-tree (jgraph/create-graph)))
  ([graph]
   [graph]))

(defn init-db
  ([] (init-db (init-versions-tree)))
  ([versions-tree]
   (let [empty-undo-list (seq [])]
     (atom {:versions-tree versions-tree
            :undo-list     empty-undo-list}))))

(defn get-versions-tree [db]
  (get @db :versions-tree))

(defn get-undo-list [db]
  (get @db :undo-list))

(defn get-graph-from-version-tree [version-tree]
  (first version-tree))

(defn get-last-version [db]
  (->
    (get-versions-tree db)
    (get-graph-from-version-tree)))

(defn undo! [db]
  (let [undo-list (get-undo-list db)
        undo-first-elem (first undo-list)
        prev-version (if (nil? undo-first-elem) (get-last-version db) undo-first-elem)]
    (swap! db #(assoc % :versions-tree prev-version))
    (swap! db #(assoc % :undo-list (rest undo-list)))
    (get-graph-from-version-tree prev-version)))

(defn add-new-version! [db new-graph]
  (let [versions-tree (get-versions-tree db)
        undo-list (get-undo-list db)
        new-versions-tree (init-versions-tree new-graph)
        updated-versions-tree (conj versions-tree new-versions-tree)]
    (swap! db #(assoc % :versions-tree new-versions-tree))
    (swap! db #(assoc % :undo-list (cons updated-versions-tree undo-list)))))
