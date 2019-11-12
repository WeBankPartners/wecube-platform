current_dir=$(shell pwd)
date=$(shell date +%Y%m%d%H%M%S)
version=$(shell bash  ./build/version.sh)
remote_docker_image_registry=ccr.ccs.tencentyun.com/webankpartners

clean:
	rm -rf $(current_dir)/platform-auth-client/target
	rm -rf $(current_dir)/platform-auth-server/target
	rm -rf $(current_dir)/platform-core/target
	rm -rf $(current_dir)/wecube-portal/node
	rm -rf $(current_dir)/wecube-portal/node_modules
	rm -rf $(current_dir)/wecube-portal/target
	rm -rf $(current_dir)/platform-gateway/target

.PHONY:build

build_name=wecube-build
build:
	mkdir -p repository
	docker run --rm --name $(build_name)  -e SASS_BINARY_SITE=https://npm.taobao.org/mirrors/node-sass -v /data/wecube_repository:/usr/src/mymaven/repository -v $(current_dir)/build/maven_settings.xml:/usr/share/maven/ref/settings-docker.xml  -v $(current_dir):/usr/src/mymaven -w /usr/src/mymaven maven:3.3-jdk-8 mvn -U clean install -Dmaven.test.skip=true -s /usr/share/maven/ref/settings-docker.xml dependency:resolve

image:
	docker build -t platform-core:$(version) -f build/platform-core/Dockerfile .
	docker build -t platform-gateway:$(version) -f build/platform-gateway/Dockerfile .
	docker build -t wecube-portal:$(version) -f build/wecube-portal/Dockerfile .

push:
	docker tag  platform-core:$(version) $(remote_docker_image_registry)/platform-core:$(date)-$(version)
	docker push $(remote_docker_image_registry)/platform-core:$(date)-$(version)

	docker tag  platform-gateway:$(version) $(remote_docker_image_registry)/platform-gateway:$(date)-$(version)
	docker push $(remote_docker_image_registry)/platform-gateway:$(date)-$(version)

	docker tag  wecube-portal:$(version) $(remote_docker_image_registry)/wecube-portal:$(date)-$(version)
	docker push $(remote_docker_image_registry)/wecube-portal:$(date)-$(version)

env_config=smoke_branch.cfg
target_host="tcp://10.0.0.1:2375"
deploy:
	docker tag  platform-core:$(version) $(remote_docker_image_registry)/platform-core:$(date)-$(version)
	docker tag  platform-gateway:$(version) $(remote_docker_image_registry)/platform-gateway:$(date)-$(version)
	docker tag  wecube-portal:$(version) $(remote_docker_image_registry)/wecube-portal:$(date)-$(version)
	
	docker push $(remote_docker_image_registry)/platform-core:$(date)-$(version)
	docker push $(remote_docker_image_registry)/platform-gateway:$(date)-$(version)
	docker push $(remote_docker_image_registry)/wecube-portal:$(date)-$(version)
	
	sh build/deploy_generate_compose.sh $(env_config) $(date)-$(version)
	docker-compose -f docker-compose.yml -H $(target_host) up -d
