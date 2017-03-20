#!/bin/bash
if [[ $# -ne 1 ]]; then
    echo 'Usage: run-test-task TOKEN'
    exit 1
fi

ID=$(curl -H "Authorization: Bearer $1" \
    http://localhost:8888/v1.0/applications \
    | jq --raw-output ".applications | map(select(.name | contains(\"oph-term\")) | .id)[0]")

if [[ $ID == "null" ]]; then
    echo 'Application is not present in the database!'
    exit 2
fi

TASK=$(curl -H "Authorization: Bearer $1" \
    -H 'Content-Type: application/json' \
    -X POST -d "{\"application\":\"$ID\",\"arguments\":[\"193.204.199.174 11732 \\\"8 CMCC-CM rcp85 day 0.9 1976_2006 2071_2101 -90:90|0:360 r360x180\\\"\"]}" \
    http://localhost:8888/v1.0/tasks \
    | jq --raw-output .id)

(cd example/
curl -H "Authorization: Bearer $1" \
    -F "file[]=@script.sh" \
    -F "file[]=@workflow.json" \
    -F "file[]=@oph-credentials.txt" \
    http://localhost:8888/v1.0/tasks/$TASK/input
)

echo "Monitor the job by executing:"
echo "watch \"curl -H 'Authorization: Bearer $1' http://localhost:8888/v1.0/tasks/$TASK\""
