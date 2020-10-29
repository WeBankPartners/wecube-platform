version: '2'
services:
  wecube-core-mysql:
    image: ccr.ccs.tencentyun.com/webankpartners/{{WECUBE_DB_IMAGE_NAME}}
    restart: always
    command: [
            '--character-set-server=utf8mb4',
            '--collation-server=utf8mb4_unicode_ci',
            '--default-time-zone=+8:00',
            '--max_allowed_packet=4M',
            '--lower_case_table_names=1'
    ]
    volumes:
      - /etc/localtime:/etc/localtime
    environment:
      - MYSQL_ROOT_PASSWORD=Abcd1234
    ports:
      - 3306:3306
    volumes:
      - /data/wecube-core/db:/var/lib/mysql