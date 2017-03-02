#!/bin/bash
if [[ $# -ne 1 ]]; then
    echo 'Usage: setup TOKEN'
    exit 1
fi

curl -H "Authorization: Bearer $1" \
    -H 'Content-Type: application/json' \
    -X POST -d "$(cat application.json)" \
    http://localhost:8888/v1.0/applications
