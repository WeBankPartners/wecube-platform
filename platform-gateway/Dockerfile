FROM ccr.ccs.tencentyun.com/webankpartners/alpine-base:v1.0
LABEL maintainer = "Webank CTB Team"

ENV BASE_HOME=/app/tradesys

RUN mkdir -p $BASE_HOME $BASE_HOME/config $BASE_HOME/config/i18n $BASE_HOME/config/private_key $BASE_HOME/config/public_key

ADD build/start.sh $BASE_HOME/
ADD build/stop.sh $BASE_HOME/
ADD build/config $BASE_HOME/config/
ADD platform-gateway $BASE_HOME/

RUN addgroup -S apps -g 6000 && adduser -S app -u 6001 -G apps
RUN chown -R app:apps $BASE_HOME && chmod -R 755 $BASE_HOME
USER app

WORKDIR $BASE_HOME
ENTRYPOINT ["/bin/sh", "start.sh"]

