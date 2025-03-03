package service

import (
	"errors"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/utils"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/db"
	"go.uber.org/zap"
	"strings"
	"time"
)

const (
	StatusDeleted   = "Deleted"
	StatusNoDeleted = "NotDeleted"
)

var RoleManagementServiceInstance RoleManagementService

type RoleManagementService struct {
}

func (RoleManagementService) RetrieveLocalRoleByRoleName(roleName string) (*model.SimpleLocalRoleDto, error) {
	if len(roleName) == 0 {
		return nil, exterror.Catch(exterror.New().AuthServer3002Error, nil)
	}

	existedRole, err := db.RoleRepositoryInstance.FindNotDeletedRoleByName(roleName)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to find not deleted role by name", zap.String("name", roleName), zap.Error(err))
		return nil, err
	}

	if existedRole == nil {
		return nil, exterror.Catch(exterror.New().AuthServer3003Error.WithParam(roleName), nil)
	}
	return convertToSimpleLocalRoleDto(existedRole), nil
}

func convertToSimpleLocalRoleDto(role *model.SysRoleEntity) *model.SimpleLocalRoleDto {
	status := ""
	if role.Deleted {
		status = StatusDeleted
	} else {
		status = StatusNoDeleted
	}

	return &model.SimpleLocalRoleDto{
		ID:            role.Id,
		Name:          role.Name,
		DisplayName:   role.DisplayName,
		Email:         role.EmailAddress,
		Status:        status,
		Administrator: role.Administrator,
	}
}

func (RoleManagementService) UpdateLocalRole(roleDto *model.SimpleLocalRoleDto, curUser string) (*model.SimpleLocalRoleDto, error) {
	role := &model.SysRoleEntity{}
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	existed, err := session.ID(roleDto.ID).Get(role)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, fmt.Sprintf("failed to get role:%s", roleDto.ID), zap.Error(err))
		return nil, err
	}
	if !existed {
		log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("can not find role:%s", roleDto.ID))
		return nil, exterror.Catch(exterror.New().AuthServer3006Error.WithParam(roleDto.ID), nil)
	}

	if len(roleDto.DisplayName) > 0 {
		role.DisplayName = roleDto.DisplayName
	}

	if len(roleDto.Email) > 0 {
		role.EmailAddress = roleDto.Email
	}

	inputStatus := roleDto.Status
	if validateRoleStatus(inputStatus) {
		inputRoleDeletedStatus, err := convertRoleStatus(inputStatus)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to convertRoleStatus", zap.Error(err))
			session.Rollback()
			return nil, err
		}
		existRoleDeletedStatus := role.Deleted
		if inputRoleDeletedStatus != existRoleDeletedStatus {

			if !existRoleDeletedStatus {
				// NotDeleted -> Deleted
				role.Deleted = true
				role.Active = false

				userRoles, err := db.UserRoleRsRepositoryInstance.FindAllByRoleId(role.Id)
				if err != nil {
					log.Error(nil, log.LOGGER_APP, "failed to faill all by roleId", zap.String("role id", role.Id), zap.Error(err))
					session.Rollback()
					return nil, err
				}

				if len(userRoles) == 0 {
					userRoles = make([]*model.UserRoleRsEntity, 0)
				}

				for _, userRole := range userRoles {
					userRole.Active = false
					userRole.UpdatedBy = curUser
					userRole.Deleted = true
					userRole.UpdatedTime = time.Now()
					affected, err := session.ID(userRole.Id).UseBool().Update(userRole)
					if err != nil || affected == 0 {
						session.Rollback()
						if err != nil {
							log.Error(nil, log.LOGGER_APP, "failed to update user role", log.JsonObj("userRole", userRole), zap.Error(err))
						}
						return nil, fmt.Errorf("failed to UpdateLocalRole: %v(%v)", roleDto.ID, roleDto.Name)
					}
				}
			} else {
				// Deleted -> NotDeleted
				role.Deleted = false
				role.Active = true
			}
		}
	}
	role.Administrator = roleDto.Administrator
	role.UpdatedBy = curUser
	role.UpdatedTime = time.Now()
	_, err = session.ID(role.Id).UseBool().Update(role)
	if err != nil {
		session.Rollback()
		log.Error(nil, log.LOGGER_APP, "failed to update role", log.JsonObj("role", role), zap.Error(err))
		return nil, fmt.Errorf("failed to UpdateLocalRole: %v(%v)", roleDto.ID, roleDto.Name)
	}

	session.Commit()
	return convertToSimpleLocalRoleDto(role), nil
}

