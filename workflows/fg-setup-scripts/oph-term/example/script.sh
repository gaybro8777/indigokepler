#! /bin/bash
credentials=$(cat oph-credentials.txt)
user=${credentials%:*}
pass=${credentials#*:}
host=$1
port=$2
shift 2

oph_term -u "$user" -p "$pass" -H "$host" -P "$port" -e "./workflow.json $@" -j > output.json

curl https://raw.githubusercontent.com/FutureGateway/ENES-portlet/master/src/utils/ophidia_helper.py -o ophidia_helper.py
python2 ophidia_helper.py output.json oph-credentials.txt out.png

echo 'Finished!'
