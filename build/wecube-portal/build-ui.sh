#!/bin/bash
set -e -x
cd /home/node/app/wecube-portal
npm cache clean --force
npm install --force
# npm install --registry https://mirrors.cloud.tencent.com/npm/
npm run build
