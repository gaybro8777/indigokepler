-- Log into FutureGateway, run alias 'asdb' (connection to MySQL) and execute statements below
-- This will create a new application with id 100 which will run a bash script to execute oph_term
-- NOTE: oph_term must be also installed and accessible to jobtest user on FutureGateway machine!
INSERT INTO application VALUES(100, 'ophidia-localhost', 'Run Ophidia terminal from localhost', 'JOB', NOW(), 1);

INSERT INTO application_file VALUES(100, 1, 'script.sh', '/home/futuregateway/FutureGateway/fgAPIServer/apps/ophidia-localhost', 0);
INSERT INTO application_file VALUES(100, 2, 'workflow.json', '/home/futuregateway/FutureGateway/fgAPIServer/apps/ophidia-localhost', 0);
INSERT INTO application_file VALUES(100, 3, 'oph-credentials.txt', '/home/futuregateway/FutureGateway/fgAPIServer/apps/ophidia-localhost', 0);

INSERT INTO application_parameter VALUES(100, 1, 'jobdesc_executable', '/bin/bash', NULL);
INSERT INTO application_parameter VALUES(100, 2, 'jobdesc_arguments', 'script.sh', NULL);
INSERT INTO application_parameter VALUES(100, 3, 'jobdesc_output', 'stdout.txt', NULL);
INSERT INTO application_parameter VALUES(100, 4, 'jobdesc_error', 'stderr.txt', NULL);
INSERT INTO application_parameter VALUES(100, 5, 'target_executor', 'GridEngine', NULL);

INSERT INTO infrastructure VALUES(1, 100, 'ophidia on localhost', 'Run oph_term as jobtest@localhost', NOW(), 1, 0);


-- Application with id 101 is *almost* identical. It only has one more input file: arguments.txt
INSERT INTO application VALUES(101, 'ophidia-localhost-input-file', 'Run Ophidia terminal from localhost + read arguments from input file', 'JOB', NOW(), 1);

INSERT INTO application_file VALUES(101, 1, 'script.sh', '/home/futuregateway/FutureGateway/fgAPIServer/apps/ophidia-localhost', 0);
INSERT INTO application_file VALUES(101, 2, 'workflow.json', '/home/futuregateway/FutureGateway/fgAPIServer/apps/ophidia-localhost', 0);
INSERT INTO application_file VALUES(101, 3, 'oph-credentials.txt', '/home/futuregateway/FutureGateway/fgAPIServer/apps/ophidia-localhost', 0);
INSERT INTO application_file VALUES(101, 4, 'arguments.txt', '/home/futuregateway/FutureGateway/fgAPIServer/apps/ophidia-localhost', 0);

INSERT INTO application_parameter VALUES(101, 1, 'jobdesc_executable', '/bin/bash', NULL);
INSERT INTO application_parameter VALUES(101, 2, 'jobdesc_arguments', 'script.sh', NULL);
INSERT INTO application_parameter VALUES(101, 3, 'jobdesc_output', 'stdout.txt', NULL);
INSERT INTO application_parameter VALUES(101, 4, 'jobdesc_error', 'stderr.txt', NULL);
INSERT INTO application_parameter VALUES(101, 5, 'target_executor', 'GridEngine', NULL);

INSERT INTO infrastructure VALUES(1, 101, 'ophidia on localhost', 'Run oph_term as jobtest@localhost', NOW(), 1, 0);
