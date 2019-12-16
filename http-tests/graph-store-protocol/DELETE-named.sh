#!/bin/bash

# re-initialize writable dataset

initialize_dataset "$BASE_URL_WRITABLE" "../dataset.trig" "$ENDPOINT_URL_WRITABLE"

# use conneg to request N-Triples as the preferred format

curl -w "%{http_code}\n" -f -s -G \
  -X DELETE \
  "${BASE_URL_WRITABLE}service" \
  --data-urlencode "graph=${BASE_URL_WRITABLE}graph-name" \
| grep -q "${STATUS_NO_CONTENT}"

curl -w "%{http_code}\n" -f -s -G \
  -H "Accept: application/n-triples" \
  "${BASE_URL_WRITABLE}service" \
  --data-urlencode "graph=${BASE_URL_WRITABLE}graph-name" \
| grep -q "${STATUS_NOT_FOUND}"