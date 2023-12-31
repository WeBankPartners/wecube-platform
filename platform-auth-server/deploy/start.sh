#!/bin/bash
IMAGE_DIR='./images/'
images=$(find $IMAGE_DIR -name '*.tar')
for i in ${images[@]}
do
    docker load --input "$i"
done

docker-compose -f docker-compose.yml up -d
sleep 5
# health check
http_code=`curl -X GET -o /dev/null -s -w '%{http_code}' 'http://[@HOSTIP]:[#port]/user-mgmt/v1/health-check'`
if [[ "$http_code" == "200" ]];then
    echo "heath check: ok"
    exit 0
else
    echo "heath check: failed"
    exit 1
fi