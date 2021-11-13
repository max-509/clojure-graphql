(ns clojure-graphql.core
  (:require [instaparse.core :as insta]))

; see https://github.com/aroemers/crustimoney
;(def create-rule
;  {:clause [ :create ]
;   :create [ :create-command ]
;
;   :left-brace #"("
;   :right-brace #")"
;   :create-command #"(create|CREATE)"
;   })

(def create-rule
  (insta/parser
    "clauses = clause (<whitespaces> clause)*
    clause = create;
    create = <create-command> <left-brace> labels properties? <right-brace>;

    properties = <left-curly-bracket> property (<whitespaces> property)* <right-curly-bracket>
    property = word <':'> <whitespaces> data

    labels = label (<whitespaces> label)*;
    label = <':'> word;

    create-command = ('create'|'CREATE'|'Create') <whitespaces>;

    left-curly-bracket = <whitespaces>? '{' <whitespaces>?;
    right-curly-bracket = <whitespaces>? '}' <whitespaces>?;
    left-brace = <whitespaces>? '(' <whitespaces>?;
    right-brace = <whitespaces>? ')' <whitespaces>?;

    <data> = string | integer | float | boolean | list;
    list = '[' (string* | integer* | float* | boolean* | list*) ']';
    string = <'\"'> word <'\"'>;
    integer = #'-?[0-9]+';
    float = #'-?([0-9]+.[0-9]+ | (INF | inf) | (NAN | nan))';
    boolean = 'True' | 'TRUE' | 'true' | 'False' | 'FALSE' | 'false';

    <word> = #'[a-zA-Z]+';
    <digit> = #'[0-9]';
    <whitespaces> = #'\\s+';"))


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
