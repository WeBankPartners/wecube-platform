#!/bin/bash

set -e

echo "GitHub action was triggered with:"
echo "- GITHUB_EVENT_NAME=${GITHUB_EVENT_NAME}"
echo "- GITHUB_REF=${GITHUB_REF}"


if [ "${GITHUB_REF/refs/}" != "${GITHUB_REF}" ]; then
	GITHUB_RELEASE_URL="${GITHUB_API_URL}/repos/${GITHUB_REPOSITORY}/latest"
else
	GITHUB_RELEASE_URL="${GITHUB_API_URL}/repos/${GITHUB_REPOSITORY}/tags/${GITHUB_REF}"
fi

RELEASE_JSON=$(curl -sSfL \
	--request GET "${GITHUB_RELEASE_URL}" \
	| jq --exit-status --arg access_token ${GITEE_API_TOKEN} \
		'{access_token: $access_token, name: .name, tag_name: .tag_name, target_commitish: .target_commitish, body: .body}'
)

TAG_NAME=$(jq -r '.tag_name' <<<"$RELEASE_JSON")

GITEE_RELEASE_ID=$(curl -sSfL \
	--request GET "https://gitee.com/api/v5/repos/${GITHUB_REPOSITORY}/releases/tags/${TAG_NAME}" 2>/dev/null \
	| jq --exit-status '.id'
)


if [ -z "${GITEE_RELEASE_ID}" ]; then
	curl -sSfL \
		--request POST "https://gitee.com/api/v5/repos/${GITHUB_REPOSITORY}/releases" \
		--header 'Content-Type: application/json;charset=UTF-8' \
		--data @- <<<"$RELEASE_JSON"
else
	curl -sSfL \
		--request PATCH "https://gitee.com/api/v5/repos/${GITHUB_REPOSITORY}/releases/${GITEE_RELEASE_ID}" \
		--header 'Content-Type: application/json;charset=UTF-8' \
		--data @- <<<"$RELEASE_JSON"
fi
