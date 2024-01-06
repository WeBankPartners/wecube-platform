#!/bin/bash
set -e -x
cd $(dirname $0)/../../platform-gateway/
go build -ldflags "-linkmode external -extldflags -static -s"
