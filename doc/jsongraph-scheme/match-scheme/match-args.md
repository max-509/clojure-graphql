## Match arguments for query

Example of match-query:

```
  MATCH (a:Person)-[b:ACTED_IN]->(:Film)
  WHERE a.name = "Morgan" AND b.salary > 10000
 ```

Arguments for match:

- List of nodes:

```clojure
[
 {:var-name "a" :var-value <node-value-generated-by-gen-node>}
 {:var-name "" :var-value <node-value-generated-by-gen-node>} ; Node without name
 ]
```

- List of edges:
```clojure
[
 {:var-name "b" :var-value <edge-value-generated-by-gen-edge>}
 ]
```

- WHERE-expression-tree:

```clojure
([:binary-op :and]
 ([:pred {:type :field-check :val [{:name "a" :property "name"} :eq "Morgan"]}])
 ([:pred {:type :field-check :val [{:name "b" :property "salary"} :gt 10000]}]))
```

Firstly, need find all nodes-edges in graph, which suited by nodes-edges list (For Denis).

Secondly, founded nodes-edges in graph need filter by WHERE-expression-tree (For Maxim).