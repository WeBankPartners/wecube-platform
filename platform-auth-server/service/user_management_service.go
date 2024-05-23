package service

import (
	"context"
	"errors"
	"fmt"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/utils"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/db"
	"golang.org/x/crypto/bcrypt"
	"xorm.io/xorm"
)

const DefPasswordLength = 8

var UserManagementServiceInstance UserManagementService

type UserManagementService struct {
}

func (UserManagementService) ResetLocalUserPassword(userPassDto *model.SimpleLocalUserPassDto) (string, error) {
	username := userPassDto.Username
	if len(username) == 0 {
		return "", exterror.NewAuthServerError("Username cannot be blank.")
	}

	encoedPwd, err := doResetLocalUserPassword(username)
	return encoedPwd, err
}

func doResetLocalUserPassword(username string) (string, error) {
	user, err := db.UserRepositoryInstance.FindNotDeletedUserByUsername(username)
	if err != nil {
		log.Logger.Error("failed to find not deleted user by user name", log.String("user name", username),
			log.Error(err))
		return "", err
	}
	if user == nil {
		log.Logger.Debug(fmt.Sprintf("Such user does not exist with username %v", username))
		// msg := fmt.Sprintf("Failed to modify a none existed user with username {%s}.", username)
		return "", exterror.Catch(exterror.New().AuthServer3021Error.WithParam(username), nil)
	}

	if utils.EqualsIgnoreCase(constant.AuthSourceUm, user.AuthSource) {
		return "", exterror.NewAuthServerError("Cannot modify password of UM user account.")
	}

	ranPassword := PasswordGeneratorInstance.GenerateStrongPassword(DefPasswordLength)

	encodedNewPassword, err := encodePassword(ranPassword)
	if err != nil {
		log.Logger.Error("failed to encode password", log.Error(err))
		return "", err
	}
	if affected, err := db.Engine.ID(user.Id).Cols("password").Update(&model.SysUserEntity{Password: encodedNewPassword}); affected == 0 || err != nil {
		if err != nil {
			log.Logger.Error("failed to update user", log.Error(err))
		}
		return "", fmt.Errorf("failed to update user,err:%+v", err)
	}

	return ranPassword, nil
}

func encodePassword(rawPassword string) (string, error) {
	if len(rawPassword) == 0 {
		return "", nil
	}
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(rawPassword), 14)
	if err != nil {
		return "", err
	}
	return string(hashedPassword), nil
}

func (UserManagementService) ModifyLocalUserPassword(userPassDto *model.SimpleLocalUserPassDto) (*model.SimpleLocalUserDto, error) {
	username := userPassDto.Username
	if len(username) == 0 {
		return nil, exterror.NewAuthServerError("Username cannot be blank.")
	}

	originalPassword := userPassDto.OriginalPassword
	toChangePassword := userPassDto.ChangedPassword

	if len(originalPassword) == 0 || len(toChangePassword) == 0 {
		return nil, exterror.NewAuthServerError("Password cannot be blank.")
	}

	return doModifyLocalUserPassword(username, originalPassword, toChangePassword)
}

func doModifyLocalUserPassword(username, originalPassword, toChangePassword string) (*model.SimpleLocalUserDto, error) {
	user, err := db.UserRepositoryInstance.FindNotDeletedUserByUsername(username)
	if err != nil {
		log.Logger.Error("failed to find not deleted user by user name", log.String("user name", username),
			log.Error(err))
		return nil, err
	}
	if user == nil {
		log.Logger.Debug(fmt.Sprintf("Such user does not exist with username %s", username))
		//msg := fmt.Sprintf("Failed to modify a none existed user with username {%s}.", username)
		return nil, exterror.Catch(exterror.New().AuthServer3021Error.WithParam(username), nil)
	}

	if utils.EqualsIgnoreCase(constant.AuthSourceUm, user.AuthSource) {
		return nil, exterror.NewAuthServerError("Cannot modify password of UM user account.")
	}

	if len(user.Password) == 0 {
		return nil, exterror.NewAuthServerError("The password of user to modify is blank.")
	}

	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(originalPassword)); err != nil {
		return nil, exterror.NewAuthServerError("The password of user to modify is invalid.")
	}

	if !PasswordGeneratorInstance.CheckPasswordStrength(toChangePassword) {
		return nil, exterror.Catch(exterror.New().AuthServer3030Error, nil)
	}

	encodedNewPassword, err := encodePassword(toChangePassword)
	if err != nil {
		return nil, err
	}
	user.Password = encodedNewPassword

	user.UpdatedTime = time.Now()
	if affected, err := db.Engine.ID(user.Id).Update(user); affected == 0 || err != nil {
		if err != nil {
			log.Logger.Error(fmt.Sprintf("failed to update user:%v", user.Id), log.Error(err))
		}
		return nil, errors.New("failed to update user")
	}

	return convertToSimpleLocalUserDto(user, ""), nil

}

func convertToSimpleLocalUserDto(user *model.SysUserEntity, roleAdministrator string) *model.SimpleLocalUserDto {
	return &model.SimpleLocalUserDto{
		ID:                user.Id,
		Username:          user.Username,
		Password:          "",
		NativeName:        user.LocalName,
		Title:             user.Title,
		EmailAddr:         user.EmailAddr,
		OfficeTelNo:       user.OfficeTelNo,
		CellPhoneNo:       user.CellPhoneNo,
		Department:        user.Department,
		EnglishName:       user.EnglishName,
		Active:            user.IsActive,
		Blocked:           user.IsBlocked,
		RoleAdministrator: roleAdministrator,
	}
}

