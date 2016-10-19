#! /bin/bash
credentials=$(cat oph-credentials.txt)
user=${credentials%:*}
pass=${credentials#*:}

arguments=($(cat arguments.txt))
host=${arguments[0]}
port=${arguments[1]}
unset arguments[0]
unset arguments[1]

python2 oph_workflow_check.py -e oph_term -u "$user" -p "$pass" -H "$host" -P "$port" -w workflow.json -a "${arguments[@]}"

curl https://raw.githubusercontent.com/tzok/ENES-portlet/develop/src/utils/ophidia_helper.py -o ophidia_helper.py
python2 ophidia_helper.py output.json 'Post (5)' oph-credentials.txt avg.png avg.png
python2 ophidia_helper.py output.json 'Post (5)' oph-credentials.txt min.png min.png
python2 ophidia_helper.py output.json 'Post (5)' oph-credentials.txt max.png max.png
python2 ophidia_helper.py output.json 'Post (5)' oph-credentials.txt var.png var.png
python2 ophidia_helper.py output.json 'Post (5)' oph-credentials.txt std.png std.png

echo 'Finished!'
