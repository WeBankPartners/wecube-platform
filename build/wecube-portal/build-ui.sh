set -e -x
cd /home/node/app/wecube-portal
# npm --registry https://registry.npmmirror.com install --unsafe-perm --force
npm config set registry https://registry.npmjs.org/
npm install --force
npm run build
