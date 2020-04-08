current_dir:=$(shell pwd)
date:=$(shell date +%Y%m%d%H%M%S)
version:=$(shell bash  ./build/version.sh)
tencent_cloud_docker_image_registry=ccr.ccs.tencentyun.com/webankpartners

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
	docker run --rm --name $(build_name)_node1  -v $(current_dir)/wecube-portal:/home/node/app -w /home/node/app node:12.13.1 npm rebuild node-sass
	docker run --rm --name $(build_name)_node2  -v $(current_dir)/wecube-portal:/home/node/app -w /home/node/app node:12.13.1 npm run build

image:
	docker build -t platform-core:$(version) -f build/platform-core/Dockerfile .
	docker build -t platform-gateway:$(version) -f build/platform-gateway/Dockerfile .
	docker build -t wecube-portal:$(version) -f build/wecube-portal/Dockerfile .
	docker build -t platform-auth-server:$(version) -f build/platform-auth-server/Dockerfile .
	sh build/db/build-image.sh $(version)

push:
	docker tag  platform-core:$(version) $(tencent_cloud_docker_image_registry)/platform-core:$(date)-$(version)
	docker push $(tencent_cloud_docker_image_registry)/platform-core:$(date)-$(version)

	docker tag  platform-gateway:$(version) $(tencent_cloud_docker_image_registry)/platform-gateway:$(date)-$(version)
	docker push $(tencent_cloud_docker_image_registry)/platform-gateway:$(date)-$(version)

	docker tag  wecube-portal:$(version) $(tencent_cloud_docker_image_registry)/wecube-portal:$(date)-$(version)
	docker push $(tencent_cloud_docker_image_registry)/wecube-portal:$(date)-$(version)

	docker tag  platform-auth-server:$(version) $(tencent_cloud_docker_image_registry)/platform-auth-server:$(date)-$(version)
	docker push $(tencent_cloud_docker_image_registry)/platform-auth-server:$(date)-$(version)

	docker tag  wecube-db:$(version) ${tencent_cloud_docker_image_registry}/wecube-db:${date}-$(version)
	docker push ${tencent_cloud_docker_image_registry}/wecube-db:${date}-$(version)

tencent_cloud_release_version=tencent-cloud-release-version
releaseToTencentCloud:
	docker tag  platform-core:$(version) $(tencent_cloud_docker_image_registry)/platform-core:$(tencent_cloud_release_version)
	docker push $(tencent_cloud_docker_image_registry)/platform-core:$(tencent_cloud_release_version)

	docker tag  platform-gateway:$(version) $(tencent_cloud_docker_image_registry)/platform-gateway:$(tencent_cloud_release_version)
	docker push $(tencent_cloud_docker_image_registry)/platform-gateway:$(tencent_cloud_release_version)

	docker tag  wecube-portal:$(version) $(tencent_cloud_docker_image_registry)/wecube-portal:$(tencent_cloud_release_version)
	docker push $(tencent_cloud_docker_image_registry)/wecube-portal:$(tencent_cloud_release_version)

	docker tag  platform-auth-server:$(version) $(tencent_cloud_docker_image_registry)/platform-auth-server:$(tencent_cloud_release_version)
	docker push $(tencent_cloud_docker_image_registry)/platform-auth-server:$(tencent_cloud_release_version)
    
	docker tag  wecube-db:$(version) $(tencent_cloud_docker_image_registry)/wecube-db:$(tencent_cloud_release_version)
	docker push $(tencent_cloud_docker_image_registry)/wecube-db:$(tencent_cloud_release_version)
    
release_version=release-version
release:
	docker tag  platform-core:$(version) webankpartners/platform-core:$(release_version)
	docker push webankpartners/platform-core:$(release_version)

	docker tag  platform-gateway:$(version) webankpartners/platform-gateway:$(release_version)
	docker push webankpartners/platform-gateway:$(release_version)

	docker tag  wecube-portal:$(version) webankpartners/wecube-portal:$(release_version)
	docker push webankpartners/wecube-portal:$(release_version)

	docker tag  platform-auth-server:$(version) webankpartners/platform-auth-server:$(release_version)
	docker push webankpartners/platform-auth-server:$(release_version)
    
	docker tag  wecube-db:$(version) webankpartners/wecube-db:$(release_version)
	docker push webankpartners/wecube-db:$(release_version)

