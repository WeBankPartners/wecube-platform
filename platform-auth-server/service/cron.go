package service

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/db"
	"time"
)

func StartCornJob() {
	go startNotifyCronJob()
	select {}
}

func startNotifyCronJob() {
	t := time.NewTicker(30 * time.Minute).C
	for {
		<-t
		go notifyAction()
	}
}

func notifyAction() {
	var err error
	log.Logger.Info("Start notify action")
	var allUserList []*model.SysUserEntity
	var allUserEmailMap = make(map[string]string)
	var userRoleEntityList []*model.UserRoleRsEntity
	var allRoleList []*model.SysRoleEntity
	// 角色显示名 map
	var allRoleDisplayNameMap = make(map[string]string)
	if err = db.Engine.SQL("select * from auth_sys_user where is_deleted = 0").Find(&allUserList); err != nil {
		return
	}
	if len(allUserList) > 0 {
		for _, user := range allUserList {
			allUserEmailMap[user.Username] = user.EmailAddr
		}
	}
	if err = db.Engine.SQL("select * from auth_sys_role where is_deleted = 0").Find(&allRoleList); err != nil {
		return
	}
	if len(allRoleList) > 0 {
		for _, role := range allRoleList {
			allRoleDisplayNameMap[role.Name] = role.DisplayName
		}
	}
	if err = db.Engine.SQL("select  * from auth_sys_user_role where  expire_time is not null  and is_deleted = 0").Find(&userRoleEntityList); err != nil {
		log.Logger.Error("notify action fail,query auth_sys_user_role error", log.Error(err))
		return
	}
	if len(userRoleEntityList) == 0 {
		return
	}
	log.Logger.Info("mail config", log.String("mailServer", model.Config.Mail.AuthServer), log.String("senderMail", model.Config.Mail.SenderMail))
	for _, entity := range userRoleEntityList {
		// 计算过期百分比
		entityPercent := calcExpireObj(entity)
		if entityPercent >= 100 {
			go NotifyRoleExpireMail(allRoleDisplayNameMap[entity.RoleName], allUserEmailMap[entity.Username], entity.ExpireTime.Format(constant.DateTimeFormat))
			// 删除授权角色
			if _, err = db.Engine.Exec("update auth_sys_user_role set is_deleted = 1,updated_time = ?,role_apply = null where id = ?", time.Now().Format(constant.DateTimeFormat), entity.Id); err != nil {
				log.Logger.Error("update auth_sys_user_role error", log.Error(err))
				return
			}
		} else if entityPercent >= model.Config.NotifyPercent && entity.NotifyCount == 0 {
			// 发送邮件
			go NotifyRolePreExpireMail(allRoleDisplayNameMap[entity.RoleName], allUserEmailMap[entity.Username], entity.ExpireTime.Format(constant.DateTimeFormat))
			// 更新通知次数
			if _, err = db.Engine.Exec("update auth_sys_user_role set notify_count = 1,updated_time = ? where id = ?", time.Now().Format(constant.DateTimeFormat), entity.Id); err != nil {
				log.Logger.Error("update auth_sys_user_role error", log.Error(err))
				return
			}
		}
	}
}

func calcExpireObj(entity *model.UserRoleRsEntity) float64 {
	if entity.CreatedTime.Unix() > 0 {
		max := entity.ExpireTime.Sub(entity.CreatedTime).Seconds()
		use := time.Now().Sub(entity.CreatedTime).Seconds()
		return (use / max) * 100
	} else {
		if entity.ExpireTime.After(time.Now()) {
			return 0
		}
		return 100
	}
}

func NotifyRoleExpireMail(role, email, expireTime string) (err error) {
	var subject, content string
	if model.Config.Mail.AuthServer != "" && model.Config.Mail.SenderMail != "" {
		subject = "[wecube] [Permission expiration reminder]  【权限过期提醒】"
		content = fmt.Sprintf("Your role [%s] permission will expire on %s. Please go to the page - Account in the upper right corner - Role application - Effective - Expired role, click the button", role, expireTime)
		content = content + fmt.Sprintf("\n\n\n您的角色[%s]权限将在%s过期,请进入页面-右上角账户-角色申请-已过期-将过期角色,点击按钮申请续期,否则将影响您的正常使用", role, expireTime)
		err = model.MailSender.Send(subject, content, []string{email})
		if err != nil {
			log.Logger.Error("send mail err", log.Error(err))
		}
	}
	return
}

func NotifyRolePreExpireMail(role, email, expireTime string) (err error) {
	var subject, content string
	if model.Config.Mail.AuthServer != "" && model.Config.Mail.SenderMail != "" {
		subject = "[wecube] [Permission expiration reminder]  【权限过期提醒】"
		content = fmt.Sprintf("Your role [%s] permission will expire on %s. Please go to the page - Account in the upper right corner - Role application - Expired - Expired role, click the button to apply for renewal, otherwise it will affect your normal use", role, expireTime)
		content = content + fmt.Sprintf("\n\n\n您的角色[%s]权限将在%s过期,请进入页面-右上角账户-角色申请-已过期-将过期角色,点击按钮申请续期,否则将影响您的正常使用", role, expireTime)
		err = model.MailSender.Send(subject, content, []string{email})
		if err != nil {
			log.Logger.Error("send mail err", log.Error(err))
		}
	}
	return
}
