(ns clojure-graphql.core)

(def clauses
  {'create ::create
   'match ::match})

;(defmulti parse-query (fn clause-extractor
;                        ([query] (clause-extractor query {}))
;                        ([[clause & body] context] (get clauses clause))))
;
;(defmethod parse-query ::create
;  ([query] (parse-query query {}))
;  ([[clause & body] context]
;   (for [n (map vec (partition 2 1 (concat [nil] body [nil])))]
;     (if (list? (second n))
;       ))))
;
;(defmacro defquery
;  ([query_name query]
;   `(let [parsed_query# ~(parse-query query)]
;     (println parsed_query#)
;     (def ~query_name (fn
;                        ([~'session] (println "Work with session"))
;                        ([~'session ~'params] (println "Work with session with params"))))))
;  ([query_name query & queries]))
