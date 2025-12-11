ALTER TABLE `auth_sys_user` ADD COLUMN `mfa_secret` VARCHAR(255) NULL DEFAULT NULL COMMENT 'MFA密钥，用于生成TOTP二维码' AFTER `auth_src`;

