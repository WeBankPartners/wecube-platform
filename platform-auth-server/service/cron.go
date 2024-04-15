package service

import (
	"fmt"
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
	t := time.NewTicker(time.Hour).C
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
	for _, entity := range userRoleEntityList {
		// 计算过期百分比
		entityPercent := calcExpireObj(entity)
		if entityPercent >= model.Config.NotifyPercent && entity.NotifyCount == 0 {
			// 发送邮件
			go NotifyRoleExpireMail(allRoleDisplayNameMap[entity.RoleName], allUserEmailMap[entity.Username])
		} else if entityPercent >= 100 {
			// 删除授权角色
			if _, err = db.Engine.Exec("update auth_sys_user_role set is_deleted = 1 where id = ?", entity.Id); err != nil {
				log.Logger.Error("update auth_sys_user_role error", log.Error(err))
				return
			}
		}
	}
}

func calcExpireObj(entity *model.UserRoleRsEntity) float64 {
	max := entity.ExpireTime.Sub(entity.CreatedTime).Seconds()
	use := time.Now().Sub(entity.CreatedTime).Seconds()
	return (use / max) * 100
}

func NotifyRoleExpireMail(role, email string) (err error) {
	var subject, content string
	if model.Config.Mail.AuthServer != "" && model.Config.Mail.SenderMail != "" {
		subject = "[wecube] [Request return reminder]  【请求被退回提醒】"
		content = fmt.Sprintf("The [request: %s] you initiated was returned to. Please make the necessary modifications and resubmit. Click the link to view details", role)
		content = content + fmt.Sprintf("\n\n\n您发起的[请求:%s],在%s节点被%s退回到草稿,请修改之后重新提交,点击链接查看详情", role)
		err = model.MailSender.Send(subject, content, []string{email})
		if err != nil {
			log.Logger.Error("send mail err", log.Error(err))
		}
	}
	return
}
