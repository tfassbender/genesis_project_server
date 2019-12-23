#Create the test database and grand the permissions. The database name has to be the same in the configuration file 'config/test.properties'.
#WARNING: this script will only be executed if there are NO DATABASES already existing in the volume.
CREATE DATABASE `genesis_project_test`;
GRANT ALL ON `genesis_project`.* TO `genesis_project`@`%`;
GRANT ALL ON `genesis_project_test`.* TO `genesis_project`@`%`;
FLUSH PRIVILEGES;