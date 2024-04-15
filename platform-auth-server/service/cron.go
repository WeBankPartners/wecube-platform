package service

import (
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/db"
	"time"
)

const percent = 75

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
	var userRoleEntityList []*model.UserRoleRsEntity
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
		if entityPercent >= percent && entity.NotifyCount == 0 {
			// 发送邮件

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
