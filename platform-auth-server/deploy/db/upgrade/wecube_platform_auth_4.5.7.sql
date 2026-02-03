SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE `auth_sys_user`
    ADD COLUMN `mfa_secret` VARCHAR(255) NULL DEFAULT NULL COMMENT 'MFA密钥,用于生成TOTP二维码' AFTER `auth_src`,
    ADD COLUMN `mfa_bound` BIT(1) NULL DEFAULT b'0' COMMENT 'MFA绑定状态,0-未绑定,1-已绑定' AFTER `mfa_secret`;
SET FOREIGN_KEY_CHECKS = 1;