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
	"golang.org/x/crypto/bcrypt"
	"time"
)

const DefPasswordLength = 6

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

	ranPassword := PasswordGeneratorInstance.RandomPassword(DefPasswordLength)

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

	return convertToSimpleLocalUserDto(user, false), nil

}

func convertToSimpleLocalUserDto(user *model.SysUserEntity, roleAdministrator bool) *model.SimpleLocalUserDto {
	return &model.SimpleLocalUserDto{
		ID:                user.Id,
		Username:          user.Username,
		Password:          "",
		Department:        user.Department,
		EmailAddr:         user.EmailAddr,
		Title:             user.Title,
		EnglishName:       user.EnglishName,
		NativeName:        user.LocalName,
		CellPhoneNo:       user.CellPhoneNo,
		OfficeTelNo:       user.OfficeTelNo,
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
		// 角色配置当前用户为角色管理员,不允许删除
		if userRole.AdminFlag {
			err = exterror.Catch(exterror.New().AuthServer3027Error.WithParam(userRole.UserId), nil)
			session.Rollback()
			return err
		}

		userRole.Deleted = true
		userRole.UpdatedBy = curUser
		userRole.UpdatedTime = time.Now()
		if affected, err := session.ID(userRole.Id).UseBool().Update(userRole); affected == 0 || err != nil {
			if err != nil {
				log.Logger.Error(fmt.Sprintf("failed to update userRoleRs:%v", userRole.Id), log.Error(err))
			}
			session.Rollback()
			return errors.New(fmt.Sprintf("failed to update userRoleRs:%v", userRole.Id))
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
	remainUserRoles := make([]*model.UserRoleRsEntity, 0)

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
		remainUserRoles = append(remainUserRoles, foundUserRoles...)
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
			log.Logger.Info(fmt.Sprintf("such user userRole configuration already exist,userId=%s,roleId=%a", userId, role.Id))
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
			return errors.New(fmt.Sprintf("failed to update userRole:%s", userRole.Id))
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

func (UserManagementService) GetLocalRolesByUsername(username string) ([]*model.SimpleLocalRoleDto, error) {
	roleDtos := make([]*model.SimpleLocalRoleDto, 0)
	if len(username) == 0 {
		return nil, exterror.Catch(exterror.New().AuthServer3020Error, nil)
	}
	user, err := db.UserRepositoryInstance.FindNotDeletedUserByUsername(username)
	if err != nil {
		log.Logger.Error("failed to find not deleted user by username", log.String("username", username),
			log.Error(err))
		return nil, err
	}

	if user == nil {
		return roleDtos, nil
	}

	userRoles, err := db.UserRoleRsRepositoryInstance.FindAllByUserId(user.Id)
	if err != nil {
		log.Logger.Error("failed to find UserRoleRs", log.String("userId", user.Id), log.Error(err))
		return nil, err
	}

	if len(userRoles) == 0 {
		return roleDtos, nil
	}

	for _, userRole := range userRoles {
		role := &model.SysRoleEntity{}
		found, err := db.Engine.ID(userRole.RoleId).Get(role)
		if err != nil {
			log.Logger.Error("failed to get role", log.String("roleId", userRole.RoleId),
				log.Error(err))
			return nil, err
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
			ID:          role.Id,
			Name:        role.Name,
			DisplayName: role.DisplayName,
			Email:       role.EmailAddress,
			Status:      role.GetRoleDeletedStatus(),
		}

		roleDtos = append(roleDtos, roleDto)
	}

	return roleDtos, nil
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

		userDto := convertToSimpleLocalUserDto(user, userRole.AdminFlag)
		result = append(result, userDto)
	}

	return result, nil
}

func (UserManagementService) RetireveLocalUserByUserid(userId string) (*model.SimpleLocalUserDto, error) {
	user := &model.SysUserEntity{}

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
		log.Logger.Debug(fmt.Sprintf("Such user with ID {} has already been deleted.", userId))
		return nil, exterror.Catch(exterror.New().AuthServer3024Error.WithParam(userId), nil)
	}

	return convertToSimpleLocalUserDto(user, false), nil
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
	return convertToSimpleLocalUserDto(user, false), nil
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

		log.Logger.Info(fmt.Sprintf("such username {} to create has already existed.", userDto.Username))
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
	return convertToSimpleLocalUserDto(userEntity, false), nil
}

func validateSimpleLocalUserDto(userDto *model.SimpleLocalUserDto) error {

	if len(userDto.Username) == 0 {
		return exterror.Catch(exterror.New().AuthServer3025Error, nil)
	}

	authSource := constant.AuthSourceLocal
	if len(userDto.AuthSource) > 0 {
		authSource = userDto.AuthSource
	}

	if utils.EqualsIgnoreCase(constant.AuthSourceLocal, authSource) && len(userDto.Password) == 0 {
		return exterror.Catch(exterror.New().AuthServer3026Error, nil)
	}
	return nil
}

func buildSysUserEntity(dto *model.SimpleLocalUserDto, curUser string) (*model.SysUserEntity, error) {
	now := time.Now()
	encodePassword(dto.Password)
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
		userDto := convertToSimpleLocalUserDto(user, false)

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

	if userRoles != nil {
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
				return errors.New(fmt.Sprintf("failed to update userRoleRs:%v", userRole.Id))
			}
		}
	}
	session.Commit()
	return nil
}
