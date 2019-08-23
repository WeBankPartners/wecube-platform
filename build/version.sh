#!/bin/bash
COMMIT=$(git rev-parse --short HEAD)
GIT_TAG=$(git tag -l --contains HEAD | head -n 1)


if [[ -z "$DIRTY" && -n "$GIT_TAG" ]]; then
    VERSION=$GIT_TAG
else
    VERSION="${COMMIT}${DIRTY}"
fi

if [ -z $VERSION ];then
    VERSION="dev"
fi

echo $VERSION


