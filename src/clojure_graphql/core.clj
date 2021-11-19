(ns clojure-graphql.core
  (:require [instaparse.core :as insta]))

; see https://github.com/aroemers/crustimoney
;(def create-rule
;  {:clause [ :create ]
;   :create [ :create-command ]
;
;   :left-bracket #"("
;   :right-bracket #")"
;   :create-command #"(create|CREATE)"
;   })

(def create-rule
  (insta/parser
    "clauses = clause (<whitespaces> clause)*
    <clause> = create | match

    (*----------------------------CLAUSES DESCRIPTION-----------------------*)
    create = <create-command> patterns
    match = <match-command> patterns where
    (*----------------------------CLAUSES DESCRIPTION-----------------------*)

    (*----------------------------SUPPORT FOR CLAUSES-------------------------*)
    where = <where-command> predicates | Epsilon
    (*----------------------------SUPPORT FOR CLAUSES-------------------------*)

    (*----------------------------PATTERN DESCRIPTION-----------------------*)
    patterns = pattern (',' <whitespaces>? pattern)*
    pattern = node (relationship node)*

    node = <left-bracket> variable-name labels properties <right-bracket>
    relationship = dash relation right-arrow | left-arrow relation dash | dash relation dash
    relation = <left-square-bracket> variable-name label properties <right-square-bracket> | Epsilon

    labels = (label)+ | Epsilon
    label = <':'> word

    properties = <left-curly-bracket> property (<whitespaces> property)* <right-curly-bracket> | Epsilon
    property = field <':'> <whitespaces> data
    field = word
    (*----------------------------PATTERN DESCRIPTION-----------------------*)

    (*----------------------------PREDICATES DESCRIPTION----------------------*)
    predicates = predicate (<whitespaces> boolean-operator predicate)*
    predicate = not-command? (brackets-predicates | atom-predicate)
    <brackets-predicates> = <left-bracket> predicate (<whitespaces> boolean-operator predicate)* <right-bracket>
    <atom-predicate> = label-check | field-check | pattern-check

    label-check = variable-name labels
    field-check = variable-name <'.'> field <whitespaces> comparing-operator data
    pattern-check = pattern
    <comparing-operator> = lt-command
                        | le-command
                        | gt-command
                        | ge-command
                        | eq-command
                        | ne-command
                        | is-null-command
                        | is-not-null-command
                        | starts-with-command
                        | ends-with-command
                        | contains-command
                        | like-regex-command
                        | in-command
    <boolean-operator> = and-command | or-command | xor-command
    (*----------------------------PREDICATES DESCRIPTION----------------------*)

    (*----------------------------DATA TYPES-------------------------------*)

    variable-name = word | Epsilon
    <data> = string | integer | float | boolean | list
    list = '[' (string* | integer* | float* | boolean* | list*) ']'
    string = <'\"'> word <'\"'>
    integer = #'-?[0-9]+'
    float = #'-?([0-9]+.[0-9]+ | (INF | inf) | (NAN | nan))'
    boolean = 'True' | 'TRUE' | 'true' | 'False' | 'FALSE' | 'false'
    (*----------------------------DATA TYPES-------------------------------*)

    (*-------------------------------------SPECIAL CONSTRUCTS--------------------------------*)
    <right-arrow> = <whitespaces>? '->' <whitespaces>?
    <left-arrow> = <whitespaces>? '<-' <whitespaces>?
    <dash> = <whitespaces>? '-' <whitespaces>?
    left-curly-bracket = <whitespaces>? '{' <whitespaces>?
    right-curly-bracket = <whitespaces>? '}' <whitespaces>?
    left-square-bracket = <whitespaces>? '[' <whitespaces>?
    right-square-bracket = <whitespaces>? ']' <whitespaces>?
    left-bracket = <whitespaces>? '(' <whitespaces>?
    right-bracket = <whitespaces>? ')' <whitespaces>?
    (*-------------------------------------SPECIAL CONSTRUCTS--------------------------------*)

    (*--------------------------------COMMANDS---------------------------*)
    create-command = <('create' | 'CREATE' | 'Create')> <whitespaces>
    match-command = <('match' | 'MATCH' | 'Match')> <whitespaces>
    where-command = <('where' | 'WHERE' | 'Where')> <whitespaces>
    and-command = <('and' | 'AND' | 'And')> <whitespaces>
    or-command = <('or' | 'OR' | 'Or')> <whitespaces>
    xor-command = <('xor' | 'XOR' | 'Xor')> <whitespaces>
    not-command = <('not' | 'NOT' | 'Not')> <whitespaces>
    lt-command = <'<'> <whitespaces>
    le-command = <'<='> <whitespaces>
    gt-command = <'>'> <whitespaces>
    ge-command = <'>='> <whitespaces>
    eq-command = <'='> <whitespaces>
    ne-command = <'!='> <whitespaces>
    is-null-command = <('is null' | 'IS NULL' | 'Is null')> <whitespaces>
    is-not-null-command = <('is not null' | 'IS NOT NULL' | 'Is not null')> <whitespaces>
    starts-with-command = <('starts with' | 'STARTS WITH' | 'Starts with')> <whitespaces>
    ends-with-command = <('ends with' | 'ENDS WITH' | 'Ends with')> <whitespaces>
    contains-command = <('contains' | 'CONTAINS' | 'Contains')> <whitespaces>
    like-regex-command = <'=~'> <whitespaces>
    in-command = <('in' | 'IN' | 'In')> <whitespaces>
    (*--------------------------------COMMANDS---------------------------*)

    <word> = #'[a-zA-Z]+'
    <digit> = #'[0-9]'
    <whitespaces> = #'\\s+'"))


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
