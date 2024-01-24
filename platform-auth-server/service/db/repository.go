package db

import (
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"xorm.io/xorm"
)

var AuthorityRepositoryInstance AuthorityRepository

type AuthorityRepository struct {
}

func (AuthorityRepository) FindNotDeletedOneByCode(code string) (*model.SysAuthorityEntity, error) {
	authority := &model.SysAuthorityEntity{}
	found, err := Engine.Where("code = ?", code).And("is_deleted = ?", false).Get(authority)
	if err != nil {
		return nil, err
	}
	if found {
		return authority, nil
	} else {
		return nil, nil
	}
}

func (AuthorityRepository) FindAllNotDeletedAuthorities() ([]*model.SysAuthorityEntity, error) {
	var authorities []*model.SysAuthorityEntity
	err := Engine.Where("is_deleted = ?", false).Find(&authorities)
	if err != nil {
		return nil, err
	}
	return authorities, nil
}

var RoleAuthorityRsRepositoryInstance RoleAuthorityRsRepository

type RoleAuthorityRsRepository struct {
}

func (RoleAuthorityRsRepository) FindAllConfiguredAuthoritiesByRoleId(roleId string) ([]*model.RoleAuthorityRsEntity, error) {
	var authorities []*model.RoleAuthorityRsEntity
	err := Engine.Where("role_id = ?", roleId).And("is_deleted = ?", false).Find(&authorities)
	if err != nil {
		return nil, err
	}
	return authorities, nil
}

func (RoleAuthorityRsRepository) FindOneByRoleIdAndAuthorityId(roleId, authorityId string, session *xorm.Session) (*model.RoleAuthorityRsEntity, error) {
	authority := &model.RoleAuthorityRsEntity{}
	if session == nil {
		session := Engine.NewSession()
		defer session.Close()
	}
	found, err := session.Where("role_id = ?", roleId).And("authority_id = ?", authorityId).And("is_deleted = ?", false).Get(authority)
	if err != nil {
		return nil, err
	}
	if found {
		return authority, nil
	} else {
		return nil, nil
	}
}

func (RoleAuthorityRsRepository) FindOneByRoleIdAndAuthorityCode(roleId, authorityCode string, session *xorm.Session) (*model.RoleAuthorityRsEntity, error) {
	authority := &model.RoleAuthorityRsEntity{}
	if session == nil {
		session := Engine.NewSession()
		defer session.Close()
	}
	found, err := session.Where("role_id = ?", roleId).And("authority_code = ?", authorityCode).And("is_deleted = ?", false).Get(authority)
	if err != nil {
		return nil, err
	}

	if found {
		return authority, nil
	} else {
		return nil, nil
	}
}

var RoleRepositoryInstance RoleRepository

type RoleRepository struct {
}

func (RoleRepository) FindNotDeletedRoleByName(name string) (*model.SysRoleEntity, error) {
	role := &model.SysRoleEntity{}
	found, err := Engine.Where("name = ?", name).And("is_deleted = ?", false).Get(role)
	if err != nil {
		return nil, err
	}
	if found {
		return role, nil
	} else {
		return nil, nil
	}
}

func (RoleRepository) FindNotDeletedRolesById(roleId string) (*model.SysRoleEntity, error) {
	role := &model.SysRoleEntity{}
	found, err := Engine.Where("id = ?", roleId).And("is_deleted = ?", false).Get(role)
	if err != nil {
		return nil, err
	}
	if found {
		return role, nil
	} else {
		return nil, nil
	}
}

func (RoleRepository) FindAllRolesByName(name string) ([]*model.SysRoleEntity, error) {
	var roles []*model.SysRoleEntity
	err := Engine.Where("name = ?", name).Find(&roles)
	if err != nil {
		return nil, err
	}
	return roles, nil
}

func (RoleRepository) FindAllActiveRoles() ([]*model.SysRoleEntity, error) {
	var roles []*model.SysRoleEntity
	err := Engine.Where("is_active = ?", true).And("is_deleted = ?", false).Find(&roles)
	if err != nil {
		return nil, err
	}
	return roles, nil
}