func validateRoleStatus(status string) bool {
	if len(status) == 0 {
		return false
	}
	if strings.EqualFold(StatusDeleted, status) || strings.EqualFold(StatusNoDeleted, status) {
		return true
	}

	return false
}

func convertRoleStatus(status string) (bool, error) {
	if utils.EqualsIgnoreCase(StatusDeleted, status) {
		return true, nil
	}

	if utils.EqualsIgnoreCase(StatusNoDeleted, status) {
		return false, nil
	}

	return false, fmt.Errorf("Unsupported role status:" + status)
}

func validateSimpleLocalRoleDto(roleDto *model.SimpleLocalRoleDto) error {
	if len(roleDto.Name) == 0 || len(roleDto.DisplayName) == 0 {
		return exterror.Catch(exterror.New().AuthServer3007Error, nil)
	}

	if len(roleDto.Email) > 0 {
		if !utils.IsEmailValid(roleDto.Email) {
			return exterror.Catch(exterror.New().AuthServer3008Error, nil)
		}
	}
	return nil
}

func buildSysRoleEntity(dto *model.SimpleLocalRoleDto, curUser string) *model.SysRoleEntity {
	now := time.Now()
	return &model.SysRoleEntity{
		Id:            utils.Uuid(),
		CreatedBy:     curUser,
		UpdatedBy:     curUser,
		CreatedTime:   now,
		UpdatedTime:   now,
		Active:        true,
		Deleted:       false,
		Name:          dto.Name,
		DisplayName:   dto.DisplayName,
		EmailAddress:  dto.Email,
		Administrator: dto.Administrator,
	}
}

func (RoleManagementService) RegisterLocalRole(roleDto *model.SimpleLocalRoleDto, curUser string) (*model.SimpleLocalRoleDto, error) {
	validateSimpleLocalRoleDto(roleDto)

	existedRoles, err := db.RoleRepositoryInstance.FindAllRolesByName(roleDto.Name)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to find all roles by name", zap.String("name", roleDto.Name), zap.Error(err))
		return nil, err
	}

	if len(existedRoles) > 0 {
		return nil, exterror.Catch(exterror.New().AuthServer3004Error.WithParam(roleDto.Name), nil)
	}

	role := buildSysRoleEntity(roleDto, curUser)
	affect, err := db.Engine.Insert(role)
	if affect == 0 || err != nil {
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to insert role", log.JsonObj("role", role), zap.Error(err))
		}
		return nil, fmt.Errorf("failed to registerLocalRole:" + roleDto.Name)
	}

	return convertToSimpleLocalRoleDto(role), nil
}

func (RoleManagementService) RetrieveAllLocalRoles(requiredAll bool) ([]*model.SimpleLocalRoleDto, error) {
	var roles []*model.SysRoleEntity
	if requiredAll {
		if result, err := db.RoleRepositoryInstance.FindAllRoles(); err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to find all roles", zap.Error(err))
			return nil, err
		} else {
			roles = result
		}
	} else {
		if result, err := db.RoleRepositoryInstance.FindAllActiveRoles(); err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to find all active roles", zap.Error(err))
			return nil, err
		} else {
			roles = result
		}
	}
	result := make([]*model.SimpleLocalRoleDto, 0)

	if len(roles) == 0 {
		return result, nil
	}

	for _, role := range roles {
		result = append(result, convertToSimpleLocalRoleDto(role))
	}
	return result, nil
}

