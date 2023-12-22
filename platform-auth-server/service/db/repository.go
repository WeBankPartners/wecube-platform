package db

import "github.com/WeBankPartners/wecube-platform/platform-auth-server/model"

var AuthorityRepositoryInstance AuthorityRepository

type AuthorityRepository struct {
}

func (AuthorityRepository) FindNotDeletedOneByCode(code string) (*model.SysAuthorityEntity, error) {
	authority := &model.SysAuthorityEntity{}
	_, err := Engine.Where("code = ?", code).And("deleted = ?", false).Get(authority)
	if err != nil {
		return nil, err
	}
	return authority, nil
}

func (AuthorityRepository) FindAllNotDeletedAuthorities() ([]*model.SysAuthorityEntity, error) {
	var authorities []*model.SysAuthorityEntity
	err := Engine.Where("deleted = ?", false).Find(&authorities)
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
	err := Engine.Where("role_id = ?", roleId).And("deleted = ?", false).Find(&authorities)
	if err != nil {
		return nil, err
	}
	return authorities, nil
}

func (RoleAuthorityRsRepository) FindOneByRoleIdAndAuthorityId(roleId, authorityId string) (*model.RoleAuthorityRsEntity, error) {
	authority := &model.RoleAuthorityRsEntity{}
	_, err := Engine.Where("role_id = ?", roleId).And("authority_id = ?", authorityId).And("deleted = ?", false).Get(authority)
	if err != nil {
		return nil, err
	}
	return authority, nil
}

func (RoleAuthorityRsRepository) FindOneByRoleIdAndAuthorityCode(roleId, authorityCode string) (*model.RoleAuthorityRsEntity, error) {
	authority := &model.RoleAuthorityRsEntity{}
	_, err := Engine.Where("role_id = ?", roleId).And("authority_code = ?", authorityCode).And("deleted = ?", false).Get(authority)
	if err != nil {
		return nil, err
	}
	return authority, nil
}

var RoleRepositoryInstance RoleRepository

type RoleRepository struct {
}

func (RoleRepository) FindNotDeletedRoleByName(name string) (*model.SysRoleEntity, error) {
	role := &model.SysRoleEntity{}
	_, err := Engine.Where("name = ?", name).And("deleted = ?", false).Get(role)
	if err != nil {
		return nil, err
	}
	return role, nil
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
	err := Engine.Where("active = ?", true).And("deleted = ?", false).Find(&roles)
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

type SubSystemAuthorityRsRepository struct {
}

func (SubSystemAuthorityRsRepository) FindAllBySubSystemId(subSystemId string) ([]*model.SubSystemAuthorityRsEntity, error) {
	var authorities []*model.SubSystemAuthorityRsEntity
	err := Engine.Where("sub_system_id = ?", subSystemId).And("deleted = ?", false).Find(&authorities)
	if err != nil {
		return nil, err
	}
	return authorities, nil
}

type SubSystemRepository struct {
}

func (SubSystemRepository) FindOneBySystemCode(systemCode string) (*model.SysSubSystemEntity, error) {
	subSystem := &model.SysSubSystemEntity{}
	_, err := Engine.Where("system_code = ?", systemCode).Get(subSystem)
	if err != nil {
		return nil, err
	}
	return subSystem, nil
}

func (SubSystemRepository) FindOneBySystemName(name string) (*model.SysSubSystemEntity, error) {
	subSystem := &model.SysSubSystemEntity{}
	_, err := Engine.Where("name = ?", name).Get(subSystem)
	if err != nil {
		return nil, err
	}
	return subSystem, nil
}

type UserRepository struct {
}

func (UserRepository) FindNotDeletedUserByUsername(username string) (*model.SysUserEntity, error) {
	user := &model.SysUserEntity{}
	_, err := Engine.Where("username = ?", username).And("deleted = ?", false).Get(user)
	if err != nil {
		return nil, err
	}
	return user, nil
}

func (UserRepository) FindAllActiveUsers() ([]*model.SysUserEntity, error) {
	var users []*model.SysUserEntity
	err := Engine.Where("deleted = ?", false).And("active = ?", true).And("blocked = ?", false).Find(&users)
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
	err := Engine.Where("role_id = ?", roleId).And("deleted = ?", false).Find(&userRoleRsList)
	if err != nil {
		return nil, err
	}
	return userRoleRsList, nil
}

func (UserRoleRsRepository) FindAllByUserId(userId string) ([]*model.UserRoleRsEntity, error) {
	var userRoleRsList []*model.UserRoleRsEntity
	err := Engine.Where("user_id = ?", userId).And("deleted = ?", false).Find(&userRoleRsList)
	if err != nil {
		return nil, err
	}
	return userRoleRsList, nil
}

func (UserRoleRsRepository) FindOneByUserIdAndRoleId(userId string, roleId string) (*model.UserRoleRsEntity, error) {
	userRoleRs := &model.UserRoleRsEntity{}
	_, err := Engine.Where("user_id = ?", userId).And("role_id = ?", roleId).And("deleted = ?", false).Get(userRoleRs)
	if err != nil {
		return nil, err
	}
	return userRoleRs, nil
}
