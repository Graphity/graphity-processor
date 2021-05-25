#!/bin/bash

# check that parameter state is included in the RDF response (HATEOAS)

curl -f -s \
  -H "Accept: application/n-triples" \
  "${BASE_URL}mandatory-param?object=mandatory%20object" \
| rapper -q --input ntriples --output ntriples /dev/stdin - \
| tr -s '\n' '\t' \
| grep "<${BASE_URL}mandatory-param?object=mandatory%20object> <https://www.w3.org/ns/ldt/core/domain#stateOf> <${BASE_URL}mandatory-param> ." > /dev/null