// @Transactional
func (UserManagementService) RevokeRoleFromUsers(roleId string, userDtos []*model.SimpleLocalUserDto, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	role := &model.SysRoleEntity{}
	found, err := session.ID(roleId).Get(role)
	if err != nil {
		session.Rollback()
		return err
	}

	if !found {
		session.Rollback()
		log.Logger.Debug(fmt.Sprintf("revoking user roles error:such role entity does not exist, role id %v", roleId))
		return exterror.Catch(exterror.New().AuthServer3018Error.WithParam(roleId), nil)
	}

	for _, userDto := range userDtos {
		userRole, err := db.UserRoleRsRepositoryInstance.FindOneByUserIdAndRoleId(userDto.ID, role.Id, session)
		if err != nil {
			log.Logger.Error("failed to find userRoleRs", log.String("userId", userDto.ID), log.String("roleId", role.Id),
				log.Error(err))
			session.Rollback()
			return err
		}
		if userRole == nil {
			continue
		}

		if userRole.Deleted {
			continue
		}

		userRole.Deleted = true
		userRole.UpdatedBy = curUser
		userRole.UpdatedTime = time.Now()
		if affected, err := session.ID(userRole.Id).UseBool().Update(userRole); affected == 0 || err != nil {
			if err != nil {
				log.Logger.Error(fmt.Sprintf("failed to update userRoleRs:%v", userRole.Id), log.Error(err))
			}
			session.Rollback()
			return fmt.Errorf("failed to update userRoleRs:%v", userRole.Id)
		}
	}
	session.Commit()
	return nil
}

// @Transactional
func (UserManagementService) ConfigureUserWithRoles(userId string, roleDtos []*model.SimpleLocalRoleDto, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	if len(roleDtos) == 0 {
		return nil
	}
	user := &model.SysUserEntity{}

	found, err := session.ID(userId).Get(user)
	if err != nil {
		log.Logger.Error(fmt.Sprintf("failed to get user:%v", userId), log.Error(err))
	}
	if !found {
		session.Rollback()
		return exterror.Catch(exterror.New().AuthServer3024Error.WithParam(userId), nil)
	}

	existUserRoles, err := db.UserRoleRsRepositoryInstance.FindAllByUserId(user.Id)
	if err != nil {
		log.Logger.Error("failed to find UserRoleRs by userId", log.String("userId", user.Id),
			log.Error(err))
		session.Rollback()
		return err
	}

	if existUserRoles == nil {
		existUserRoles = make([]*model.UserRoleRsEntity, 0)
	}

	remainUserRoleMap := make(map[string]string)
	// remainUserRoles := make([]*model.UserRoleRsEntity, 0)

	for _, roleDto := range roleDtos {
		role := &model.SysRoleEntity{}
		found, err := session.ID(roleDto.ID).Get(role)
		if err != nil {
			log.Logger.Error("failed to get role", log.String("roleId", roleDto.ID), log.Error(err))
			session.Rollback()
			return err
		}
		if !found {
			session.Rollback()
			return exterror.Catch(exterror.New().AuthServer3012Error, nil)
		}

		if role.Deleted {
			continue
		}
		foundUserRoles := findFromUserRoles(existUserRoles, role.Id)
		// remainUserRoles = append(remainUserRoles, foundUserRoles...)
		for _, userRole := range foundUserRoles {
			remainUserRoleMap[userRole.Id] = "1"
		}

		userRole, err := db.UserRoleRsRepositoryInstance.FindOneByUserIdAndRoleId(userId, role.Id, session)
		if err != nil {
			log.Logger.Error("failed to find UserRoleRs", log.String("userId", userId), log.String("roleId", role.Id),
				log.Error(err))
			session.Rollback()
			return err
		}
		if userRole != nil {
			log.Logger.Info(fmt.Sprintf("such user userRole configuration already exist,userId=%s,roleId=%s", userId, role.Id))
			continue
		} else {
			userRole := &model.UserRoleRsEntity{
				Id:        utils.Uuid(),
				CreatedBy: curUser,
				UserId:    userId,
				Username:  user.Username,
				RoleId:    role.Id,
				RoleName:  role.Name,
				Active:    true,
				Deleted:   false,
			}
			affected, err := session.Insert(userRole)
			if err != nil || affected == 0 {
				if err != nil {
					log.Logger.Error("failed to insert userRole", log.Error(err))
				}
				session.Rollback()
				return errors.New("failed to insert userRole")
			}
		}

	}

	resultRoles := make([]*model.UserRoleRsEntity, 0)
	for _, userRole := range existUserRoles {
		if _, ok := remainUserRoleMap[userRole.Id]; !ok {
			resultRoles = append(resultRoles, userRole)
		}
	}
	//existUserRoles.removeAll(remainUserRoles);

	for _, ur := range resultRoles {
		ur.Active = false
		ur.Deleted = true
		ur.UpdatedBy = curUser
		ur.UpdatedTime = time.Now()

		affected, err := session.ID(ur.Id).UseBool().Update(ur)
		if affected == 0 || err != nil {
			if err != nil {
				log.Logger.Error("failed to update userRole", log.Error(err))
			}
			session.Rollback()
			return errors.New("failed to update userRole")
		}
	}

	session.Commit()
	return nil
}

func findFromUserRoles(existUserRoles []*model.UserRoleRsEntity, roleId string) []*model.UserRoleRsEntity {
	remainUserRoles := make([]*model.UserRoleRsEntity, 0)
	for _, ur := range existUserRoles {
		if roleId == ur.RoleId {
			remainUserRoles = append(remainUserRoles, ur)
		}
	}

	return remainUserRoles
}

