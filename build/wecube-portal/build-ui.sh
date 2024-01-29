#!/bin/bash
set -e -x
cd /home/node/app/wecube-portal
npm --registry https://registry.npm.taobao.org install --unsafe-perm
npm run build