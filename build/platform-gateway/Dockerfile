from platten/alpine-oracle-jre8-docker:latest
LABEL maintainer = "Webank CTB Team"
ADD platform-gateway/target/platform-gateway-3.0-SNAPSHOT.jar  /application/platform-gateway.jar
ADD build/platform-gateway/start_gateway.sh /scripts/start.sh
RUN chmod +x /scripts/start.sh
CMD ["/bin/sh","-c","/scripts/start.sh"]

