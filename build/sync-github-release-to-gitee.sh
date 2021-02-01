#!/bin/bash

set -e

echo "GitHub action was triggered with:"
echo "- GITHUB_EVENT_NAME=${GITHUB_EVENT_NAME}"
echo "- GITHUB_REF=${GITHUB_REF}"


if [ "${GITHUB_REF/refs/}" != "${GITHUB_REF}" ]; then
	GITHUB_RELEASE_URL="${GITHUB_API_URL}/repos/${GITHUB_REPOSITORY}/releases/latest"
else
	GITHUB_RELEASE_URL="${GITHUB_API_URL}/repos/${GITHUB_REPOSITORY}/releases/tags/${GITHUB_REF}"
fi

echo "Fetching GitHub release from \"${GITHUB_RELEASE_URL}\"..."
RELEASE_JSON=$(curl -sSfL \
	--request GET "${GITHUB_RELEASE_URL}" \
	| jq --exit-status --arg access_token ${GITEE_API_TOKEN} \
		'{access_token: $access_token, name: .name, tag_name: .tag_name, target_commitish: .target_commitish, body: .body}'
)

TAG_NAME=$(jq -r '.tag_name' <<<"$RELEASE_JSON")

echo "Fetching Gitee release for tag \"${TAG_NAME}\"..."
GITEE_RELEASE_ID=$(curl -sSfL \
	--request GET "https://gitee.com/api/v5/repos/${GITHUB_REPOSITORY}/releases/tags/${TAG_NAME}" 2>/dev/null \
	| jq --exit-status '.id'
)


if [ -z "${GITEE_RELEASE_ID}" ]; then
	echo "Creating new Gitee release..."
	curl -sSfL \
		--request POST "https://gitee.com/api/v5/repos/${GITHUB_REPOSITORY}/releases" \
		--header 'Content-Type: application/json;charset=UTF-8' \
		--data @- <<<"$RELEASE_JSON"
else
	echo "Patching Gitee release for id \"${GITEE_RELEASE_ID}\""
	curl -sSfL \
		--request PATCH "https://gitee.com/api/v5/repos/${GITHUB_REPOSITORY}/releases/${GITEE_RELEASE_ID}" \
		--header 'Content-Type: application/json;charset=UTF-8' \
		--data @- <<<"$RELEASE_JSON"
fi
