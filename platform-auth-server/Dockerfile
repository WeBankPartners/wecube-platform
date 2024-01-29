FROM ccr.ccs.tencentyun.com/webankpartners/alpine-bash:v1

ENV BASE_HOME=/app/platform-auth-server

RUN mkdir -p $BASE_HOME $BASE_HOME/config $BASE_HOME/logs $BASE_HOME/config/private_keys $BASE_HOME/config/public_keys

ADD build/start.sh $BASE_HOME/
ADD build/stop.sh $BASE_HOME/
ADD build/config $BASE_HOME/config/
ADD platform-auth-server $BASE_HOME/

RUN addgroup -S apps -g 6000 && adduser -S app -u 6001 -G apps
RUN chown -R app:apps $BASE_HOME && chmod -R 755 $BASE_HOME
USER app

WORKDIR $BASE_HOME
ENTRYPOINT ["/bin/sh", "start.sh"]