set -e -x
cd /home/node/app/wecube-portal
npm --registry https://registry.npmmirror.com install --unsafe-perm --force
npm install --force
npm run build