// @Transactional
func (RoleManagementService) UnregisterLocalRoleById(roleId string, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	role := &model.SysRoleEntity{}
	found, err := session.ID(roleId).Get(role)
	if !found || err != nil {
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to get role", zap.String("roleId", roleId), zap.Error(err))
		}
		return exterror.Catch(exterror.New().AuthServer3012Error, nil)
	}

	if role.Deleted {
		//throw new AuthServerException("3005", msg, roleId);
		return exterror.Catch(exterror.New().AuthServer3005Error.WithParam(roleId), nil)
	}

	role.Active = false
	role.Deleted = true
	role.UpdatedBy = curUser
	role.UpdatedTime = time.Now()
	affecCnt, err := session.ID(role.Id).UseBool().Update(role)
	if affecCnt == 0 || err != nil {
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to update role", log.JsonObj("role", role), zap.Error(err))
		}
		session.Rollback()
		return fmt.Errorf("failed to UnregisterLocalRoleById %v", roleId)
	}

	userRoles, err := db.UserRoleRsRepositoryInstance.FindAllByRoleId(role.Id)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to find all by roleId", zap.String("role id", role.Id), zap.Error(err))
		session.Rollback()
		return err
	}

	if len(userRoles) == 0 {
		userRoles = make([]*model.UserRoleRsEntity, 0)
	}

	for _, userRole := range userRoles {
		userRole.Active = false
		userRole.UpdatedBy = curUser
		userRole.Deleted = true
		userRole.UpdatedTime = time.Now()
		affected, err := session.ID(userRole.Id).UseBool().Update(userRole)
		if err != nil || affected == 0 {
			session.Rollback()
			if err != nil {
				log.Error(nil, log.LOGGER_APP, "failed to update user role", log.JsonObj("userRole", userRole), zap.Error(err))
			}
			return fmt.Errorf("failed to UpdateLocalRole: %v", roleId)
		}
	}
	session.Commit()
	return nil
}

func (RoleManagementService) RetriveLocalRoleByRoleId(roleId string) (*model.SimpleLocalRoleDto, error) {
	role := &model.SysRoleEntity{}
	existed, err := db.Engine.ID(roleId).Get(role)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to get role", zap.String("roleId", roleId), zap.Error(err))
		return nil, err
	}
	if !existed {
		return nil, exterror.Catch(exterror.New().AuthServer3006Error.WithParam(roleId), nil)
	}

	return convertToSimpleLocalRoleDto(role), nil
}

func (RoleManagementService) RetrieveAllAuthoritiesByRoleId(roleId string) ([]*model.SimpleAuthorityDto, error) {
	result := make([]*model.SimpleAuthorityDto, 0)
	role := &model.SysRoleEntity{}
	existed, err := db.Engine.ID(roleId).Get(role)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to get role", zap.String("roleId", roleId), zap.Error(err))
		return nil, err
	}
	if !existed {
		return result, nil
	}

	if role.Deleted || !role.Active {
		log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("such role is deleted or inactive,role id %v", roleId))
		return result, nil
	}

	roleAuthorities, err := db.RoleAuthorityRsRepositoryInstance.FindAllConfiguredAuthoritiesByRoleId(role.Id)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to find all configured auth by roleId", zap.String("role id", role.Id), zap.Error(err))
		return nil, err
	}

	for _, roleAuthority := range roleAuthorities {
		authority := &model.SysAuthorityEntity{}
		existed, err := db.Engine.ID(roleAuthority.AuthorityID).Get(authority)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to get authority", zap.String("roleId", roleId), zap.Error(err))
			return nil, err
		}
		if !existed {
			log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("authority entity does not exist, authority id %v", roleAuthority.AuthorityID))
			continue
		}

		if authority.Deleted {
			log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("such authority is deleted,authority:%v %v", authority.Id, authority.Code))
			continue
		}

		dto := model.SimpleAuthorityDto{
			Active:      authority.Active,
			Code:        authority.Code,
			Description: authority.Description,
			DisplayName: authority.DisplayName,
			ID:          authority.Id,
			Scope:       authority.Scope,
		}

		result = append(result, &dto)
	}

	return result, nil
}

