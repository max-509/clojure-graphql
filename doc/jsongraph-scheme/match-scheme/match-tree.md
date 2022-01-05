## Example of expression tree by WHERE

### Terms

Define as `predicate` expression of the view:

- `<var>.<property> <cs> <value>`

where:

- `<var>` - variable name
- `<property>` - property name
- `<cs>` - comparing operation
- `<value>` - value with that will compare

Predicates can be union to composite expression with help `AND`, `OR`, `NOT` operators. For example:

- `predicate1 AND predicate2 OR predicate3 AND NOT (predicate4 OR predicate5)`

### Inner view

WHERE-expression will pass by expression tree. Expression tree has next view:

Example of WHERE-expression:

`a.name = 'Name' AND b.surname = 'Surname' OR a.child = 'Boy'`

Example of WHERE-expression-tree:

```
├───AND
│   ├───a.name = 'Name'
│   └───OR
│       ├───b.surname = 'Surname'
│       └───a.child = 'Boy'
```

Example of possible Clojure view:

```clojure
([:binary-op :and]
 ([:pred {:type :field-check :val [{:name "a" :property "name"} :eq "Name"]}])
 ([:binary-op :or]
  ([:pred {:type :field-check :val [{:name "b" :property "surname"} :eq "Surname"]}])
  ([:pred {:type :field-check :val [{:name "a" :property "child"} :eq "Boy"]}])))
```

This tree
