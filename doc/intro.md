# Introduction to clojure-graphql

TODO: write [great documentation](http://jacobian.org/writing/what-to-write/)

Graph
The Graph model is Adjacency list. This format is more convenient for Graph database.

See files graph.json and graph_empty.json for an example.

- adjacency -- list of all graph nodes. 
- metadata  -- metadata of graph (maybe useful)

Each node contain
- out-edges -- list of nodes names and data of edge.
- node-data -- data of node