You need to modify SSH password for jobtest user in application.json file. Also, check if the path for input files is correct.

Then, install new application `./setup.sh URI AUTH_TOKEN`

`URI` is a base URI of the FutureGateway API server e.g. https://fgw01.ncg.ingrid.pt/apis

`AUTH_TOKEN` is the IAM token taken from e.g. https://iam-test.indigo-datacloud.eu/iam-test-client/

Then, prepare a test task by putting `user:password` in the `example/oph-credentials.txt` file and run `./run-test-task.sh URI AUTH_TOKEN`
