#!/bin/bash
set -e -x
cd /home/node/app/wecube-portal
npm set registry https://mirrors.cloud.tencent.com/npm/

# Reinstall only when the dependency manifest has changed.  node_modules is
# mounted from the host, so retaining this marker avoids a full npm install for
# ordinary UI-only builds.
dependency_hash=$(sha256sum package.json | awk '{print $1}')
dependency_marker=node_modules/.wecube-package-json.sha256
if [[ -f $dependency_marker ]] && [[ $(<"$dependency_marker") == "$dependency_hash" ]]; then
    echo "package.json unchanged; reusing existing node_modules"
elif [[ -d node_modules ]] && npm ls --depth=0 --silent >/dev/null 2>&1; then
    # Existing dependencies may predate this cache marker (for example after
    # upgrading this script).  Validate them once, then start reusing them.
    printf '%s\n' "$dependency_hash" > "$dependency_marker"
    echo "existing node_modules validated; reusing it"
else
    npm install --no-package-lock
    printf '%s\n' "$dependency_hash" > "$dependency_marker"
fi
npm run build
