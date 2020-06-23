CREATE TABLE `plugin_packages111` (
    `id`                    VARCHAR(255) PRIMARY KEY,
    `name`                  VARCHAR(63) NOT NULL,
    `version`               VARCHAR(20) NOT NULL,
    `status`                VARCHAR(20) NOT NULL default 'UNREGISTERED',
    `upload_timestamp`      timestamp default current_timestamp,
    `ui_package_included`   BIT default 0,
    UNIQUE INDEX `name` (`name`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ;

create table plugin_package_dependencies111 (
  id VARCHAR(255) PRIMARY KEY,
  plugin_package_id VARCHAR(255) not null,
  dependency_package_name VARCHAR(63) not null,
  dependency_package_version varchar(20) not null
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 ;