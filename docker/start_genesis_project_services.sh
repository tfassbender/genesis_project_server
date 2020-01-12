# stop all old services
printf "docker ps\n"
docker ps
printf "\n"
docker stop genesis_project_genesis_project_service_1 genesis_project_mysql_1 jfg_notifier_jfg_notifier_1

# start the new ones
./build_and_run.sh
printf "\n\n"
cd ../jfg_notifier/
./build_and_run.sh

# print the active dockers on the console
printf "\n\ndocker ps\n"
docker ps