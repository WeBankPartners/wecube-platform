#!/bin/bash

set -e

RELEASE_JSON=$(curl -sSfL \
	--reqeust GET "${GITHUB_API_URL}/repos/${GITHUB_REPOSITORY}/releases/latest" \
	| jq --exit-status --arg access_token ${GITEE_API_TOKEN} \
		'{access_token: $access_token, name: .name, tag_name: .tag_name, target_commitish: .target_commitish, body: .body}'
)
curl -sSfL \
	--request POST "https://gitee.com/api/v5/repos/${GITHUB_REPOSITORY}/releases" \
	--header 'Content-Type: application/json;charset=UTF-8' \
	--data @- <<<"$RELEASE_JSON"
