#! /bin/bash
credentials=$(cat oph-credentials.txt)
user=${credentials%:*}
pass=${credentials#*:}
host=$1
port=$2
shift 2

exec python2 oph_workflow_check.py -e oph_term -u "$user" -p "$pass" -H "$host" -P "$port" -w workflow.json -a "$@"