// @Transactional
func (UserManagementService) RevokeRolesFromUser(userId string, roleDtos []*model.SimpleLocalRoleDto, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	if len(roleDtos) == 0 {
		session.Rollback()
		return nil
	}
	user := &model.SysUserEntity{}
	found, err := session.ID(userId).Get(user)
	if err != nil {
		session.Rollback()
		return err
	}

	if !found {
		session.Rollback()
		return exterror.Catch(exterror.New().AuthServer3024Error.WithParam(userId), nil)
	}

	for _, roleDto := range roleDtos {
		userRole, err := db.UserRoleRsRepositoryInstance.FindOneByUserIdAndRoleId(user.Id, roleDto.ID, session)
		if err != nil {
			session.Rollback()
			return err
		}
		if userRole == nil {
			continue
		}

		if userRole.Deleted {
			continue
		}

		userRole.Deleted = true
		userRole.UpdatedBy = curUser
		userRole.UpdatedTime = time.Now()
		affected, err := session.ID(userRole.Id).UseBool().Update(userRole)
		if affected == 0 || err != nil {
			if err != nil {
				log.Logger.Error("failed to update userRole", log.Error(err))
			}
			session.Rollback()
			return fmt.Errorf("failed to update userRole:%s", userRole.Id)
		}
	}
	session.Commit()
	return nil
}

// @Transactional
func (UserManagementService) ConfigureRoleForUsers(roleId string, userDtos []*model.SimpleLocalUserDto, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	role := &model.SysRoleEntity{}
	found, err := session.ID(roleId).Get(role)
	if err != nil {
		log.Logger.Error("failed to get role", log.String("roleId", roleId), log.Error(err))
		session.Rollback()
		return err
	}

	if !found {
		session.Rollback()
		return exterror.Catch(exterror.New().AuthServer3012Error.WithParam(roleId), nil)
	}

	if role.Deleted {
		session.Rollback()
		return nil
	}

	for _, userDto := range userDtos {

		user := &model.SysUserEntity{}

		found, err := session.ID(userDto.ID).Get(user)
		if err != nil {
			log.Logger.Error(fmt.Sprintf("failed to get user:%v", userDto.ID), log.Error(err))
		}
		if !found {
			session.Rollback()
			return exterror.Catch(exterror.New().AuthServer3019Error.WithParam(userDto.ID), nil)
		}

		userRole, err := db.UserRoleRsRepositoryInstance.FindOneByUserIdAndRoleId(userDto.ID, roleId, session)
		if err != nil {
			log.Logger.Error("failed to find UserRoleRs", log.String("userId", userDto.ID), log.String("roleId", roleId),
				log.Error(err))
			session.Rollback()
			return err
		}
		if userRole != nil {
			log.Logger.Info(fmt.Sprintf("such user role configuration already exist,userId=%s,roleId=%s", userDto.ID, roleId))
			continue
		} else {
			userRole = &model.UserRoleRsEntity{
				Id:        utils.Uuid(),
				CreatedBy: curUser,
				UserId:    userDto.ID,
				Username:  user.Username,
				RoleId:    roleId,
				RoleName:  role.Name,
				Active:    true,
				Deleted:   false,
			}

			affected, err := session.Insert(userRole)
			if err != nil || affected == 0 {
				if err != nil {
					log.Logger.Error("failed to insert userRole", log.Error(err))
				}
				session.Rollback()
				return errors.New("failed to insert userRole")
			}

		}

	}
	session.Commit()
	return nil
}

func (UserManagementService) getLocalRolesByUsername(username string) (*model.SysUserEntity, []*model.SimpleLocalRoleDto, error) {
	roleDtos := make([]*model.SimpleLocalRoleDto, 0)
	if len(username) == 0 {
		return nil, nil, exterror.Catch(exterror.New().AuthServer3020Error, nil)
	}
	user, err := db.UserRepositoryInstance.FindNotDeletedUserByUsername(username)
	if err != nil {
		log.Logger.Error("failed to find not deleted user by username", log.String("username", username),
			log.Error(err))
		return nil, nil, err
	}

	if user == nil {
		return user, roleDtos, nil
	}

	userRoles, err := db.UserRoleRsRepositoryInstance.FindAllByUserId(user.Id)
	if err != nil {
		log.Logger.Error("failed to find UserRoleRs", log.String("userId", user.Id), log.Error(err))
		return nil, nil, err
	}

	if len(userRoles) == 0 {
		return user, roleDtos, nil
	}

	for _, userRole := range userRoles {
		role := &model.SysRoleEntity{}
		found, err := db.Engine.ID(userRole.RoleId).Get(role)
		if err != nil {
			log.Logger.Error("failed to get role", log.String("roleId", userRole.RoleId),
				log.Error(err))
			return nil, nil, err
		}

		if !found {
			log.Logger.Debug(fmt.Sprintf("cannot find such role entity with role id %v", userRole.RoleId))
			continue
		}

		if role.Deleted {
			log.Logger.Debug(fmt.Sprintf("such role entity is deleted,role id %v", role.Id))
			continue
		}

		roleDto := &model.SimpleLocalRoleDto{
			ID:            role.Id,
			Name:          role.Name,
			DisplayName:   role.DisplayName,
			Email:         role.EmailAddress,
			Status:        role.GetRoleDeletedStatus(),
			Administrator: role.Administrator,
		}

		roleDtos = append(roleDtos, roleDto)
	}

	return user, roleDtos, nil
}

func (UserManagementService) GetLocalRolesByUsername(username string) ([]*model.SimpleLocalRoleDto, error) {
	_, roleDtos, err := UserManagementServiceInstance.getLocalRolesByUsername(username)
	return roleDtos, err
}

func (UserManagementService) GetAdminRolesByUsername(username string) ([]*model.SimpleLocalRoleDto, error) {
	user, roleDtos, err := UserManagementServiceInstance.getLocalRolesByUsername(username)
	if err != nil {
		return nil, err
	}
	result := make([]*model.SimpleLocalRoleDto, 0, len(roleDtos))
	if user != nil && len(roleDtos) > 0 {
		for _, roleDto := range roleDtos {
			if roleDto.Administrator == user.Id {
				result = append(result, roleDto)
			}
		}
	}
	return result, nil
}

