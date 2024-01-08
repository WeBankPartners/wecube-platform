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

build_auth_server:
	rm -f platform-auth-server/platform-auth-server
	chmod +x platform-auth-server/build/*.sh
	docker run --rm -v $(current_dir)/platform-auth-server:/go/src/github.com/WeBankPartners/wecube-platform/platform-auth-server --name build_platform-auth-server_authserver golang:1.18.0 /bin/bash /go/src/github.com/WeBankPartners/wecube-platform/platform-auth-server/build/build-server.sh

image_auth_server: build_auth_server
	docker build -t platform-auth-server:$(version) -f build/platform-auth-server/Dockerfile .

build_gateway:
	rm -f platform-gateway/platform-gateway
	chmod +x platform-gateway/build/*.sh
	docker run --rm -v $(current_dir)/platform-gateway:/go/src/github.com/WeBankPartners/wecube-platform/platform-gateway --name build_platform-gateway_authserver golang:1.18.0 /bin/bash /go/src/github.com/WeBankPartners/wecube-platform/platform-gateway/build/build-server.sh

image_gateway: build_gateway
	docker build -t platform-gateway:$(version) -f build/platform-gateway/Dockerfile .
	
