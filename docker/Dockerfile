FROM tomcat:9.0-alpine
LABEL maintainer="tobias-fassbender@gmx.de"

ADD genesis_project_server.war /usr/local/tomcat/webapps/
ADD config /usr/local/tomcat/lib/

EXPOSE 8080
CMD ["catalina.sh", "run"]