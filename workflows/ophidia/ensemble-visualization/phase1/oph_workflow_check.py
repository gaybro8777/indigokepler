#!/bin/python
# coding=utf-8
#
#     Python script for Ophidia workflow check		     
#     Copyright (C) 2016 CMCC Foundation
#
#     This program is free software: you can redistribute it and/or modify
#     it under the terms of the GNU General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     (at your option) any later version.
#
#     This program is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU General Public License for more details.
#
#     You should have received a copy of the GNU General Public License
#     along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

from __future__ import print_function
import argparse
import json
import os
import re
import subprocess
import sys
import time

if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Check Ophidia Workflow status and provide image file name')
    parser.add_argument('-e', '--execut', default="/usr/local/ophidia/oph-terminal/bin/oph_term",
                        help='Absolute path of Ophidia Terminal executable')
    parser.add_argument('-H', '--host', default="127.0.0.1", help='Ophidia server hostname or IP address')
    parser.add_argument('-P', '--port', default="11732", help='Ophidia server port')
    parser.add_argument('-u', '--user', default="oph-test", help='Ophidia username')
    parser.add_argument('-p', '--password', default="abcd", help='Ophidia password')
    parser.add_argument('-w', '--workflow', help='Absolute path of Ophidia Workflow JSON file', required=True)
    parser.add_argument('-a', '--args', help='List of workflow comma-separated arguments', required=True)

    args = parser.parse_args()

    # Input arguments
    terminal_exec = args.execut
    server_host = args.host
    server_port = args.port
    server_user = args.user
    server_pass = args.password
    workflow_name = args.workflow
    workflow_args = args.args

    # Common variables
    wid = 0

    # Run workflow async command
    try:
        workflow_resp = subprocess.check_output([
            terminal_exec
            + ' -H ' + server_host
            + ' -P ' + server_port
            + ' -u ' + server_user
            + ' -p ' + server_pass
            + ' -e "./' + workflow_name + ' ' + workflow_args + '"'
            + ' --json'],
            shell=True)
    except (OSError, CalledProcessError):
        sys.exit('Unable to run workflow command')

    try:
        workflow_json = json.loads(workflow_resp)
    except (ValueError, TypeError):
        sys.exit('Not a valid JSON')

    if "jobid" in workflow_json:
        jobid = workflow_json["jobid"]
    else:
        sys.exit('No jobid key in JSON response')

    reg = re.compile('.*experiment\?(.\d+?)#.*')

    # Get workflow ID
    workflow_id = reg.search(jobid)

    if workflow_id is not None:
        wid = workflow_id.group(1)
    # print wid
    else:
        sys.exit('No Workflow ID found in JSON response')

    if int(wid) == 0:
        sys.exit('Workflow ID not valid')

    # Crete dir for image if not exists
    img_path = os.getcwd()
    if not os.path.exists(img_path):
        try:
            os.mkdir(img_path)
        except OSError:
            sys.exit('Unable to create directory: ' + img_path)

    # Loop until end of workflow
    workflow_running = True
    reg2 = re.compile('.*Workflow Image File : (.+?).svg.*')

    while workflow_running:
        time.sleep(1)
        return_val = 0

        # Run view command
        try:
            proc = subprocess.Popen([
                'OPH_TERM_IMGS=save '
                + terminal_exec
                + ' -H ' + server_host
                + ' -P ' + server_port
                + ' -u ' + server_user
                + ' -p ' + server_pass
                + ' -e "view ' + wid + '" --json'],
                shell=True, cwd=img_path, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        except OSError:
            sys.exit('Unable to run view command')

        workflow_resp, workflow_error = proc.communicate()
        if workflow_error:
            sys.exit('Unable to get view command output')

        try:
            workflow_json = json.loads(workflow_resp)
            with open('output.json', 'w') as jsonfile:
                jsonfile.write(workflow_resp)
        except (ValueError, TypeError):
            sys.exit('Not a valid JSON')

        # Get image name
        if "stdout" in workflow_json:
            image_string = workflow_json["stdout"].encode('ascii', 'ignore')
        else:
            sys.exit('No stdout key in JSON response')

        image_name = reg2.search(image_string)

        if image_name is not None:
            print(img_path + '/' + image_name.group(1) + '.svg')
        else:
            sys.exit('Image name not available in JSON response')

        # Get workflow status
        if ("response" in workflow_json) and ("response" in workflow_json["response"]):
            resp_string = workflow_json["response"]["response"]
        else:
            sys.exit('Response key missing in JSON reposnse')

        for obj in resp_string:
            if ("objkey" in obj) and (obj["objkey"] == "workflow_status"):
                if ("objcontent" in obj) and (isinstance(obj["objcontent"], list)) and (
                            "message" in obj["objcontent"][0]):
                    print(obj["objcontent"][0]["message"])
                    if obj["objcontent"][0]["message"] == "OPH_STATUS_COMPLETED":
                        workflow_running = False
                    elif obj["objcontent"][0]["message"] == "OPH_STATUS_ERROR":
                        sys.exit('Workflow ended with error')
                    return_val = 1
                    break
                else:
                    sys.exit('Workflow status message not available in JSON response')

        # Status not found
        if return_val == 0:
            sys.exit('Workflow status not found in JSON response')

    exit(0)