func (UserManagementService) GetLocalUsersByRoleId(roleId string) ([]*model.SimpleLocalUserDto, error) {
	result := make([]*model.SimpleLocalUserDto, 0)

	userRoles, err := db.UserRoleRsRepositoryInstance.FindAllByRoleId(roleId)
	if err != nil {
		log.Logger.Error("failed to find UserRoleRs", log.String("roleId", roleId),
			log.Error(err))
		return nil, err
	}
	if len(userRoles) == 0 {
		return result, nil
	}

	for _, userRole := range userRoles {
		user := &model.SysUserEntity{}

		found, err := db.Engine.ID(userRole.UserId).Get(user)
		if err != nil {
			log.Logger.Error(fmt.Sprintf("failed to get user:%v", userRole.UserId), log.Error(err))
			return nil, err
		}
		if !found {
			continue
		}

		userDto := convertToSimpleLocalUserDto(user, "")
		userDto.Status = model.CalcUserRolePermissionStatus(userRole)
		if userRole.ExpireTime.Unix() > 0 {
			userDto.ExpireTime = userRole.ExpireTime.Format(constant.DateTimeFormat)
		}
		result = append(result, userDto)
	}

	return result, nil
}

func (UserManagementService) RetireveLocalUserByUserid(userId string) (*model.SimpleLocalUserDto, error) {
	var roleAdministrator string
	user := &model.SysUserEntity{}
	sysRoleList := make([]model.SysRoleEntity, 0)

	found, err := db.Engine.ID(userId).Get(user)
	if err != nil {
		log.Logger.Error(fmt.Sprintf("failed to get user:%v", userId), log.Error(err))
		return nil, err
	}

	if !found {
		log.Logger.Debug(fmt.Sprintf("Such user with ID %s does not exist.", userId))
		return nil, exterror.Catch(exterror.New().AuthServer3024Error.WithParam(userId), nil)
	}

	if user.IsDeleted {
		log.Logger.Debug(fmt.Sprintf("Such user with ID %s has already been deleted.", userId))
		return nil, exterror.Catch(exterror.New().AuthServer3024Error.WithParam(userId), nil)
	}

	err = db.Engine.Where("administrator = ?", userId).Find(&sysRoleList)
	if err != nil {
		return nil, err
	}
	if len(sysRoleList) > 0 {
		// 赋值角色名称
		roleAdministrator = sysRoleList[0].Name
	}
	return convertToSimpleLocalUserDto(user, roleAdministrator), nil
}

func (UserManagementService) RetireveLocalUserByUsername(username string) (*model.SimpleLocalUserDto, error) {
	user, err := db.UserRepositoryInstance.FindNotDeletedUserByUsername(username)
	if err != nil {
		log.Logger.Error("failed to find not deleted user by username", log.String("username", username),
			log.Error(err))
		return nil, err
	}
	if user == nil {
		log.Logger.Debug(fmt.Sprintf("Such user does not exist with username %v", username))
		return nil, exterror.Catch(exterror.New().AuthServer3021Error.WithParam(username), nil)
	}
	return convertToSimpleLocalUserDto(user, ""), nil
}

func (UserManagementService) ModifyLocalUserInfomation(username string, userDto *model.SimpleLocalUserDto, curUser string) (*model.SimpleLocalUserDto, error) {
	user, err := db.UserRepositoryInstance.FindNotDeletedUserByUsername(username)
	if err != nil {
		log.Logger.Error("failed to find not deleted user by username", log.String("username", username),
			log.Error(err))
		return nil, err
	}
	if user == nil {
		log.Logger.Debug(fmt.Sprintf("Such user does not exist with username %v", username))
		// msg := fmt.Sprintf("Failed to modify a none existed user with username {%s}.", username)
		return nil, exterror.Catch(exterror.New().AuthServer3021Error.WithParam(username), nil)
	}

	if username != userDto.Username {
		return nil, exterror.Catch(exterror.New().AuthServer3022Error, nil)
	}

	user.CellPhoneNo = userDto.CellPhoneNo
	user.Department = userDto.Department
	user.EmailAddr = userDto.EmailAddr
	user.EnglishName = userDto.EnglishName
	user.LocalName = userDto.NativeName
	user.OfficeTelNo = userDto.OfficeTelNo
	user.Title = userDto.Title
	user.CellPhoneNo = userDto.CellPhoneNo
	user.UpdatedBy = curUser
	user.UpdatedTime = time.Now()

	if affected, err := db.Engine.ID(user.Id).Update(user); affected == 0 || err != nil {
		if err != nil {
			log.Logger.Error(fmt.Sprintf("failed to update user:%v", user.Id), log.Error(err))
		}
		return nil, errors.New("failed to update user")
	}
	return convertToSimpleLocalUserDto(user, ""), nil
}

func (UserManagementService) RegisterLocalUser(userDto *model.SimpleLocalUserDto, curUser string) (*model.SimpleLocalUserDto, error) {
	if err := validateSimpleLocalUserDto(userDto); err != nil {
		return nil, err
	}

	user, err := db.UserRepositoryInstance.FindNotDeletedUserByUsername(userDto.Username)
	if err != nil {
		log.Logger.Error("failed to find not deleted user by username", log.String("username", userDto.Username),
			log.Error(err))
		return nil, err
	}
	if user != nil {

		log.Logger.Info(fmt.Sprintf("such username %s to create has already existed.", userDto.Username))
		return nil, exterror.Catch(exterror.New().AuthServer3023Error.WithParam(userDto.Username), nil)
	}

	userEntity, err := buildSysUserEntity(userDto, curUser)
	if err != nil {
		return nil, err
	}
	if cnt, err := db.Engine.Insert(userEntity); cnt == 0 || err != nil {
		if err != nil {
			log.Logger.Error("failed to insert user", log.Error(err))
		}
		return nil, errors.New("failed to inser user")
	}
	return convertToSimpleLocalUserDto(userEntity, ""), nil
}

