You need to modify SSH password for jobtest user in application.json file.

Then, install new application `./setup.sh AUTH_TOKEN`

Then, prepare a test task by putting `user:password` in the `example/oph-credentials.txt` file and run `./run-test-task.sh AUTH_TOKEN`
