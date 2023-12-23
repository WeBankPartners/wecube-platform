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

func (RoleManagementService) retrieveLocalRoleByRoleName(roleName string) (*model.SimpleLocalRoleDto, error) {
	if len(roleName) == 0 {
		return nil, exterror.Catch(exterror.New().AuthServer3002Error, nil)
	}

	existedRole, err := db.RoleRepositoryInstance.FindNotDeletedRoleByName(roleName)
	if err != nil {
		return nil, err
	}

	if existedRole == nil {
		return nil, exterror.Catch(exterror.New().AuthServer3003Error, nil)
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
		ID:          role.Id,
		Email:       role.EmailAddress,
		Name:        role.Name,
		DisplayName: role.DisplayName,
		Status:      status,
	}
}

func (RoleManagementService) UpdateLocalRole(roleDto model.SimpleLocalRoleDto, curUser string) (*model.SimpleLocalRoleDto, error) {
	var role *model.SysRoleEntity
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	existed, err := session.ID(roleDto.ID).Get(&role)
	if err != nil {
		return nil, err
	}
	if !existed {
		return nil, exterror.Catch(exterror.New().AuthServer3006Error, nil)
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
			session.Rollback()
			return nil, err
		}
		existRoleDeletedStatus := role.Deleted
		if inputRoleDeletedStatus != existRoleDeletedStatus {

			if existRoleDeletedStatus == false {
				// NotDeleted -> Deleted
				role.Deleted = true
				role.Active = false

				userRoles, err := db.UserRoleRsRepositoryInstance.FindAllByRoleId(role.Id)
				if err != nil {
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
					affected, err := session.Update(userRole)
					if err != nil || affected == 0 {
						session.Rollback()
						if err != nil {
							log.Logger.Error("failed to update user role", log.JsonObj("userRole", userRole), log.Error(err))
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

	affected, err := session.Update(role)
	if err != nil || affected == 0 {
		session.Rollback()
		if err != nil {
			log.Logger.Error("failed to update role", log.JsonObj("role", role), log.Error(err))
		}
		return nil, fmt.Errorf("failed to UpdateLocalRole: %v(%v)", roleDto.ID, roleDto.Name)
	}

	session.Commit()
	return convertToSimpleLocalRoleDto(role), nil
}

func validateRoleStatus(status string) bool {
	if len(status) == 0 {
		return false
	}
	if strings.ToLower(StatusDeleted) == strings.ToLower(status) || strings.ToLower(StatusNoDeleted) == strings.ToLower(status) {
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
	return &model.SysRoleEntity{
		CreatedBy:    curUser,
		DisplayName:  dto.DisplayName,
		Name:         dto.Name,
		EmailAddress: dto.Email,
	}
}

func (RoleManagementService) RegisterLocalRole(roleDto *model.SimpleLocalRoleDto, curUser string) (*model.SimpleLocalRoleDto, error) {
	validateSimpleLocalRoleDto(roleDto)

	existedRoles, err := db.RoleRepositoryInstance.FindAllRolesByName(roleDto.Name)
	if err != nil {
		return nil, err
	}

	if len(existedRoles) > 0 {
		return nil, exterror.Catch(exterror.New().AuthServer3004Error, nil)
	}

	role := buildSysRoleEntity(roleDto, curUser)
	affect, err := db.Engine.Insert(role)
	if affect == 0 || err != nil {
		if err != nil {
			log.Logger.Error("failed to insert role", log.JsonObj("role", role), log.Error(err))
		}
		return nil, fmt.Errorf("failed to registerLocalRole:" + roleDto.Name)
	}

	return convertToSimpleLocalRoleDto(role), nil
}

func (RoleManagementService) RetrieveAllLocalRoles(requiredAll bool) ([]*model.SimpleLocalRoleDto, error) {
	var roles []*model.SysRoleEntity
	if requiredAll {
		if result, err := db.RoleRepositoryInstance.FindAllRoles(); err != nil {
			return nil, err
		} else {
			roles = result
		}
	} else {
		if result, err := db.RoleRepositoryInstance.FindAllActiveRoles(); err != nil {
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

//@Transactional
func (RoleManagementService) UnregisterLocalRoleById(roleId string, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	var role *model.SysRoleEntity
	affected, err := session.ID(roleId).Get(&role)
	if !affected || err != nil {
		if err != nil {
			log.Logger.Error("failed to get role", log.String("roleId", roleId), log.Error(err))
		}
		return exterror.Catch(exterror.New().AuthServer3012Error, nil)
	}

	if role.Deleted {
		//throw new AuthServerException("3005", msg, roleId);
		return exterror.Catch(exterror.New().AuthServer3005Error, nil)
	}

	role.Active = false
	role.Deleted = true
	role.UpdatedBy = curUser
	role.UpdatedTime = time.Now()
	affecCnt, err := session.Update(role)
	if affecCnt == 0 || err != nil {
		if err != nil {
			log.Logger.Error("failed to update role", log.JsonObj("role", role), log.Error(err))
		}
		session.Rollback()
		return fmt.Errorf("failed to UnregisterLocalRoleById %v", roleId)
	}

	userRoles, err := db.UserRoleRsRepositoryInstance.FindAllByRoleId(role.Id)
	if err != nil {
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
		affected, err := session.Update(userRole)
		if err != nil || affected == 0 {
			session.Rollback()
			if err != nil {
				log.Logger.Error("failed to update user role", log.JsonObj("userRole", userRole), log.Error(err))
			}
			return fmt.Errorf("failed to UpdateLocalRole: %v", roleId)
		}
	}
	session.Commit()
	return nil
}

func (RoleManagementService) RetriveLocalRoleByRoleId(roleId string) (*model.SimpleLocalRoleDto, error) {
	var role *model.SysRoleEntity
	existed, err := db.Engine.ID(roleId).Get(&role)
	if err != nil {
		return nil, err
	}
	if !existed {
		return nil, exterror.Catch(exterror.New().AuthServer3006Error, nil)
	}

	return convertToSimpleLocalRoleDto(role), nil
}

func (RoleManagementService) RetrieveAllAuthoritiesByRoleId(roleId string) ([]*model.SimpleAuthorityDto, error) {
	result := make([]*model.SimpleAuthorityDto, 0)
	var role *model.SysRoleEntity
	existed, err := db.Engine.ID(roleId).Get(&role)
	if err != nil {
		return nil, err
	}
	if !existed {
		return result, nil
	}

	if role.Deleted || !role.Active {
		log.Logger.Debug(fmt.Sprintf("such role is deleted or inactive,role id %v", roleId))
		return result, nil
	}

	roleAuthorities, err := db.RoleAuthorityRsRepositoryInstance.FindAllConfiguredAuthoritiesByRoleId(role.Id)
	if err != nil {
		return nil, err
	}

	for _, roleAuthority := range roleAuthorities {
		var authority model.SysAuthorityEntity
		existed, err := db.Engine.ID(roleId).Get(&authority)
		if err != nil {
			return nil, err
		}
		if !existed {
			log.Logger.Debug(fmt.Sprintf("authority entity does not exist, authority id %v", roleAuthority.AuthorityID))
			continue
		}

		if authority.Deleted {
			log.Logger.Debug(fmt.Sprintf("such authority is deleted,authority:%v %v", authority.Id, authority.Code))
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

//@Transactional
func (RoleManagementService) ConfigureRoleWithAuthorities(grantDto model.RoleAuthoritiesDto, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	var role *model.SysRoleEntity
	var err error

	if len(grantDto.RoleId) > 0 {
		_, err = session.ID(grantDto.RoleId).Get(&role)
		if err != nil {
			session.Rollback()
			return err
		}
	}
	if role == nil && len(grantDto.RoleName) > 0 {
		if role, err = db.RoleRepositoryInstance.FindNotDeletedRoleByName(grantDto.RoleName); err != nil {
			session.Rollback()
			return err
		}
	}

	if role == nil {
		log.Logger.Debug(fmt.Sprintf("such role entity does not exist,role id %v, role name %v ", grantDto.RoleId,
			grantDto.RoleName))
		session.Rollback()
		return exterror.Catch(exterror.New().AuthServer3009Error, nil)
	}

	for _, authorityDto := range grantDto.Authorities {
		if len(authorityDto.ID) == 0 && len(authorityDto.Code) == 0 {
			log.Logger.Debug("The ID and code of authority to configure is blank.")
			session.Rollback()
			return exterror.Catch(exterror.New().AuthServer3010Error, nil)
		}

		log.Logger.Info(fmt.Sprintf("configure role %v with authority %v-%v", role.Name, authorityDto.ID,
			authorityDto.Code))

		var authority *model.SysAuthorityEntity
		if len(authorityDto.ID) > 0 {
			existed := false
			existed, err = session.ID(authorityDto.ID).Get(&authority)
			if err != nil {
				session.Rollback()
				return err
			}
			if existed == false {
				log.Logger.Debug(fmt.Sprintf("such authority entity does not exist,authority id %v", authorityDto.ID))
				//msg := fmt.Sprintf("Authority with {%s} does not exist.", authorityDto.ID)
				//throw new AuthServerException("3011", msg, authorityDto.getId());
				session.Rollback()
				return exterror.Catch(exterror.New().AuthServer3011Error.WithParam(authorityDto.ID), nil)
			}

		} else {
			authority, err = db.AuthorityRepositoryInstance.FindNotDeletedOneByCode(authorityDto.Code)
			if err != nil {
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
			session.Rollback()
			return err
		}

		if roleAuthority != nil {
			continue
		}

		roleAuthority = &model.RoleAuthorityRsEntity{
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

//@Transactional
func (RoleManagementService) ConfigureRoleWithAuthoritiesById(roleId string, authorityDtos []model.SimpleAuthorityDto, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	var err error
	var role *model.SysRoleEntity
	_, err = session.ID(roleId).Get(&role)
	if err != nil {
		session.Rollback()
		return err
	}

	if role == nil {
		session.Rollback()
		return exterror.Catch(exterror.New().AuthServer3012Error, nil)
	}

	for _, authorityDto := range authorityDtos {
		if len(authorityDto.ID) == 0 && len(authorityDto.Code) == 0 {
			log.Logger.Debug("The ID and code of authority to configure is blank.")
			session.Rollback()
			return exterror.Catch(exterror.New().AuthServer3013Error, nil)
		}

		log.Logger.Info(fmt.Sprintf("configure role %v with authority %v-%v", role.Name, authorityDto.ID,
			authorityDto.Code))

		var authority *model.SysAuthorityEntity
		if len(authorityDto.ID) > 0 {
			_, err = session.ID(authorityDto.ID).Get(&authority)
			if err != nil {
				session.Rollback()
				return err
			}
			if authority == nil {
				session.Rollback()
				log.Logger.Debug(fmt.Sprintf("such authority entity does not exist,authority id %v", authorityDto.ID))
				return exterror.Catch(exterror.New().AuthServer3014Error.WithParam(authorityDto.ID), nil)
				//throw new AuthServerException("3014", msg, authorityDto.getId());
			}
		} else {
			authority, err = db.AuthorityRepositoryInstance.FindNotDeletedOneByCode(authorityDto.Code)
			if err != nil {
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
						log.Logger.Error("failed to insert authority", log.JsonObj("authority", authority), log.Error(err))
					}
					return errors.New("failed to insert authority")
				}
			}

		}

		roleAuthority, err := db.RoleAuthorityRsRepositoryInstance.FindOneByRoleIdAndAuthorityId(role.Id, authority.Id, session)
		if err != nil {
			log.Logger.Error("failed to query role authority rs")
			session.Rollback()
			return err
		}

		if roleAuthority != nil {
			continue
		}

		roleAuthority = &model.RoleAuthorityRsEntity{
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
				log.Logger.Error("failed to insert authority rs", log.JsonObj("roleAuthority", roleAuthority), log.Error(err))
			}
			return errors.New("failed to insert authority rs")
		}
	}
	session.Commit()
	return nil
}

//@Transactional
func (RoleManagementService) RevokeRoleAuthorities(revocationDto model.RoleAuthoritiesDto, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	var role *model.SysRoleEntity
	var err error

	if len(revocationDto.RoleId) > 0 {
		_, err = session.ID(revocationDto.RoleId).Get(&role)
		if err != nil {
			session.Rollback()
			return err
		}

	}

	if role == nil && len(revocationDto.RoleName) > 0 {
		role, err = db.RoleRepositoryInstance.FindNotDeletedRoleByName(revocationDto.RoleName)
		if err != nil {
			session.Rollback()
			return err
		}
	}

	if role == nil {
		log.Logger.Debug(fmt.Sprintf("such role entity does not exist,role id %v, role name %v ", revocationDto.RoleId,
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
				log.Logger.Error("failed to query role authority rs", log.Error(err))
				session.Rollback()
				return err
			}

		} else {
			roleAuthority, err = db.RoleAuthorityRsRepositoryInstance.FindOneByRoleIdAndAuthorityId(role.Id, authorityDto.ID, session)
			if err != nil {
				log.Logger.Error("failed to query role authority rs", log.Error(err))
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

		if result, err := session.Update(roleAuthority); result == 0 || err != nil {
			session.Rollback()
			if err != nil {
				log.Logger.Error("failed to update authority rs", log.JsonObj("roleAuthority", roleAuthority), log.Error(err))
			}
			return errors.New("failed to update authority rs")
		}
		//roleAuthorityRsRepository.save(roleAuthority)
	}
	session.Commit()
	return nil
}

//@Transactional
func (RoleManagementService) RevokeRoleAuthoritiesById(roleId string, authorityDtos []*model.SimpleAuthorityDto, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	var err error
	var role *model.SysRoleEntity
	_, err = session.ID(roleId).Get(&role)
	if err != nil {
		session.Rollback()
		return err
	}

	if role == nil {
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
				log.Logger.Error("failed to query role authority rs", log.Error(err))
				session.Rollback()
				return err
			}
		} else {
			roleAuthority, err = db.RoleAuthorityRsRepositoryInstance.FindOneByRoleIdAndAuthorityId(role.Id, authorityDto.ID, session)
			if err != nil {
				log.Logger.Error("failed to query role authority rs", log.Error(err))
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

		if result, err := session.Update(roleAuthority); result == 0 || err != nil {
			session.Rollback()
			if err != nil {
				log.Logger.Error("failed to update authority rs", log.JsonObj("roleAuthority", roleAuthority), log.Error(err))
			}
			return errors.New("failed to update authority rs")
		}
		//roleAuthorityRsRepository.save(roleAuthority);
	}
	session.Commit()
	return nil
}