func validateSimpleLocalUserDto(userDto *model.SimpleLocalUserDto) error {

	if len(userDto.Username) == 0 {
		return exterror.Catch(exterror.New().AuthServer3025Error, nil)
	}

	authSource := constant.AuthSourceLocal
	if len(userDto.AuthSource) > 0 {
		authSource = userDto.AuthSource
	}

	if utils.EqualsIgnoreCase(constant.AuthSourceLocal, authSource) {
		if len(userDto.Password) == 0 {
			return exterror.Catch(exterror.New().AuthServer3026Error, nil)
		}
		if !PasswordGeneratorInstance.CheckPasswordStrength(userDto.Password) {
			return exterror.Catch(exterror.New().AuthServer3030Error, nil)
		}
	}
	return nil
}

// 检查密码是否符合要求

func buildSysUserEntity(dto *model.SimpleLocalUserDto, curUser string) (*model.SysUserEntity, error) {
	now := time.Now()
	encodedNewPassword, err := encodePassword(dto.Password)
	if err != nil {
		return nil, err
	}

	return &model.SysUserEntity{
		Id:          utils.Uuid(),
		CreatedBy:   curUser,
		UpdatedBy:   curUser,
		CreatedTime: now,
		UpdatedTime: now,
		Username:    dto.Username,
		EnglishName: dto.EnglishName,
		LocalName:   dto.NativeName,
		Department:  dto.Department,
		Title:       dto.Title,
		EmailAddr:   dto.EmailAddr,
		OfficeTelNo: dto.OfficeTelNo,
		CellPhoneNo: dto.CellPhoneNo,
		Password:    encodedNewPassword,
		IsActive:    true,
		IsBlocked:   false,
		IsDeleted:   false,
		AuthSource:  dto.AuthSource,
		AuthContext: dto.AuthContext,
	}, nil
}

func (UserManagementService) RetrieveAllActiveUsers() ([]*model.SimpleLocalUserDto, error) {
	userEntities, err := db.UserRepositoryInstance.FindAllActiveUsers()
	if err != nil {
		log.Logger.Error("failed to find all active users", log.Error(err))
		return nil, err
	}

	result := make([]*model.SimpleLocalUserDto, 0)
	if len(userEntities) == 0 {
		return result, nil
	}

	for _, user := range userEntities {
		userDto := convertToSimpleLocalUserDto(user, "")

		userRoles, err := db.UserRoleRsRepositoryInstance.FindAllByUserId(user.Id)
		if err != nil {
			log.Logger.Error("failed to find all UserRoleRs", log.String("userId", user.Id),
				log.Error(err))
			return nil, err
		}
		if len(userRoles) > 0 {
			for _, userRole := range userRoles {

				role := &model.SysRoleEntity{}
				found, err := db.Engine.ID(userRole.RoleId).Get(role)
				if err != nil {
					log.Logger.Error("failed to get role", log.String("roleId", userRole.RoleId),
						log.Error(err))
					return nil, err
				}

				if found {
					roleDto := &model.SimpleLocalRoleDto{
						ID:          role.Id,
						DisplayName: role.DisplayName,
						Name:        role.Name,
						Email:       role.EmailAddress,
						Status:      role.GetRoleDeletedStatus(),
					}

					userDto.AddRoles([]*model.SimpleLocalRoleDto{roleDto})
				}
			}
		}
		result = append(result, userDto)
	}
	return result, nil
}

// @Transactional
func (UserManagementService) UnregisterLocalUser(userId string, curUser string) error {
	session := db.Engine.NewSession()
	session.Begin()
	defer session.Close()

	user := &model.SysUserEntity{}

	found, err := session.ID(userId).Get(user)
	if err != nil {
		log.Logger.Error(fmt.Sprintf("failed to get user:%v", userId), log.Error(err))
		return err
	}
	if !found {
		session.Rollback()
		return exterror.Catch(exterror.New().AuthServer3024Error.WithParam(userId), nil)
	}

	if user.IsDeleted {
		session.Rollback()
		return exterror.Catch(exterror.New().AuthServer3024Error.WithParam(userId), nil)
	}

	user.IsActive = false
	user.IsDeleted = true
	user.UpdatedBy = curUser
	user.UpdatedTime = time.Now()
	if affected, err := session.ID(user.Id).UseBool().Update(user); affected == 0 || err != nil {
		if err != nil {
			log.Logger.Error(fmt.Sprintf("failed to update user:%v", user.Id), log.Error(err))
		}
		return errors.New("failed to update user")
	}

	userRoles, err := db.UserRoleRsRepositoryInstance.FindAllByUserId(user.Id)
	if err != nil {
		log.Logger.Error("failed to find UserRoleRs by userId", log.String("userId", user.Id),
			log.Error(err))
		session.Rollback()
		return err
	}

	for _, userRole := range userRoles {
		userRole.Active = false
		userRole.Deleted = true
		userRole.UpdatedBy = curUser
		userRole.UpdatedTime = time.Now()

		if affected, err := session.ID(userRole.Id).UseBool().Update(userRole); affected == 0 || err != nil {
			if err != nil {
				log.Logger.Error(fmt.Sprintf("failed to update userRoleRs:%v", userRole.Id), log.Error(err))
			}
			session.Rollback()
			return fmt.Errorf("failed to update userRoleRs:%v", userRole.Id)
		}
	}
	session.Commit()
	return nil
}

