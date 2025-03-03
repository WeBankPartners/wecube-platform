package service

import (
	"errors"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/db"
	"go.uber.org/zap"
)

var SubSystemInfoDataServiceImplInstance SubSystemInfoDataServiceImpl

type SubSystemInfoDataServiceImpl struct {
}

func (SubSystemInfoDataServiceImpl) retrieveSysSubSystemInfoWithSystemCode(systemCode string) (*model.SysSubSystemInfo, error) {
	if len(systemCode) == 0 {
		log.Debug(nil, log.LOGGER_APP, "system code is blank.")
		return nil, errors.New("system code cannot be blank")
	}

	subSystem, err := db.SubSystemRepositoryInstance.FindOneBySystemCode(systemCode)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to find by subsystem by systemcode", zap.String("systemcode", systemCode), zap.Error(err))
		return nil, err
	}

	if subSystem == nil {
		log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("cannot find sub system with system code:%v", systemCode))
		return nil, nil
	}

	grantedAuthorities := []string{constant.AuthoritySubsystem, "ADMIN_SYSTEM_PARAMS"}

	subSystemAuthorities, err := db.SubSystemAuthorityRsRepositoryInstance.FindAllBySubSystemId(subSystem.Id)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to find all SubSystemAuthorityRsEntity", zap.String("subSystem id", subSystem.Id),
			zap.Error(err))
		return nil, err
	}

	for _, subSystemAuthority := range subSystemAuthorities {
		if !subSystemAuthority.Active || subSystemAuthority.Deleted {
			continue
		}

		authority := &model.SysAuthorityEntity{}
		existed, err := db.Engine.ID(subSystemAuthority.AuthorityID).Get(authority)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to get authority", zap.String("authorityId", subSystemAuthority.AuthorityID),
				zap.Error(err))
			return nil, err
		}

		if !existed {
			continue
		}
		if !authority.Active || authority.Deleted {
			continue
		}

		//grantedAuthorities.add(new SimpleGrantedAuthority(authority.getCode()));
		grantedAuthorities = append(grantedAuthorities, authority.Code)
	}

	returnSystemInfo := buildSysSubSystemInfo(subSystem)

	returnSystemInfo.Authorities = grantedAuthorities

	return returnSystemInfo, nil
}

func buildSysSubSystemInfo(entity *model.SysSubSystemEntity) *model.SysSubSystemInfo {
	return &model.SysSubSystemInfo{
		ID:         entity.Id,
		Name:       entity.Name,
		PubAPIKey:  entity.PubApiKey,
		SystemCode: entity.SystemCode,
		Active:     entity.IsActive,
		Blocked:    entity.IsBlocked,
		APIKey:     entity.ApiKey,
	}
}
