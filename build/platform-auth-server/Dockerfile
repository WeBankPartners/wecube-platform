from platten/alpine-oracle-jre8-docker:latest
LABEL maintainer = "Webank CTB Team"
ADD platform-auth-server/target/platform-auth-server-3.0-SNAPSHOT.jar  /application/platform-auth-server.jar
ADD build/platform-auth-server/start_auth_server.sh /scripts/start.sh
RUN chmod +x /scripts/start.sh
CMD ["/bin/sh","-c","/scripts/start.sh"]

