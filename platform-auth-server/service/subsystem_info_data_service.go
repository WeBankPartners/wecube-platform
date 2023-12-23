package service

import (
	"errors"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/db"
)

var SubSystemInfoDataServiceImplInstance SubSystemInfoDataServiceImpl

type SubSystemInfoDataServiceImpl struct {
}

func (SubSystemInfoDataServiceImpl) retrieveSysSubSystemInfoWithSystemCode(systemCode string) (*model.SysSubSystemInfo, error) {
	if len(systemCode) == 0 {
		log.Logger.Debug("system code is blank.")
		return nil, errors.New("system code cannot be blank.")
	}

	subSystem, err := db.SubSystemRepositoryInstance.FindOneBySystemCode(systemCode)
	if err != nil {
		return nil, err
	}

	if subSystem == nil {
		log.Logger.Debug(fmt.Sprintf("cannot find sub system with system code:%v", systemCode))
		return nil, nil
	}

	//List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
	//grantedAuthorities.add(new SimpleGrantedAuthority(ApplicationConstants.Authority.SUBSYSTEM));
	grantedAuthorities := []string{constant.AuthoritySubsystem}

	subSystemAuthorities, err := db.SubSystemAuthorityRsRepositoryInstance.FindAllBySubSystemId(subSystem.Id)
	if err != nil {
		return nil, err
	}

	if subSystemAuthorities != nil {
		for _, subSystemAuthority := range subSystemAuthorities {
			if !subSystemAuthority.Active || subSystemAuthority.Deleted {
				continue
			}

			var authority *model.SysAuthorityEntity
			existed, err := db.Engine.ID(subSystemAuthority.AuthorityID).Get(&authority)
			if err != nil {
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
