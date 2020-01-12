/*Table structure for table `auth_sys_api` */

CREATE TABLE `auth_sys_api` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `api_url` varchar(255) DEFAULT NULL,
  `http_method` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `system_code` varchar(255) DEFAULT NULL,
  `system_id` bigint(20) DEFAULT NULL,
  `system_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `auth_sys_api` */

/*Table structure for table `auth_sys_api_authority` */

CREATE TABLE `auth_sys_api_authority` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `api_id` bigint(20) DEFAULT NULL,
  `authority_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2vabi4abv5dxfp2mqa0y95f0e` (`api_id`),
  KEY `FK2qengicw6afc3j7o4905o7pcq` (`authority_id`),
  CONSTRAINT `FK2qengicw6afc3j7o4905o7pcq` FOREIGN KEY (`authority_id`) REFERENCES `auth_sys_authority` (`id`),
  CONSTRAINT `FK2vabi4abv5dxfp2mqa0y95f0e` FOREIGN KEY (`api_id`) REFERENCES `auth_sys_api` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `auth_sys_api_authority` */

/*Table structure for table `auth_sys_api_role` */

CREATE TABLE `auth_sys_api_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `api_id` bigint(20) DEFAULT NULL,
  `role_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7xbun9tvnjnhksbscavqw7ryr` (`api_id`),
  KEY `FKat4svvvfbq5ik4u3qp76pmn4p` (`role_id`),
  CONSTRAINT `FK7xbun9tvnjnhksbscavqw7ryr` FOREIGN KEY (`api_id`) REFERENCES `auth_sys_api` (`id`),
  CONSTRAINT `FKat4svvvfbq5ik4u3qp76pmn4p` FOREIGN KEY (`role_id`) REFERENCES `auth_sys_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `auth_sys_api_role` */

/*Table structure for table `auth_sys_authority` */

CREATE TABLE `auth_sys_authority` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `system_code` varchar(255) DEFAULT NULL,
  `system_id` bigint(20) DEFAULT NULL,
  `system_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `auth_sys_authority` */

/*Table structure for table `auth_sys_authority_role` */

CREATE TABLE `auth_sys_authority_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `authority_id` bigint(20) DEFAULT NULL,
  `role_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrsua9aq30l5o3gcvl8rv9sxrm` (`authority_id`),
  KEY `FK4aso5c73vodgmqmoo44tbwa1t` (`role_id`),
  CONSTRAINT `FK4aso5c73vodgmqmoo44tbwa1t` FOREIGN KEY (`role_id`) REFERENCES `auth_sys_role` (`id`),
  CONSTRAINT `FKrsua9aq30l5o3gcvl8rv9sxrm` FOREIGN KEY (`authority_id`) REFERENCES `auth_sys_authority` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `auth_sys_authority_role` */

/*Table structure for table `auth_sys_menu` */

CREATE TABLE `auth_sys_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_blocked` bit(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `system_code` varchar(255) DEFAULT NULL,
  `systemid` bigint(20) DEFAULT NULL,
  `system_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `auth_sys_menu` */

/*Table structure for table `auth_sys_role` */

CREATE TABLE `auth_sys_role` (
  `id` varchar(255) NOT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `auth_sys_sub_system` */

CREATE TABLE `auth_sys_sub_system` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `api_key` varchar(500) DEFAULT NULL,
  `is_blocked` bit(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pub_api_key` varchar(500) DEFAULT NULL,
  `system_code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;


/*Table structure for table `auth_sys_user` */

CREATE TABLE `auth_sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_blocked` bit(1) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

/*Table structure for table `auth_sys_user_role` */

CREATE TABLE `auth_sys_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `role_id` varchar(255) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpll9es009p59gcxh1e68wp6jc` (`user_id`),
  KEY `FKg3oaag8htwje9luu0986eoju1` (`role_id`),
  CONSTRAINT `FKg3oaag8htwje9luu0986eoju1` FOREIGN KEY (`role_id`) REFERENCES `auth_sys_role` (`id`),
  CONSTRAINT `FKpll9es009p59gcxh1e68wp6jc` FOREIGN KEY (`user_id`) REFERENCES `auth_sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

