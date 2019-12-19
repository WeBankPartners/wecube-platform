current_dir:=$(shell pwd)
date:=$(shell date +%Y%m%d%H%M%S)
version:=$(shell bash  ./build/version.sh)
remote_docker_image_registry=ccr.ccs.tencentyun.com/webankpartners

clean:
	rm -rf $(current_dir)/platform-auth-client/target
	rm -rf $(current_dir)/platform-auth-server/target
	rm -rf $(current_dir)/platform-core/target
	rm -rf $(current_dir)/wecube-portal/node
	rm -rf $(current_dir)/wecube-portal/node_modules
	rm -rf $(current_dir)/wecube-portal/dist
	rm -rf $(current_dir)/platform-gateway/target
	rm -rf $(current_dir)/platform-auth-server/target

.PHONY:build

build_name=wecube-build
build:
	mkdir -p repository
	docker run --rm --name $(build_name)  -e SASS_BINARY_SITE=https://npm.taobao.org/mirrors/node-sass -v /data/wecube_repository:/usr/src/mymaven/repository -v $(current_dir)/build/maven_settings.xml:/usr/share/maven/ref/settings-docker.xml  -v $(current_dir):/usr/src/mymaven -w /usr/src/mymaven maven:3.3-jdk-8 mvn -U clean install -Dmaven.test.skip=true -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
	docker run --rm --name $(build_name)_node  -v $(current_dir)/wecube-portal:/home/node/app -w /home/node/app node:12.13.1 npm --registry https://registry.npm.taobao.org install --unsafe-perm
	docker run --rm --name $(build_name)_node  -v $(current_dir)/wecube-portal:/home/node/app -w /home/node/app node:12.13.1 npm rebuild node-sass
	docker run --rm --name $(build_name)_node  -v $(current_dir)/wecube-portal:/home/node/app -w /home/node/app node:12.13.1 npm run build

image:
	docker build -t platform-core:$(version) -f build/platform-core/Dockerfile .
	docker build -t platform-gateway:$(version) -f build/platform-gateway/Dockerfile .
	docker build -t wecube-portal:$(version) -f build/wecube-portal/Dockerfile .
	docker build -t platform-auth-server:$(version) -f build/platform-auth-server/Dockerfile .

push:
	docker tag  platform-core:$(version) $(remote_docker_image_registry)/platform-core:$(date)-$(version)
	docker push $(remote_docker_image_registry)/platform-core:$(date)-$(version)

	docker tag  platform-gateway:$(version) $(remote_docker_image_registry)/platform-gateway:$(date)-$(version)
	docker push $(remote_docker_image_registry)/platform-gateway:$(date)-$(version)

	docker tag  wecube-portal:$(version) $(remote_docker_image_registry)/wecube-portal:$(date)-$(version)
	docker push $(remote_docker_image_registry)/wecube-portal:$(date)-$(version)

	docker tag  platform-auth-server:$(version) $(remote_docker_image_registry)/platform-auth-server:$(date)-$(version)
	docker push $(remote_docker_image_registry)/platform-auth-server:$(date)-$(version)

env_config=smoke_branch.cfg
target_host="tcp://10.0.0.1:2375"
deploy:
	docker tag  platform-core:$(version) $(remote_docker_image_registry)/platform-core:$(date)-$(version)
	docker push $(remote_docker_image_registry)/platform-core:$(date)-$(version)

	docker tag  platform-gateway:$(version) $(remote_docker_image_registry)/platform-gateway:$(date)-$(version)
	docker push $(remote_docker_image_registry)/platform-gateway:$(date)-$(version)

	docker tag  wecube-portal:$(version) $(remote_docker_image_registry)/wecube-portal:$(date)-$(version)
	docker push $(remote_docker_image_registry)/wecube-portal:$(date)-$(version)
	
	docker tag  platform-auth-server:$(version) $(remote_docker_image_registry)/platform-auth-server:$(date)-$(version)
	docker push $(remote_docker_image_registry)/platform-auth-server:$(date)-$(version)
	
	sh build/deploy_generate_compose.sh $(env_config) $(date)-$(version)
	docker-compose -f docker-compose.yml -H $(target_host) up -d
