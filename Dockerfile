from platten/alpine-oracle-jre8-docker:8u202b08_server-jre
LABEL maintainer = "Webank CTB Team"
ADD build/init_cmdb  /home/init_cmdb
ADD wecube-core/target/wecube-core-1.0-SNAPSHOT.jar  /home/wecube-core.jar
ADD build/start.sh /start.sh
RUN chmod +x /start.sh
CMD ["/bin/sh","-c","/start.sh"]

