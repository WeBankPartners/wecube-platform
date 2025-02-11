#!/bin/bash
set -e -x
cd /home/node/app/wecube-portal
npm install --registry https://registry.npmmirror.com --unsafe-perm
# npm install --registry https://mirrors.cloud.tencent.com/npm/
npm run build
