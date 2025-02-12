#!/bin/bash
set -e -x
cd /home/node/app/wecube-portal
npm install --legacy-peer-depss
# npm install --registry https://mirrors.cloud.tencent.com/npm/
npm run build
