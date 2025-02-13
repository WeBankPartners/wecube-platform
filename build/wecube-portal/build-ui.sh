set -e -x
cd /home/node/app/wecube-portal
# npm --registry https://registry.npmmirror.com install --unsafe-perm --force
npm cache clean --force
npm set registry https://mirrors.cloud.tencent.com/npm/
npm install --force
npm run build
