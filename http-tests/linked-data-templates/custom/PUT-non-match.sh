#!/bin/bash

curl -w "%{http_code}\n" -f -s \
  -X PUT \
  -H "Accept: application/n-quads" \
  -H "Content-Type: application/n-quads" \
  "${BASE_URL_WRITABLE}non-match" \
| grep -q "${STATUS_CREATED}"