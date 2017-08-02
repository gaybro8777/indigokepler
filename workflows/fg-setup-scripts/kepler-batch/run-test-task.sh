#!/bin/bash
if [[ $# -ne 2 ]]; then
    echo 'Usage: run-test-task URI TOKEN'
    exit 1
fi

ID=$(curl -H "Authorization: Bearer $2" \
    "$1/v1.0/applications" \
    | jq --raw-output ".applications | map(select(.name | contains(\"kepler-batch\")) | .id)[0]")

if [[ $ID == "null" ]]; then
    echo 'Application is not present in the database!'
    exit 2
fi

TASK=$(curl -H "Authorization: Bearer $2" \
    -H 'Content-Type: application/json' \
    -X POST -d "{\"application\":\"$ID\"}" \
    "$1/v1.0/tasks" \
    | jq --raw-output .id)

(cd example/
curl -H "Authorization: Bearer $2" \
    -F "file[]=@template.yaml" \
    -F "file[]=@parameters.json" \
    "$1/v1.0/tasks/$TASK/input"
)

echo "Monitor the job by executing:"
echo "watch \"curl -H 'Authorization: Bearer $2' $1/v1.0/tasks/$TASK\""