func (UserManagementService) RegisterUmUser(param *model.RoleApplyParam, curUser string) error {
	if len(param.RoleIds) == 0 {
		return nil
	}
	if len(param.UserName) == 0 {
		return exterror.Catch(exterror.New().AuthServer3025Error, nil)
	}
	if param.UserName != curUser {
		return exterror.Catch(exterror.New().AuthServer3022Error, nil)
	}
	if !utils.IsEmailValid(param.EmailAddr) {
		return exterror.Catch(exterror.New().AuthServer3008Error, nil)
	}

	user, err := db.UserRepositoryInstance.FindNotDeletedUserByUsername(param.UserName)
	if err != nil {
		log.Logger.Error("failed to find not deleted user by username", log.String("username", param.UserName),
			log.Error(err))
		return err
	}
	if user != nil {
		// 走申请角色
		return UserManagementServiceInstance.CreateRoleApply(param, curUser)
	}

	// 过滤已申请的角色
	roleApplys, err := db.RoleApplyRepositoryInstance.FindByApplier(param.UserName, param.RoleIds, []string{model.RoleApplyStatusInit})
	if err != nil {
		return err
	}
	existRoleApplyMap := make(map[string]*model.RoleApplyEntity)
	for _, roleApply := range roleApplys {
		existRoleApplyMap[roleApply.RoleId] = roleApply
	}
	// 已申请的更新下时间
	updateRoleApplys := make([]*model.RoleApplyEntity, 0, len(param.RoleIds))
	insertRoleIds := make([]string, 0, len(param.RoleIds))
	now := time.Now()
	for _, roleId := range param.RoleIds {
		if roleApply, ok := existRoleApplyMap[roleId]; !ok {
			insertRoleIds = append(insertRoleIds, roleId)
		} else {
			updateRoleApplys = append(updateRoleApplys, &model.RoleApplyEntity{
				Id:          roleApply.Id,
				CreatedTime: now,
			})
		}
	}

	// 插入申请
	insertRoleApplys := make([]*model.RoleApplyEntity, len(insertRoleIds))
	for i, roleId := range insertRoleIds {
		insertRoleApplys[i] = &model.RoleApplyEntity{
			Id:          utils.Uuid(),
			CreatedBy:   param.UserName,
			CreatedTime: now,
			EmailAddr:   param.EmailAddr,
			RoleId:      roleId,
			Status:      model.RoleApplyStatusInit,
		}
		if param.ExpireTime != "" {
			insertRoleApplys[i].ExpireTime, _ = time.ParseInLocation(constant.DateTimeFormat, param.ExpireTime, time.Local)
		}
	}

	// 执行db操作
	if len(updateRoleApplys) == 0 && len(insertRoleApplys) == 0 {
		return nil
	}
	_, err = db.Engine.Transaction(func(session *xorm.Session) (interface{}, error) {
		for _, roleApply := range updateRoleApplys {
			if _, err := session.Update(roleApply, &model.RoleApplyEntity{Id: roleApply.Id}); err != nil {
				return nil, err
			}
		}
		if len(insertRoleApplys) > 0 {
			if _, err = db.Engine.Insert(insertRoleApplys); err != nil {
				return nil, err
			}
		}
		return nil, nil
	})
	return err
}

func (UserManagementService) CreateRoleApply(param *model.RoleApplyParam, curUser string) error {
	if len(param.RoleIds) == 0 {
		return nil
	}
	if len(param.UserName) == 0 {
		return exterror.Catch(exterror.New().AuthServer3025Error, nil)
	}
	if param.UserName != curUser {
		return exterror.Catch(exterror.New().AuthServer3022Error, nil)
	}

	user, err := db.UserRepositoryInstance.FindNotDeletedUserByUsername(param.UserName)
	if err != nil {
		log.Logger.Error("failed to find not deleted user by username", log.String("username", param.UserName),
			log.Error(err))
		return err
	}
	if user == nil {
		return exterror.Catch(exterror.New().AuthServer3021Error.WithParam(param.UserName), nil)
	}

	// 过滤已申请的角色
	roleApplys, err := db.RoleApplyRepositoryInstance.FindByApplier(param.UserName, param.RoleIds, []string{model.RoleApplyStatusInit})
	if err != nil {
		return err
	}
	existRoleApplyMap := make(map[string]*model.RoleApplyEntity)
	for _, roleApply := range roleApplys {
		existRoleApplyMap[roleApply.RoleId] = roleApply
	}

	// 过滤已有角色
	userRoles, err := db.UserRoleRsRepositoryInstance.FindAllByUserId(user.Id)
	if err != nil {
		log.Logger.Warn("failed to FindAllByUserId for userRoleRs", log.String("userId", user.Id), log.Error(err))
		return err
	}
	// 已有角色已过期,需要删除已有角色
	existUserRoleMap := make(map[string]*model.UserRoleRsEntity)
	for _, userRole := range userRoles {
		if userRole.ExpireTime.Unix() > 0 && userRole.ExpireTime.Before(time.Now()) {
			if _, err = db.Engine.Exec("update auth_sys_user_role set is_deleted = 1,updated_time = ? where id = ?", time.Now().Format(constant.DateTimeFormat), userRole.Id); err != nil {
				log.Logger.Error("update auth_sys_user_role error", log.Error(err))
			}
		} else {
			existUserRoleMap[userRole.RoleId] = userRole
		}
	}

	// 已申请的更新下时间
	updateRoleApplys := make([]*model.RoleApplyEntity, 0, len(param.RoleIds))
	insertRoleIds := make([]string, 0, len(param.RoleIds))
	now := time.Now()
	for _, roleId := range param.RoleIds {
		if _, ok := existUserRoleMap[roleId]; ok {
			return fmt.Errorf("you already have roleId: %s", roleId)
		}
		if roleApply, ok := existRoleApplyMap[roleId]; !ok {
			insertRoleIds = append(insertRoleIds, roleId)
		} else {
			roleApplyEntity := &model.RoleApplyEntity{
				Id:          roleApply.Id,
				CreatedTime: now,
			}
			if param.ExpireTime != "" {
				roleApplyEntity.ExpireTime, _ = time.ParseInLocation(constant.DateTimeFormat, param.ExpireTime, time.Local)
			}
			updateRoleApplys = append(updateRoleApplys, roleApplyEntity)
		}
	}

	// 插入申请
	insertRoleApplys := make([]*model.RoleApplyEntity, len(insertRoleIds))
	for i, roleId := range insertRoleIds {
		insertRoleApplys[i] = &model.RoleApplyEntity{
			Id:          utils.Uuid(),
			CreatedBy:   param.UserName,
			CreatedTime: now,
			EmailAddr:   param.EmailAddr,
			RoleId:      roleId,
			Status:      model.RoleApplyStatusInit,
		}
		if param.ExpireTime != "" {
			insertRoleApplys[i].ExpireTime, _ = time.ParseInLocation(constant.DateTimeFormat, param.ExpireTime, time.Local)
		}
	}

	// 执行db操作
	if len(updateRoleApplys) == 0 && len(insertRoleApplys) == 0 {
		return nil
	}
	_, err = db.Engine.Transaction(func(session *xorm.Session) (interface{}, error) {
		for _, roleApply := range updateRoleApplys {
			if param.ExpireTime != "" {
				_, err = session.Update(roleApply, &model.RoleApplyEntity{Id: roleApply.Id})
			} else {
				// 过期时间传递为空,需要更新 db expire_time设置为空
				_, err = session.Exec("update auth_sys_role_apply set created_time = ?,expire_time = null where id=?", roleApply.CreatedTime, roleApply.Id)
			}
			if err != nil {
				return nil, err
			}
		}
		if len(insertRoleApplys) > 0 {
			if _, err = session.Insert(insertRoleApplys); err != nil {
				return nil, err
			}
		}
		return nil, nil
	})
	return err
}

