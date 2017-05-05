#! /bin/bash
credentials=$(cat oph-credentials.txt)
user=${credentials%:*}
pass=${credentials#*:}
host=$1
port=$2
futuregateway=$3
shift 3

# create empty output.json because if Ophidia server fails, then the workflow
# will be able to deal with empty results but will not be able to deal with
# '404 Not Found'
touch output.json

python2 oph_workflow_check.py -e oph_term -u "$user" -p "$pass" -H "$host" -P "$port" -w workflow.json -f "$futuregateway" -a "$@"
# finish up with exit code 0 even if the Python script fails
exit 0
