#!/bin/bash

# use conneg to request N-Triples as the preferred format

curl -f -s \
  -H "Accept: application/n-triples" \
  "${BASE_URL}sparql" \
  --data-urlencode "query=CONSTRUCT { <${BASE_URL}named-subject> <http://example.com/named-predicate> ?o } { GRAPH <${BASE_URL}graph-name> { <${BASE_URL}named-subject> <http://example.com/named-predicate> ?o } }" \
| rapper -q --input nquads --output nquads /dev/stdin - \
| tr -s '\n' '\t' \
| grep '"named object"' \
| grep "${BASE_URL}named-object" > /dev/null