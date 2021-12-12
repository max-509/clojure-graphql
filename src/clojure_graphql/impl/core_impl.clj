(ns clojure-graphql.impl.core-impl
  (:require [instaparse.core :as insta]))

(def create-rule
  (insta/parser
    "clauses = clause (<whitespaces> clause)*
    clause = create | match

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
    relationship = dash relation right-arrow | left-arrow relation dash (* | dash relation dash *)
    relation = <left-square-bracket> variable-name labels properties <right-square-bracket> | Epsilon variable-name labels properties

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
    list = <left-square-bracket> (string (<whitespaces> string)*
                                | integer (<whitespaces> integer)*
                                | float (<whitespaces> float)*
                                | boolean (<whitespaces> boolean)*
                                | list (<whitespaces> list)*
                                | Epsilon) <right-square-bracket>
    string = <'\"'> word <'\"'>
    integer = #'-?[0-9]+'
    float = #'-?([0-9]+.[0-9]+ | (INF | inf) | (NAN | nan))'
    boolean = 'True' | 'TRUE' | 'true' | 'False' | 'FALSE' | 'false'
    (*----------------------------DATA TYPES-------------------------------*)

    (*-------------------------------------SPECIAL CONSTRUCTS--------------------------------*)
    right-arrow = <whitespaces>? <'->'> <whitespaces>?
    left-arrow = <whitespaces>? <'<-'> <whitespaces>?
    dash = <whitespaces>? <'-'> <whitespaces>?
    left-curly-bracket = <whitespaces>? <'{'> <whitespaces>?
    right-curly-bracket = <whitespaces>? <'}'> <whitespaces>?
    left-square-bracket = <whitespaces>? <'['> <whitespaces>?
    right-square-bracket = <whitespaces>? <']'> <whitespaces>?
    left-bracket = <whitespaces>? <'('> <whitespaces>?
    right-bracket = <whitespaces>? <')'> <whitespaces>?
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
