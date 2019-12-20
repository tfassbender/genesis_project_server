#create port and password enviroment variables
export TOMCAT_EXTERNAL_PORT=<a_port_on_which_your_service_shall_be_visible_to_the_outside_world>
export MYSQL_DOCKER_ROOT_PASSWORD=<a_root_password>
export MYSQL_DOCKER_USER_PASSWORD=<a_user_password>
export MYSQL_DOCKER_DATABASE=genesis_project
export MYSQL_DOCKER_USER=genesis_project
export MYSQL_DOCKER_VOLUME=genesis_project

#make the logs reachable for everyone (because they are created by root, which can cause problems)
sudo chmod 777 logs/*

#run docker-compose
docker-compose build
docker-compose up &
#the docker container will be started in the background
#use 'docker ps' to see running containers
#use 'docker stop <name_of_container>' to stop the container

#start the service by requesting anything (otherwhise the socket connection can not be established)
#wait 20 seconds before the request to give the docker time to start
sleep 20
curl localhost:${TOMCAT_EXTERNAL_PORT}/genesis_project_server/genesis_project/genesis_project/hello 

#unset the variables
unset TOMCAT_EXTERNAL_PORT
unset MYSQL_DOCKER_ROOT_PASSWORD
unset MYSQL_DOCKER_USER_PASSWORD
unset MYSQL_DOCKER_DATABASE
unset MYSQL_DOCKER_USER
unset MYSQL_DOCKER_VOLUME