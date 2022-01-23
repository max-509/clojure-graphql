(ns clojure-graphql.impl.language-parser
  (:require [instaparse.core :as insta]))

(def create-rule
  (insta/parser
    "clauses = clause (<whitespaces> clause)*
    clause = create | delete | match | set | undo | return | saveviz

    (*----------------------------CLAUSES DESCRIPTION-----------------------*)
    create = <create-command> patterns
    delete = <delete-command> variables
    match = <match-command> patterns where
    set = <set-command> set-params
    undo = <undo-command>
    saveviz = <saveviz-command> filepath
    return = <return-command> return-params
    (*----------------------------CLAUSES DESCRIPTION-----------------------*)

    (*----------------------------SUPPORT FOR CLAUSES-------------------------*)
    where = <where-command> predicates | Epsilon
    (*----------------------------SUPPORT FOR CLAUSES-------------------------*)

    (*----------------------------SET PARAMS-----------------------*)
    set-params = set-param (<comma> set-param)*
    set-param = assign-command | label-command
    label-command = var-labels
    assign-command = attribute <whitespaces> <eq-command> (data | null | external-param)
    external-param = <'$'> name
    (*----------------------------SET PARAMS-----------------------*)

    (*----------------------------RETURN PARAMS-----------------------*)
    return-params = (return-param (<comma> return-param)*) | all
    return-param = (return-param-var | return-param-field)
    return-param-var = variable-name
    return-param-field = attribute
    all = <asterisk>
    (*----------------------------RETURN PARAMS-----------------------*)

    (*----------------------------PATTERN DESCRIPTION-----------------------*)
    patterns = pattern (<comma> pattern)*
    pattern = node (relation node)*

    node = <left-bracket> variable-name labels properties <right-bracket>
    relation = dash edge right-arrow | left-arrow edge dash (* | dash edge dash *)
    edge = <left-square-bracket> variable-name labels properties <right-square-bracket> | Epsilon variable-name labels properties

    labels = (label)+ | Epsilon
    label = <':'> name

    properties = internal-properties | external-properties | Epsilon
    internal-properties = <left-curly-bracket> property (<whitespaces> property)* <right-curly-bracket>
    external-properties = <whitespaces>? <'$'> name <whitespaces>?
    property = field <':'> <whitespaces> data
    (*----------------------------PATTERN DESCRIPTION-----------------------*)

    (*----------------------------PREDICATES DESCRIPTION----------------------*)
    predicates = predicate (<whitespaces> boolean-operator predicate)*
    predicate = (brackets-predicates | atom-predicate)
    brackets-predicates = not-command? <left-bracket> predicates <right-bracket>
    (* For future *)
    (*atom-predicate = not-command? (label-check | field-check)*)

    atom-predicate = not-command? field-check


    label-check = var-labels
    field-check = attribute <whitespaces> comparing-operator data
    <comparing-operator> = lt-command
                        | le-command
                        | gt-command
                        | ge-command
                        | eq-command
                        | ne-command
                        | starts-with-command
                        | ends-with-command
                        | contains-command
                        | like-regex-command
                        | in-command
    <boolean-operator> = and-command | or-command | xor-command
    (*----------------------------PREDICATES DESCRIPTION----------------------*)

    (*----------------------------DATA TYPES-------------------------------*)
    <var-labels> = variable-name labels
    <attribute> = variable-name <'.'> field
    field = name
    variables = variable-name (<comma> variable-name)*
    variable-name = name | Epsilon
    <data> = string | integer | float | boolean | list
    list = <left-square-bracket> (string (<whitespaces> string)*
                                | integer (<whitespaces> integer)*
                                | float (<whitespaces> float)*
                                | boolean (<whitespaces> boolean)*
                                | list (<whitespaces> list)*
                                | Epsilon) <right-square-bracket>
    string = (<quote> string-val <quote>) | (<dquote> string-val <dquote>)
    integer = #'-?[0-9]+'
    float = #'-?([0-9]+.[0-9]+ | (INF | inf) | (NAN | nan))'
    boolean = 'True' | 'TRUE' | 'true' | 'False' | 'FALSE' | 'false'
    null = <('null' | 'NULL')>
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
    set-command = <('set' | 'SET' | 'Set')> <whitespaces>
    undo-command = <('undo' | 'UNDO' | 'Undo')> <whitespaces>?
    delete-command = <('delete' | 'DELETE' | 'Delete')> <whitespaces>
    saveviz-command = <('saveviz' | 'SAVEVIZ' | 'Saveviz')> <whitespaces>
    return-command = <('return' | 'RETURN' | 'Return')> <whitespaces>

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

    <filepath> = #'^(?:[a-z]:)?[\\/\\\\]{0,2}(?:[.\\/\\\\ ](?![.\\/\\\\\\n])|[^<>:\"|?*.\\/\\\\ \\n])+$'
    <comma> = ',' <whitespaces>?
    <asterisk> = '*'
    <dquote> = '\"'
    <quote> = \"'\"
    <string-val> = #'[\\x20-\\x21\\x23-\\x26\\x28-x7E]+'
    <name> = #'[a-zA-Z_$][a-zA-Z_$0-9]*'
    <digit> = #'[0-9]'
    <whitespaces> = #'\\s+'"))