huawei_cloud_release_version=huawei-cloud-release-version
releaseToHuaweiCloud:
	docker tag  platform-core:$(version) swr.ap-southeast-3.myhuaweicloud.com/webankpartners/platform-core:$(huawei_cloud_release_version)
	docker push swr.ap-southeast-3.myhuaweicloud.com/webankpartners/platform-core:$(huawei_cloud_release_version)

	docker tag  platform-gateway:$(version) swr.ap-southeast-3.myhuaweicloud.com/webankpartners/platform-gateway:$(huawei_cloud_release_version)
	docker push swr.ap-southeast-3.myhuaweicloud.com/webankpartners/platform-gateway:$(huawei_cloud_release_version)

	docker tag  wecube-portal:$(version) swr.ap-southeast-3.myhuaweicloud.com/webankpartners/wecube-portal:$(huawei_cloud_release_version)
	docker push swr.ap-southeast-3.myhuaweicloud.com/webankpartners/wecube-portal:$(huawei_cloud_release_version)

	docker tag  platform-auth-server:$(version) swr.ap-southeast-3.myhuaweicloud.com/webankpartners/platform-auth-server:$(huawei_cloud_release_version)
	docker push swr.ap-southeast-3.myhuaweicloud.com/webankpartners/platform-auth-server:$(huawei_cloud_release_version)
    
	docker tag  wecube-db:$(version) swr.ap-southeast-3.myhuaweicloud.com/webankpartners/wecube-db:$(huawei_cloud_release_version)
	docker push swr.ap-southeast-3.myhuaweicloud.com/webankpartners/wecube-db:$(huawei_cloud_release_version)

env_config=smoke_branch.cfg
target_host="tcp://10.0.0.1:2375"
deploy:
	docker tag  platform-core:$(version) $(tencent_cloud_docker_image_registry)/platform-core:$(date)-$(version)
	docker push $(tencent_cloud_docker_image_registry)/platform-core:$(date)-$(version)

	docker tag  platform-gateway:$(version) $(tencent_cloud_docker_image_registry)/platform-gateway:$(date)-$(version)
	docker push $(tencent_cloud_docker_image_registry)/platform-gateway:$(date)-$(version)

	docker tag  wecube-portal:$(version) $(tencent_cloud_docker_image_registry)/wecube-portal:$(date)-$(version)
	docker push $(tencent_cloud_docker_image_registry)/wecube-portal:$(date)-$(version)
	
	docker tag  platform-auth-server:$(version) $(tencent_cloud_docker_image_registry)/platform-auth-server:$(date)-$(version)
	docker push $(tencent_cloud_docker_image_registry)/platform-auth-server:$(date)-$(version)
	
	sh build/deploy_generate_compose.sh $(env_config) $(date)-$(version)
	docker-compose -f docker-compose.yml -H $(target_host) up -d

plugin_host="tcp://10.0.0.2:2375"
deploy_demo:
	docker tag  platform-core:$(version) $(tencent_cloud_docker_image_registry)/platform-core:$(date)-$(version)
	docker push $(tencent_cloud_docker_image_registry)/platform-core:$(date)-$(version)

	docker tag  platform-gateway:$(version) $(tencent_cloud_docker_image_registry)/platform-gateway:$(date)-$(version)
	docker push $(tencent_cloud_docker_image_registry)/platform-gateway:$(date)-$(version)

	docker tag  wecube-portal:$(version) $(tencent_cloud_docker_image_registry)/wecube-portal:$(date)-$(version)
	docker push $(tencent_cloud_docker_image_registry)/wecube-portal:$(date)-$(version)

	docker tag  platform-auth-server:$(version) $(tencent_cloud_docker_image_registry)/platform-auth-server:$(date)-$(version)
	docker push $(tencent_cloud_docker_image_registry)/platform-auth-server:$(date)-$(version)

	docker tag  wecube-db:$(version) ${tencent_cloud_docker_image_registry}/wecube-db:${date}-$(version)
	docker push ${tencent_cloud_docker_image_registry}/wecube-db:${date}-$(version)

	docker-compose -f build/plugin_db.yml -H $(plugin_host) up -d
	sed "s~{{WECUBE_DB_IMAGE_NAME}}~wecube-db:${date}-$(version)~g" build/wecube_core_mysql.tpl > wecube_core_mysql.yml
	docker-compose -f wecube_core_mysql.yml -H $(target_host) up -d
	sleep 90
	sh build/deploy_generate_compose.sh $(env_config) $(date)-$(version)
	sed -i "s~{{WECUBE_DB_IMAGE_NAME}}~wecube-db:${date}-$(version)~g" docker-compose.yml
	sed -i "s~{{WECUBE_APP_IMAGE_VER}}~wecube-db:${date}-$(version)~g" docker-compose.yml
	docker-compose -f docker-compose.yml -H $(target_host) up -d
