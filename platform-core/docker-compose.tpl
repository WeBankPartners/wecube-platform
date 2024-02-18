version: '3'
services:
  platform-core:
    image: platform-core:{{version}}
    container_name: platform-core-{{version}}
    restart: always
    volumes:
      - /etc/localtime:/etc/localtime
      - [#path]/platform-core/logs:/app/platform-core/logs
      - [#path]/platform-core/certs:/app/platform-core/config/certs
    ports:
      - "[@HOSTIP]:[#http_port]:8000"
    environment:
      - version={{version}}
      - log_level=[#log_level]
      - password_private_key_path=/app/platform-core/config/certs/[#wecube_private_key]
      - https_enable=[#https_enable]
      - http_port=8000
      - db_server=[#db_server]
      - db_port=[#db_port]
      - db_user=[#db_user]
      - db_pass=[#db_pass]
      - db_database=[#db_database]
      - auth_server_url=[#auth_server_url]
      - access_token_expired_sec=[#access_token_expired_sec]
      - refresh_token_expired_sec=[#refresh_token_expired_sec]
      - s3_address=[#s3_address]
      - s3_access_key=[#s3_access_key]
      - s3_secret_key=[#s3_secret_key]
      - static_resource_server_ips=[#static_resource_server_ips]
      - static_resource_server_user=[#static_resource_server_user]
      - static_resource_server_password=[#static_resource_server_password]
      - static_resource_server_port=[#static_resource_server_port]
      - static_resource_server_path=[#static_resource_server_path]
      - plugin_base_mount_path=[#plugin_base_mount_path]
      - plugin_deploy_path=[#plugin_deploy_path]
      - plugin_password_pub_key_path=[#plugin_password_pub_key_path]
      - resource_server_password_seed=[#resource_server_password_seed]
      - gateway_url=[#gateway_url]
      - gateway_host_ports=[#gateway_host_ports]
      - sub_system_private_key=[#sub_system_private_key]
      - cron_keep_batch_exec_days=[#cron_keep_batch_exec_days]
