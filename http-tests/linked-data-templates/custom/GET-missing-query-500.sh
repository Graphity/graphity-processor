#!/bin/bash

curl -w "%{http_code}\n" -f -s \
  -H "Accept: application/n-triples" \
  "${BASE_URL}missing-query" \
| grep -q "${STATUS_INTERNAL_SERVER_ERROR}"