func (RoleRepository) FindAllRoles() ([]*model.SysRoleEntity, error) {
	var roles []*model.SysRoleEntity
	err := Engine.Find(&roles)
	if err != nil {
		return nil, err
	}
	return roles, nil
}

var SubSystemAuthorityRsRepositoryInstance SubSystemAuthorityRsRepository

type SubSystemAuthorityRsRepository struct {
}

func (SubSystemAuthorityRsRepository) FindAllBySubSystemId(subSystemId string) ([]*model.SubSystemAuthorityRsEntity, error) {
	var authorities []*model.SubSystemAuthorityRsEntity
	err := Engine.Where("sub_system_id = ?", subSystemId).And("is_deleted = ?", false).Find(&authorities)
	if err != nil {
		return nil, err
	}
	return authorities, nil
}

var SubSystemRepositoryInstance SubSystemRepository

type SubSystemRepository struct {
}

func (SubSystemRepository) FindOneBySystemCode(systemCode string) (*model.SysSubSystemEntity, error) {
	subSystem := &model.SysSubSystemEntity{}
	found, err := Engine.Where("system_code = ?", systemCode).Get(subSystem)
	if err != nil {
		return nil, err
	}

	if found {
		return subSystem, nil
	} else {
		return nil, nil
	}
}

func (SubSystemRepository) FindOneBySystemName(name string) (*model.SysSubSystemEntity, error) {
	subSystem := &model.SysSubSystemEntity{}
	found, err := Engine.Where("name = ?", name).Get(subSystem)
	if err != nil {
		return nil, err
	}

	if found {
		return subSystem, nil
	} else {
		return nil, nil
	}
}

var UserRepositoryInstance UserRepository

type UserRepository struct {
}

func (UserRepository) FindNotDeletedUserByUsername(username string) (*model.SysUserEntity, error) {
	user := &model.SysUserEntity{}
	found, err := Engine.Where("username = ?", username).And("is_deleted = ?", false).Get(user)
	if err != nil {
		return nil, err
	}

	if found {
		return user, nil
	} else {
		return nil, nil
	}
}

func (UserRepository) FindAllActiveUsers() ([]*model.SysUserEntity, error) {
	var users []*model.SysUserEntity
	err := Engine.Where("is_deleted = ?", false).And("is_active = ?", true).And("is_blocked = ?", false).Find(&users)
	if err != nil {
		return nil, err
	}
	return users, nil
}

var UserRoleRsRepositoryInstance UserRoleRsRepository

type UserRoleRsRepository struct {
}

func (UserRoleRsRepository) FindAllByRoleId(roleId string) ([]*model.UserRoleRsEntity, error) {
	var userRoleRsList []*model.UserRoleRsEntity
	err := Engine.Where("role_id = ?", roleId).And("is_deleted = ?", false).Find(&userRoleRsList)
	if err != nil {
		return nil, err
	}
	return userRoleRsList, nil
}

func (UserRoleRsRepository) FindAllByUserId(userId string) ([]*model.UserRoleRsEntity, error) {
	var userRoleRsList []*model.UserRoleRsEntity
	err := Engine.Where("user_id = ?", userId).And("is_deleted = ?", false).Find(&userRoleRsList)
	if err != nil {
		return nil, err
	}
	return userRoleRsList, nil
}

func (UserRoleRsRepository) FindOneByUserIdAndRoleId(userId string, roleId string, session *xorm.Session) (*model.UserRoleRsEntity, error) {
	if session == nil {
		session := Engine.NewSession()
		defer session.Close()
	}

	userRoleRs := &model.UserRoleRsEntity{}
	found, err := session.Where("user_id = ?", userId).And("role_id = ?", roleId).And("is_deleted = ?", false).Get(userRoleRs)
	if err != nil {
		return nil, err
	}

	if found {
		return userRoleRs, nil
	} else {
		return nil, nil
	}
}

func (UserRoleRsRepository) FindRoleAdministrator(roleName string) (*model.UserRoleRsEntity, error) {
	roleUserEntity := &model.UserRoleRsEntity{}
	found, err := Engine.Where("role_name = ?", roleName).And("is_admin = ?", true).Get(roleUserEntity)
	if err != nil {
		return nil, err
	}
	if found {
		return roleUserEntity, nil
	} else {
		return nil, nil
	}
}
