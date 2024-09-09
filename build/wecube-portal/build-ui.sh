#!/bin/bash
set -e -x
cd /home/node/app/wecube-portal
#npm install --registry https://registry.npmmirror.com --unsafe-perm --force
npm run build
