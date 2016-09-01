# Download files

Downloads specified files into local directory

## Download files


### Inputs

* trigger - this port can be used to control execution of the actor
* futureGatewayURI - address of Future Gateway installation
* authorizationToken - user's token for authorization
* outputFiles - list of files to be downloaded
* localFolder - local folder where files will be stored

### Outputs 

* output - BooleanToken with value true when files are retrieved successfully, false - otherwise.