// @Transactional
func (RoleManagementService) ConfigureRoleWithAuthorities(grantDto *model.RoleAuthoritiesDto, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	role := &model.SysRoleEntity{}
	var err error
	var found bool

	if len(grantDto.RoleId) > 0 {
		found, err = session.ID(grantDto.RoleId).Get(role)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to get role", zap.String("role id", grantDto.RoleId), zap.Error(err))
			session.Rollback()
			return err
		}
	}
	if !found && len(grantDto.RoleName) > 0 {
		if role, err = db.RoleRepositoryInstance.FindNotDeletedRoleByName(grantDto.RoleName); err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to find not deleted role by name", zap.String("roleName", grantDto.RoleName), zap.Error(err))
			session.Rollback()
			return err
		}
	}

	if role == nil {
		log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("such role entity does not exist,role id %v, role name %v ", grantDto.RoleId,
			grantDto.RoleName))
		session.Rollback()
		return exterror.Catch(exterror.New().AuthServer3009Error, nil)
	}

	for _, authorityDto := range grantDto.Authorities {
		if len(authorityDto.ID) == 0 && len(authorityDto.Code) == 0 {
			log.Debug(nil, log.LOGGER_APP, "The ID and code of authority to configure is blank.")
			session.Rollback()
			return exterror.Catch(exterror.New().AuthServer3010Error, nil)
		}

		log.Info(nil, log.LOGGER_APP, fmt.Sprintf("configure role %v with authority %v-%v", role.Name, authorityDto.ID,
			authorityDto.Code))

		authority := &model.SysAuthorityEntity{}
		if len(authorityDto.ID) > 0 {
			existed := false
			existed, err = session.ID(authorityDto.ID).Get(authority)
			if err != nil {
				log.Error(nil, log.LOGGER_APP, "failed to get authority", zap.String("authority id", authorityDto.ID), zap.Error(err))
				session.Rollback()
				return err
			}
			if !existed {
				log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("such authority entity does not exist,authority id %v", authorityDto.ID))
				//msg := fmt.Sprintf("Authority with {%s} does not exist.", authorityDto.ID)
				//throw new AuthServerException("3011", msg, authorityDto.getId());
				session.Rollback()
				return exterror.Catch(exterror.New().AuthServer3011Error.WithParam(authorityDto.ID), nil)
			}

		} else {
			authority, err = db.AuthorityRepositoryInstance.FindNotDeletedOneByCode(authorityDto.Code)
			if err != nil {
				log.Error(nil, log.LOGGER_APP, "failed to find not deleted auth by code", zap.String("code", authorityDto.Code), zap.Error(err))
				session.Rollback()
				return err
			}
			if authority == nil {
				scope := authorityDto.Scope
				if len(scope) == 0 {
					scope = constant.SCOPE_GLOBAL
				}

				displayName := authorityDto.DisplayName
				if len(displayName) == 0 {
					displayName = authorityDto.Code
				}
				authority = &model.SysAuthorityEntity{
					Id:          utils.Uuid(),
					Active:      true,
					Code:        authorityDto.Code,
					CreatedBy:   curUser,
					Deleted:     false,
					Scope:       scope,
					Description: authorityDto.Description,
					DisplayName: displayName,
				}
				session.Insert(authority)
			}
		}

		roleAuthority, err := db.RoleAuthorityRsRepositoryInstance.FindOneByRoleIdAndAuthorityId(role.Id, authority.Id, session)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to find role auth rs", zap.String("role id", role.Id),
				zap.String("authority id", authority.Id), zap.Error(err))
			session.Rollback()
			return err
		}

		if roleAuthority != nil {
			continue
		}

		roleAuthority = &model.RoleAuthorityRsEntity{
			Id:            utils.Uuid(),
			Active:        true,
			AuthorityCode: authority.Code,
			AuthorityID:   authority.Id,
			CreatedBy:     curUser,
			Deleted:       false,
			RoleID:        role.Id,
			RoleName:      role.Name,
		}

		session.Insert(roleAuthority)
	}
	session.Commit()
	return nil
}

