SET FOREIGN_KEY_CHECKS = 0;

alter table auth_sys_api  convert to character set utf8 collate utf8_bin;
alter table auth_sys_authority convert to character set utf8 collate utf8_bin;
alter table auth_sys_role convert to character set utf8 collate utf8_bin;
alter table auth_sys_role_authority convert to character set utf8 collate utf8_bin;
alter table auth_sys_sub_system convert to character set utf8 collate utf8_bin;
alter table auth_sys_sub_system_authority convert to character set utf8 collate utf8_bin;
alter table auth_sys_user convert to character set utf8 collate utf8_bin;
alter table auth_sys_user_role convert to character set utf8 collate utf8_bin;

delete from auth_sys_sub_system where id = '63d5ca8787e446658ae337a19176595b';
INSERT INTO `auth_sys_sub_system` (`id`, `created_by`, `created_time`, `is_active`, `api_key`, `is_blocked`, `name`, `pub_api_key`, `system_code`) VALUES ('63d5ca8787e446658ae337a19176595b', 'system', '2020-05-22 11:55:51', 1, 'MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAoE6V5GC88T/DECv6hcM8tBbUf56FqZRpuS4wiKh+S+vKSJA1zF0FPOUoMqO/R7EcWlYzG9+gJNoHbuY+qy2aGwIDAQABAkA0SCz+pMY6cC8wEQfNpFUh7ugzDwJMwkiQyo2tpDY8kA7S23u6N5hAChfUTXU9KLOehu5b/v61HPqKHWcvpSFRAiEA5pzO6lHOzoWPCkxgfjCNNwp27GiPYF/Ck4lZTBNMUU8CIQCx9GemlKZgyMuEwTCfjxSOP0oHsH6dw/IQbNhpg5Y/dQIgBvFPlWRTGoqQzNmRyhrCFZceD23yaw9W90QaSR0HnicCIFvKARy/WAJcD/3t2DK1DplAs+K7etPNmjI2snnaUclxAiEAtdIk6HwbUtrfeCvASH+kSAmdIlRgWnaLikSD/mmPflM=', 0, 'gateway', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKBOleRgvPE/wxAr+oXDPLQW1H+ehamUabkuMIiofkvrykiQNcxdBTzlKDKjv0exHFpWMxvfoCTaB27mPqstmhsCAwEAAQ==', 'SYS_GATEWAY');

SET FOREIGN_KEY_CHECKS = 1;