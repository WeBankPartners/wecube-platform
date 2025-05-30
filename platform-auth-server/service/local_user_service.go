package service

import (
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/db"
	"go.uber.org/zap"
)

const (
	AuthorityTypeRole       = "ROLE"
	AuthorityTypePermission = "AUTHORITY"
)

type LocalUserService struct {
}

var LocalUserServiceInstance LocalUserService

func (LocalUserService) loadUserByUsername(username string) (*model.SysUser, error) {
	userEntity, err := db.UserRepositoryInstance.FindNotDeletedUserByUsername(username)
	if err != nil {
		log.Warn(nil, log.LOGGER_APP, "failed to FindNotDeletedUserByUsername", zap.String("username", username), zap.Error(err))
		return nil, err
	}

	if userEntity == nil {
		return nil, nil
	}

	if !userEntity.IsActive || userEntity.IsDeleted {
		return nil, nil
	}

	if userEntity.IsBlocked {
		return nil, nil
	}

	user := &model.SysUser{
		Username:    userEntity.Username,
		Password:    userEntity.Password,
		AuthSource:  userEntity.AuthSource,
		AuthContext: userEntity.AuthContext,
	}

	userRoles, err := db.UserRoleRsRepositoryInstance.FindAllByUserId(userEntity.Id)
	if err != nil {
		log.Warn(nil, log.LOGGER_APP, "failed to FindAllByUserId for userRoleRs", zap.String("userId", userEntity.Id), zap.Error(err))
		return nil, err
	}
	if userRoles == nil {
		return user, nil
	}

	for _, userRole := range userRoles {
		if !userRole.Active || userRole.Deleted {
			continue
		}

		role := &model.SysRoleEntity{}
		found, err := db.Engine.ID(userRole.RoleId).Get(role)
		if err != nil {
			log.Warn(nil, log.LOGGER_APP, "failed to get role", zap.String("roleId", userRole.RoleId), zap.Error(err))
			return nil, err
		}

		//if role == nil {
		if !found {
			continue
		}

		if !role.Active || role.Deleted {
			continue
		}

		roleObject := &model.CompositeAuthority{
			Authority:     role.Name,
			AuthorityType: AuthorityTypeRole,
		}
		user.CompositeAuthorities = append(user.CompositeAuthorities, roleObject)

		if err := appendAuthorities(user, role); err != nil {
			return nil, err
		}

	}
	return user, nil
}

func appendAuthorities(user *model.SysUser, role *model.SysRoleEntity) error {
	roleAuthorities, err := db.RoleAuthorityRsRepositoryInstance.FindAllConfiguredAuthoritiesByRoleId(role.Id)
	if err != nil {
		log.Warn(nil, log.LOGGER_APP, "failed to FindAllConfiguredAuthoritiesByRoleId", zap.String("roleId", role.Id), zap.Error(err))
		return err
	}
	if len(roleAuthorities) == 0 {
		return nil
	}

	for _, roleAuthority := range roleAuthorities {
		if !roleAuthority.Active || roleAuthority.Deleted {
			continue
		}

		authority := &model.SysAuthorityEntity{}
		found, err := db.Engine.ID(roleAuthority.AuthorityID).Get(authority)
		if err != nil {
			log.Warn(nil, log.LOGGER_APP, "failed to get authority", zap.String("authorityId", roleAuthority.AuthorityID), zap.Error(err))
			return err
		}

		//if authority == nil {
		if !found {
			continue
		}

		if !authority.Active || authority.Deleted {
			continue
		}

		authorityObject := &model.CompositeAuthority{
			Authority:     authority.Code,
			AuthorityType: AuthorityTypePermission,
		}
		user.CompositeAuthorities = append(user.CompositeAuthorities, authorityObject)

	}
	return nil
}
