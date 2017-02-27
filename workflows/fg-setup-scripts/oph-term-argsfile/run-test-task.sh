#!/bin/bash
if [[ $# -ne 1 ]]; then
    echo 'Usage: run-test-task TOKEN'
    exit 1
fi

ID=$(curl -H "Authorization: Bearer $1" \
    http://localhost:8888/v1.0/applications \
    | jq --raw-output ".applications | map(select(.name | contains(\"oph-term-argsfile\")) | .id)[0]")

if [[ $ID == "null" ]]; then
    echo 'Application is not present in the database!'
    exit 2
fi

TASK=$(curl -H "Authorization: Bearer $1" \
    -H 'Content-Type: application/json' \
    -X POST -d "{\"application\":\"$ID\"}" \
    http://localhost:8888/v1.0/tasks \
    | jq --raw-output .id)

(cd example/
curl -H "Authorization: Bearer $1" \
    -F "file[]=@script.sh" \
    -F "file[]=@workflow.json" \
    -F "file[]=@oph-credentials.txt" \
    -F "file[]=@arguments.txt" \
    http://localhost:8888/v1.0/tasks/$TASK/input
)

echo "Monitor the job by executing:"
echo "watch \"curl -H 'Authorization: Bearer $1' http://localhost:8888/v1.0/tasks/$TASK\""