func (UserManagementService) ListRoleApply(ctx context.Context, param *model.QueryRequestParam, curUser string) (*model.ListRoleApplyResponse, error) {
	// 获取自己是角色管理员的已有角色
	adminRoles, err := UserManagementServiceInstance.GetAdminRolesByUsername(curUser)
	if err != nil {
		return nil, err
	}
	adminRoleIds := make([]string, len(adminRoles))
	adminRoleIdMap := make(map[string]any)
	for i, adminRole := range adminRoles {
		adminRoleIds[i] = adminRole.ID
		adminRoleIdMap[adminRole.ID] = nil
	}

	// 如果没有角色条件，自动加上；如果有，去掉自己不是角色管理员的
	var queryRoleIds []string
	var roleIds []string
	filters := make([]*model.QueryRequestFilterObj, 0, len(param.Filters))
	for _, filter := range param.Filters {
		if filter.Name == "roleId" && (filter.Operator == "eq" || filter.Operator == "in") {
			if filter.Operator == "eq" {
				if s, ok := filter.Value.(string); ok {
					queryRoleIds = append(queryRoleIds, s)
				}
			} else if filter.Operator == "in" {
				queryRoleIds = append(queryRoleIds, db.ParseFilterInValue(filter)...)
			}
		} else {
			filters = append(filters, filter)
		}
	}
	if len(queryRoleIds) == 0 {
		roleIds = adminRoleIds
	} else {
		roleIds = make([]string, 0, len(queryRoleIds))
		for _, roleId := range queryRoleIds {
			if _, ok := adminRoleIdMap[roleId]; ok {
				roleIds = append(roleIds, roleId)
			}
		}
	}
	filters = append(filters, &model.QueryRequestFilterObj{
		Name:     "roleId",
		Operator: "in",
		Value:    roleIds,
	})
	param.Filters = filters

	result, err := db.RoleApplyRepositoryInstance.Query(ctx, param)
	if err != nil {
		return nil, err
	}

	for _, content := range result.Contents {
		if content.Role.ID != "" {
			role, err := db.RoleRepositoryInstance.FindNotDeletedRolesById(content.Role.ID)
			if err != nil {
				return nil, err
			}
			if role != nil {
				content.Role = convertToSimpleLocalRoleDto(role)
			}
		}
		if content.Status == model.RoleApplyStatusApprove {
			content.Status = model.CalcUserRolePermissionStatusByApplyInfo(content)
		}
	}
	return result, err
}

func (UserManagementService) ListRoleApplyByApplier(ctx context.Context, param *model.QueryRequestParam, curUser string) (*model.ListRoleApplyResponse, error) {
	// 如果没有申请人条件，自动加上；如果有，只允许是自己
	filters := make([]*model.QueryRequestFilterObj, 0, len(param.Filters))
	for _, filter := range param.Filters {
		if filter.Name == "createdBy" && (filter.Operator == "eq" || filter.Operator == "in") {
		} else {
			filters = append(filters, filter)
		}
	}
	filters = append(filters, &model.QueryRequestFilterObj{
		Name:     "createdBy",
		Operator: "eq",
		Value:    curUser,
	})
	param.Filters = filters

	result, err := db.RoleApplyRepositoryInstance.Query(ctx, param)
	if err != nil {
		return nil, err
	}

	for _, content := range result.Contents {
		if content.Role.ID != "" {
			role, err := db.RoleRepositoryInstance.FindNotDeletedRolesById(content.Role.ID)
			if err != nil {
				return nil, err
			}
			if role != nil {
				content.Role = convertToSimpleLocalRoleDto(role)
			}
		}
		if content.Status == model.RoleApplyStatusApprove {
			content.Status = model.CalcUserRolePermissionStatusByApplyInfo(content)
			// 删除状态
			if param.Ext == string(constant.UserRolePermissionStatusDeleted) {
				content.Status = string(constant.UserRolePermissionStatusDeleted)
			}
		}
	}
	return result, err
}

