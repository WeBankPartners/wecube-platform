#!/bin/bash
set -e -x
cd $(dirname $0)/../
go build -ldflags "-linkmode external -extldflags -static -s"