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

push_core: image_core
	docker tag  platform-core:$(version) $(tencent_cloud_docker_image_registry)/platform-core:$(version)
	docker push $(tencent_cloud_docker_image_registry)/platform-core:$(version)

build_auth_server:
	rm -f platform-auth-server/platform-auth-server
	chmod +x platform-auth-server/build/*.sh
	docker run --rm -v $(current_dir)/platform-auth-server:/go/src/github.com/WeBankPartners/wecube-platform/platform-auth-server golang:1.19.1 /bin/bash /go/src/github.com/WeBankPartners/wecube-platform/platform-auth-server/build/build-server.sh

image_auth_server: build_auth_server
	docker build -t platform-auth-server:$(version) platform-auth-server/.

push_auth_server: image_auth_server
	docker tag  platform-auth-server:$(version) $(tencent_cloud_docker_image_registry)/platform-auth-server:$(version)
	docker push $(tencent_cloud_docker_image_registry)/platform-auth-server:$(version)

build_gateway:
	rm -f platform-gateway/platform-gateway
	chmod +x platform-gateway/build/*.sh
	docker run --rm -v $(current_dir)/platform-gateway:/go/src/github.com/WeBankPartners/wecube-platform/platform-gateway golang:1.19.1 /bin/bash /go/src/github.com/WeBankPartners/wecube-platform/platform-gateway/build/build-server.sh

image_gateway: build_gateway
	docker build -t platform-gateway:$(version) platform-gateway/.

push_gateway: image_gateway
	docker tag  platform-gateway:$(version) $(tencent_cloud_docker_image_registry)/platform-gateway:$(version)
	docker push $(tencent_cloud_docker_image_registry)/platform-gateway:$(version)

build_portal:
	rm -rf wecube-portal/node
	rm -rf wecube-portal/dist
	chmod +x build/wecube-portal/*.sh
	docker run --rm  -v $(current_dir):/home/node/app -w /home/node/app node:12.13.1 /bin/bash /home/node/app/build/wecube-portal/build-ui.sh

image_portal: build_portal
	docker build -t wecube-portal:$(version) -f build/wecube-portal/Dockerfile .

push_portal: image_portal
	docker tag  wecube-portal:$(version) $(tencent_cloud_docker_image_registry)/wecube-portal:$(version)
	docker push $(tencent_cloud_docker_image_registry)/wecube-portal:$(version)
