current_dir:=$(shell pwd)
date:=$(shell date +%Y%m%d%H%M%S)
version:=$(VERSION)
tencent_cloud_docker_image_registry=ccr.ccs.tencentyun.com/webankpartners

clean:
	rm -rf $(current_dir)/platform-core/platform-core
	rm -rf $(current_dir)/wecube-portal/node
	rm -rf $(current_dir)/wecube-portal/dist
	rm -rf $(current_dir)/platform-gateway/platform-gateway
	rm -rf $(current_dir)/platform-auth-server/platform-auth-server

build_core:
	rm -f platform-core/platform-core
	chmod +x platform-core/build/*.sh
	docker run --rm -v $(current_dir)/platform-core:/go/src/github.com/WeBankPartners/wecube-platform/platform-core golang:1.19.1 /bin/bash /go/src/github.com/WeBankPartners/wecube-platform/platform-core/build/build-server.sh

image_core: build_core
	docker build -t platform-core:$(version) platform-core/.