// @Transactional
func (RoleManagementService) ConfigureRoleWithAuthoritiesById(roleId string, authorityDtos []*model.SimpleAuthorityDto, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	role := &model.SysRoleEntity{}
	found, err := session.ID(roleId).Get(role)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to get role", zap.String("roleId", roleId), zap.Error(err))
		session.Rollback()
		return err
	}

	if !found {
		session.Rollback()
		return exterror.Catch(exterror.New().AuthServer3012Error, nil)
	}

	for _, authorityDto := range authorityDtos {
		if len(authorityDto.ID) == 0 && len(authorityDto.Code) == 0 {
			log.Debug(nil, log.LOGGER_APP, "The ID and code of authority to configure is blank.")
			session.Rollback()
			return exterror.Catch(exterror.New().AuthServer3013Error, nil)
		}

		log.Info(nil, log.LOGGER_APP, fmt.Sprintf("configure role %v with authority %v-%v", role.Name, authorityDto.ID,
			authorityDto.Code))

		authority := &model.SysAuthorityEntity{}
		if len(authorityDto.ID) > 0 {
			found, err := session.ID(authorityDto.ID).Get(authority)
			if err != nil {
				log.Error(nil, log.LOGGER_APP, "failed to get authority", zap.String("authorityId", authorityDto.ID), zap.Error(err))
				session.Rollback()
				return err
			}
			if !found {
				session.Rollback()
				log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("such authority entity does not exist,authority id %v", authorityDto.ID))
				return exterror.Catch(exterror.New().AuthServer3014Error.WithParam(authorityDto.ID), nil)
				//throw new AuthServerException("3014", msg, authorityDto.getId());
			}
		} else {
			authority, err = db.AuthorityRepositoryInstance.FindNotDeletedOneByCode(authorityDto.Code)
			if err != nil {
				log.Error(nil, log.LOGGER_APP, "failed to find not deleted authority by code", zap.String("authority code", authorityDto.Code),
					zap.Error(err))
				session.Rollback()
				return err
			}
			if authority == nil {
				scope := authorityDto.Scope
				if len(scope) == 0 {
					scope = constant.SCOPE_GLOBAL
				}
				displayName := authorityDto.DisplayName
				if len(displayName) == 0 {
					displayName = authorityDto.Code
				}

				authority = &model.SysAuthorityEntity{
					Id:          utils.Uuid(),
					Active:      true,
					Code:        authorityDto.Code,
					CreatedBy:   curUser,
					Deleted:     false,
					Scope:       scope,
					Description: authorityDto.Description,
					DisplayName: displayName,
				}
				if result, err := session.Insert(authority); result == 0 || err != nil {
					session.Rollback()
					if err != nil {
						log.Error(nil, log.LOGGER_APP, "failed to insert authority", log.JsonObj("authority", authority), zap.Error(err))
					}
					return errors.New("failed to insert authority")
				}
			}

		}

		roleAuthority, err := db.RoleAuthorityRsRepositoryInstance.FindOneByRoleIdAndAuthorityId(role.Id, authority.Id, session)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to query role authority rs")
			session.Rollback()
			return err
		}

		if roleAuthority != nil {
			continue
		}

		roleAuthority = &model.RoleAuthorityRsEntity{
			Id:            utils.Uuid(),
			Active:        true,
			AuthorityCode: authority.Code,
			AuthorityID:   authority.Id,
			CreatedBy:     curUser,
			Deleted:       false,
			RoleID:        role.Id,
			RoleName:      role.Name,
		}
		if result, err := session.Insert(roleAuthority); result == 0 || err != nil {
			session.Rollback()
			if err != nil {
				log.Error(nil, log.LOGGER_APP, "failed to insert authority rs", log.JsonObj("roleAuthority", roleAuthority), zap.Error(err))
			}
			return errors.New("failed to insert authority rs")
		}
	}
	session.Commit()
	return nil
}

