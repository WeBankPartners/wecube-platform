version: "3"
services:
  wecube-db:
    image: ccr.ccs.tencentyun.com/webankpartners/mysql:${WECUBE_RELEASE_VERSION}
    restart: always
    command:
      [
        "--character-set-server=utf8mb4",
        "--collation-server=utf8mb4_unicode_ci",
        "--default-time-zone=+8:00",
        "--max_allowed_packet=16M",
        "--lower_case_table_names=1",
        "--max-connections=1024",
        "--innodb_log_file_size=1G",
        "--transaction-isolation=READ-COMMITTED",
      ]
    volumes:
      - ${MYSQL_DATA_PATH}:/var/lib/mysql
      - /etc/localtime:/etc/localtime
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_PASSWORD}
    ports:
      - ${MYSQL_PORT}:3306