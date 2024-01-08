FROM ccr.ccs.tencentyun.com/webankpartners/alpine-bash:v1

ENV BASE_HOME=/app/platform-core

RUN mkdir -p $BASE_HOME $BASE_HOME/config $BASE_HOME/logs

ADD build/start.sh $BASE_HOME/
ADD config/default.json $BASE_HOME/config/
ADD config/i18n $BASE_HOME/config/i18n/
ADD platform-core $BASE_HOME/

WORKDIR $BASE_HOME
RUN addgroup -S apps -g 6000 && adduser -S app -u 6001 -G apps
RUN chown -R app:apps $BASE_HOME && chmod -R 755 $BASE_HOME
USER app
ENTRYPOINT ["/bin/bash", "start.sh"]