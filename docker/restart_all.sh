#!/bin/bash

sudo rm -v logs/genesis_project_service.log
docker stop genesis_project_genesis_project_service_1 genesis_project_mysql_1
#sudo rm -rfv ../mysql_docker_volumes/genesis_project/
./build_and_run.sh