// @Transactional
func (RoleManagementService) RevokeRoleAuthorities(revocationDto *model.RoleAuthoritiesDto, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	role := &model.SysRoleEntity{}
	var err error
	var found bool

	if len(revocationDto.RoleId) > 0 {
		found, err = session.ID(revocationDto.RoleId).Get(role)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to get role", zap.String("role id", revocationDto.RoleId), zap.Error(err))
			session.Rollback()
			return err
		}

	}

	if !found && len(revocationDto.RoleName) > 0 {
		role, err = db.RoleRepositoryInstance.FindNotDeletedRoleByName(revocationDto.RoleName)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to find not deleted role by name", zap.String("roleName", revocationDto.RoleName),
				zap.Error(err))
			session.Rollback()
			return err
		}
	}

	if role == nil {
		log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("such role entity does not exist,role id %v, role name %v ", revocationDto.RoleId,
			revocationDto.RoleName))
		//throw new AuthServerException("3012", "Such role entity does not exist.");
		session.Rollback()
		return exterror.Catch(exterror.New().AuthServer3012Error, nil)
	}

	for _, authorityDto := range revocationDto.Authorities {
		if len(authorityDto.ID) == 0 && len(authorityDto.Code) == 0 {
			continue
		}

		var roleAuthority *model.RoleAuthorityRsEntity
		if len(authorityDto.ID) == 0 {
			roleAuthority, err = db.RoleAuthorityRsRepositoryInstance.FindOneByRoleIdAndAuthorityCode(role.Id, authorityDto.Code, session)
			if err != nil {
				log.Error(nil, log.LOGGER_APP, "failed to query role authority rs", zap.Error(err))
				session.Rollback()
				return err
			}

		} else {
			roleAuthority, err = db.RoleAuthorityRsRepositoryInstance.FindOneByRoleIdAndAuthorityId(role.Id, authorityDto.ID, session)
			if err != nil {
				log.Error(nil, log.LOGGER_APP, "failed to query role authority rs", zap.Error(err))
				session.Rollback()
				return err
			}
		}

		if roleAuthority == nil {
			continue
		}

		roleAuthority.Active = false
		roleAuthority.Deleted = true
		roleAuthority.UpdatedBy = curUser
		roleAuthority.UpdatedTime = time.Now()

		if result, err := session.ID(roleAuthority.Id).UseBool().Update(roleAuthority); result == 0 || err != nil {
			session.Rollback()
			if err != nil {
				log.Error(nil, log.LOGGER_APP, "failed to update authority rs", log.JsonObj("roleAuthority", roleAuthority), zap.Error(err))
			}
			return errors.New("failed to update authority rs")
		}
		//roleAuthorityRsRepository.save(roleAuthority)
	}
	session.Commit()
	return nil
}

// @Transactional
func (RoleManagementService) RevokeRoleAuthoritiesById(roleId string, authorityDtos []*model.SimpleAuthorityDto, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	role := &model.SysRoleEntity{}
	found, err := session.ID(roleId).Get(role)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to get role", zap.String("roleId", roleId), zap.Error(err))
		session.Rollback()
		return err
	}

	if !found {
		session.Rollback()
		return exterror.Catch(exterror.New().AuthServer3012Error, nil)
	}

	for _, authorityDto := range authorityDtos {
		if len(authorityDto.ID) == 0 && len(authorityDto.Code) == 0 {
			continue
		}

		var roleAuthority *model.RoleAuthorityRsEntity
		if len(authorityDto.ID) == 0 {
			roleAuthority, err = db.RoleAuthorityRsRepositoryInstance.FindOneByRoleIdAndAuthorityCode(role.Id, authorityDto.Code, session)
			if err != nil {
				log.Error(nil, log.LOGGER_APP, "failed to query role authority rs", zap.Error(err))
				session.Rollback()
				return err
			}
		} else {
			roleAuthority, err = db.RoleAuthorityRsRepositoryInstance.FindOneByRoleIdAndAuthorityId(role.Id, authorityDto.ID, session)
			if err != nil {
				log.Error(nil, log.LOGGER_APP, "failed to query role authority rs", zap.Error(err))
				session.Rollback()
				return err
			}
		}

		if roleAuthority == nil {
			continue
		}

		roleAuthority.Active = false
		roleAuthority.Deleted = true
		roleAuthority.UpdatedBy = curUser
		roleAuthority.UpdatedTime = time.Now()

		if result, err := session.ID(roleAuthority.Id).UseBool().Update(roleAuthority); result == 0 || err != nil {
			session.Rollback()
			if err != nil {
				log.Error(nil, log.LOGGER_APP, "failed to update authority rs", log.JsonObj("roleAuthority", roleAuthority), zap.Error(err))
			}
			return errors.New("failed to update authority rs")
		}
		//roleAuthorityRsRepository.save(roleAuthority);
	}
	session.Commit()
	return nil
}
