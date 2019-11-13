from platten/alpine-oracle-jre8-docker:latest
LABEL maintainer = "Webank CTB Team"
ADD platform-core/target/platform-core-1.0-SNAPSHOT.jar  /application/platform-core.jar
ADD build/platform-core/start_platform_core.sh /scripts/start.sh
RUN chmod +x /scripts/start.sh
CMD ["/bin/sh","-c","/scripts/start.sh"]

