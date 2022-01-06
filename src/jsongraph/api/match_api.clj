(ns jsongraph.api.match-api
  (:require [jsongraph.impl.utils :refer :all]
            [jsongraph.impl.query.match :refer :all]))

;;generators

; structure matching
;; USE GRAPH API ;;

; where matching


; match

(comment   ; implement varietals
(defn no-query-var? [query-node]
  (uuid? (get-key query-node)))
(defn query-var? [query-node]
  (not (no-query-var? query-node)))
)

(defn match-nodes [graph query-node]
  (split-json (select-keys (graph :adjacency) (get-matched-nodes (graph :adjacency) query-node))))
  ;(if (no-query-var? query-node) '() ; []
  ;   (split-json (select-keys adjacency (get-matched-nodes adjacency query-node)))))

(defn match-edges [graph qnS query-edge qnT]
 ; (if (and (query-var? qnS) (query-var? qnT))
     (loop [adj-items (split-json (get-matched-edges (graph :adjacency) qnS query-edge qnT))
            edges (transient [])]
       (if-let [adj-item (first adj-items)]
         (recur
           (rest adj-items)
           (concat! edges (get-edges-by-match-adj-item (graph :adjacency) adj-item)))
        (persistent! edges)))
    ;'())
    )

(defn match-query [graph query]
  (let [[qnS qnT] (split-json query)   ;Node and edge only
        query-edge (get-field qnS :out-edges)]
    (if (nil? qnT) (match-nodes graph qnS)
      (match-edges graph qnS query-edge qnT))))