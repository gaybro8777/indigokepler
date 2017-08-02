#!/bin/bash
if [[ $# -ne 2 ]]; then
    echo 'Usage: setup URI TOKEN'
    exit 1
fi

curl -H "Authorization: Bearer $2" \
    -H 'Content-Type: application/json' \
    -X POST -d "$(cat application.json)" \
    "$1/v1.0/applications"
