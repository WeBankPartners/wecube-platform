from platten/alpine-oracle-jre8-docker:latest
LABEL maintainer = "Webank CTB Team"
ADD build/init_cmdb  /application/init_cmdb 
ADD wecube-core/target/wecube-core-1.0-SNAPSHOT.jar  /application/wecube-core.jar
ADD build/start.sh /scripts/start.sh
RUN chmod +x /scripts/start.sh
CMD ["/bin/sh","-c","/scripts/start.sh"]