func (UserManagementService) UpdateRoleApply(param []*model.RoleApplyDto, curUser string) error {
	if len(param) == 0 {
		return nil
	}
	applyIds := make([]string, len(param))
	statusMap := make(map[string]string)
	for i, apply := range param {
		applyIds[i] = apply.ID
		if apply.Status != model.RoleApplyStatusApprove && apply.Status != model.RoleApplyStatusDeny {
			return fmt.Errorf("invalid status: %s", apply.Status)
		}
		statusMap[apply.ID] = apply.Status
	}

	roleApplys, err := db.RoleApplyRepositoryInstance.FindByIDs(applyIds)
	if err != nil {
		return err
	}
	if len(roleApplys) == 0 {
		return nil
	}

	// 获取自己是角色管理员的已有角色
	adminRoles, err := UserManagementServiceInstance.GetAdminRolesByUsername(curUser)
	if err != nil {
		return err
	}
	adminRoleIdMap := make(map[string]any)
	for _, adminRole := range adminRoles {
		adminRoleIdMap[adminRole.ID] = nil
	}

	umAuthCtx, err := GetUmAuthContext(curUser)
	if err != nil {
		return err
	}

	updateRoleApplys := make([]*model.RoleApplyEntity, 0, len(roleApplys))
	insertUsers := make([]*model.SysUserEntity, 0, len(roleApplys))
	insertUsersMap := make(map[string]string)
	insertUserRoles := make([]*model.UserRoleRsEntity, 0, len(roleApplys))
	insertUserRolesMap := make(map[string]any)
	cachedRolesMap := make(map[string]*model.SysRoleEntity)
	now := time.Now()
	for _, roleApply := range roleApplys {
		if roleApply.Status != model.RoleApplyStatusInit {
			return fmt.Errorf("apply %s already handled", roleApply.Id)
		}
		if _, ok := adminRoleIdMap[roleApply.RoleId]; !ok {
			return fmt.Errorf("apply %s role administrator not you", roleApply.Id)
		}

		// 进行处理
		if status, ok := statusMap[roleApply.Id]; ok {
			// 审批
			updateRoleApplys = append(updateRoleApplys, &model.RoleApplyEntity{
				Id:          roleApply.Id,
				UpdatedBy:   curUser,
				UpdatedTime: now,
				Status:      status,
			})
			if status != model.RoleApplyStatusApprove {
				continue
			}
			// 插用户
			if _, ok := insertUsersMap[roleApply.CreatedBy]; !ok {
				user, err := db.UserRepositoryInstance.FindNotDeletedUserByUsername(roleApply.CreatedBy)
				if err != nil {
					return err
				}
				if user == nil {
					user = &model.SysUserEntity{
						Id:          utils.Uuid(),
						CreatedBy:   curUser,
						UpdatedBy:   curUser,
						CreatedTime: now,
						UpdatedTime: now,
						Username:    roleApply.CreatedBy,
						EmailAddr:   roleApply.EmailAddr,
						IsActive:    true,
						AuthSource:  constant.AuthSourceUm,
						AuthContext: umAuthCtx,
					}
					insertUsers = append(insertUsers, user)
				}
				insertUsersMap[roleApply.CreatedBy] = user.Id
			}
			// 插用户角色关系
			userRolesId := fmt.Sprintf("%s_%s", roleApply.CreatedBy, roleApply.RoleId)
			if _, ok := insertUserRolesMap[userRolesId]; !ok {
				insertUserRolesMap[userRolesId] = nil
				if userId, ok := insertUsersMap[roleApply.CreatedBy]; ok {
					userRole, err := db.UserRoleRsRepositoryInstance.FindOneByUserIdAndRoleId(userId, roleApply.RoleId, nil)
					if err != nil {
						return err
					}
					if userRole == nil {
						role, ok := cachedRolesMap[roleApply.RoleId]
						if !ok {
							role, err = db.RoleRepositoryInstance.FindNotDeletedRolesById(roleApply.RoleId)
							if err != nil {
								return err
							}
							cachedRolesMap[roleApply.RoleId] = role
						}
						userRole = &model.UserRoleRsEntity{
							Id:          utils.Uuid(),
							CreatedBy:   curUser,
							UpdatedBy:   curUser,
							CreatedTime: now,
							UpdatedTime: now,
							Active:      true,
							UserId:      userId,
							Username:    roleApply.CreatedBy,
							RoleId:      roleApply.RoleId,
							RoleName:    role.Name,
							ExpireTime:  roleApply.ExpireTime,
							RoleApply:   roleApply.Id,
						}
						insertUserRoles = append(insertUserRoles, userRole)
					}
				}
			}
		}
	}

	// 执行db操作
	if len(updateRoleApplys) == 0 && len(insertUsers) == 0 && len(insertUserRoles) == 0 {
		return nil
	}
	_, err = db.Engine.Transaction(func(session *xorm.Session) (interface{}, error) {
		for _, roleApply := range updateRoleApplys {
			if _, err := session.Update(roleApply, &model.RoleApplyEntity{Id: roleApply.Id}); err != nil {
				return nil, err
			}
		}
		if len(insertUsers) > 0 {
			if _, err := session.Insert(insertUsers); err != nil {
				return nil, err
			}
		}
		if len(insertUserRoles) > 0 {
			if _, err := session.Insert(insertUserRoles); err != nil {
				return nil, err
			}
		}
		return nil, nil
	})
	return err
}

func (UserManagementService) DeleteRoleApply(applyId string) error {
	_, err := db.Engine.Exec("delete from auth_sys_role_apply where id = ?", applyId)
	return err
}
