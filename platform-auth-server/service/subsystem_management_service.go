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
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/jwt"
	"go.uber.org/zap"
	"strconv"
	"time"
)

const (
	MsgBadCredential = "Bad credential."
)

var SubSystemManagementServiceInstance SubSystemManagementService

type SubSystemManagementService struct {
}

func (SubSystemManagementService) RegisterSubSystemAccessToken(subSystemDto *model.SubSystemTokenDto) (*model.SubSystemTokenDto, error) {
	result := &model.SubSystemTokenDto{
		SystemCode: subSystemDto.SystemCode,
	}

	if !validateSubSystemTokenFields(subSystemDto) {
		return result, nil
	}

	accessToken, err := tryAuthenticateSubSystem(subSystemDto)
	if err != nil {
		return nil, err
	}
	if accessToken == nil {
		return result, nil
	}

	result.AccessToken = accessToken.Token
	result.CreateDate = strconv.Itoa(int(time.Now().UTC().Unix()))
	result.ExpireDate = accessToken.Expiration

	return result, nil
}

func validateSubSystemTokenFields(dto *model.SubSystemTokenDto) bool {
	if dto == nil {
		return false
	}

	if len(dto.SystemCode) == 0 {
		return false
	}

	if len(dto.Nonce) == 0 {
		return false
	}

	if len(dto.CreateDate) == 0 {
		return false
	}

	if len(dto.ExpireDate) == 0 {
		return false
	}

	return true
}

func tryAuthenticateSubSystem(subSystem *model.SubSystemTokenDto) (*model.JwtTokenDto, error) {
	systemCode := subSystem.SystemCode
	subSystemInfo, err := SubSystemInfoDataServiceImplInstance.retrieveSysSubSystemInfoWithSystemCode(systemCode)
	if err != nil {
		return nil, err
	}
	if subSystemInfo == nil {
		msg := fmt.Sprintf("Sub system %s does not exist.", systemCode)
		return nil, exterror.NewBadCredentialsError(msg)
	}

	if subSystemInfo.Blocked {
		return nil, exterror.NewBadCredentialsError(fmt.Sprintf("Sub system %s is blocked.", systemCode))
	}

	if !subSystemInfo.Active {
		return nil, exterror.NewBadCredentialsError(fmt.Sprintf("Sub system %s is inactive.", systemCode))
	}

	jwtToken, err := doAuthenticateSubSystem(subSystem, subSystemInfo)
	return jwtToken, err
}

func doAuthenticateSubSystem(subSystem *model.SubSystemTokenDto, subSystemInfo *model.SysSubSystemInfo) (*model.JwtTokenDto, error) {
	expireTime, err := tryCalculateExpireTime(subSystem)
	if err != nil {
		return nil, err
	}
	auth, err := tryAuthenticate(subSystem, subSystemInfo)
	if err != nil {
		return nil, err
	}

	jwtToken, err := jwt.BuildAccessToken(auth.Principal, auth.Authorities, constant.AuthoritySubsystem, *expireTime)
	if err != nil {
		return nil, err
	}

	return jwtToken, nil
}

func tryCalculateExpireTime(subSystem *model.SubSystemTokenDto) (*time.Time, error) {
	expireDate, err := parseDate(subSystem.ExpireDate)
	return expireDate, err
}

func parseDate(dateString string) (*time.Time, error) {
	layout := "20060102" //yyyyMMdd

	// Parse the input date string into a time object
	parsedTime, err := time.ParseInLocation(layout, dateString, time.Local)
	if err != nil {
		return nil, err
	}
	return &parsedTime, nil
}

func tryAuthenticate(subSystem *model.SubSystemTokenDto, subSystemInfo *model.SysSubSystemInfo) (*model.SubSystemAuthenticationToken, error) {
	currTime := time.Now()
	currTimeStr := formatDate(currTime)
	if currTimeStr != subSystem.CreateDate {
		return nil, exterror.NewBadCredentialsError(MsgBadCredential)
	}

	if len(subSystem.Nonce) != 10 {
		return nil, exterror.NewBadCredentialsError(MsgBadCredential)
	}

	length, err := strconv.Atoi(subSystem.Nonce[:8])
	if err != nil {
		return nil, err
	}
	//int len = Integer.parseInt(subSystem.getNonce().substring(8));
	if length != len(subSystem.SystemCode) {
		return nil, exterror.NewBadCredentialsError(MsgBadCredential)
	}

	return createSuccessAuthentication(subSystemInfo, subSystem), nil
}

func createSuccessAuthentication(retrievedSubSystemInfo *model.SysSubSystemInfo, subSystem *model.SubSystemTokenDto) *model.SubSystemAuthenticationToken {
	returnAuthToken := &model.SubSystemAuthenticationToken{
		Principal:   subSystem.SystemCode,
		Credentials: subSystem.SystemCode,
		Nonce:       subSystem.Nonce,
		Authorities: retrievedSubSystemInfo.Authorities,
	}

	return returnAuthToken

}

func formatDate(dateTime time.Time) string {
	layout := "20060102" // yyyyMMdd

	return dateTime.Format(layout)
}

func validateStrictPermission(currUser *model.AuthenticatedUser) error {

	if currUser == nil {
		return exterror.NewAuthServerError("Lack of permission.")
	}

	userRoles := currUser.GrantedAuthorities
	if len(userRoles) == 0 {
		return exterror.NewAuthServerError("Lack of permission due to empty roles.")
	}

	if !utils.Contains(userRoles, constant.AuthoritySubsystem) {
		return exterror.NewAuthServerError("Lack of permission due to no sub-system authority.")
	}

	if utils.EqualsIgnoreCase(constant.SubSystemNameSysPlatform, currUser.Username) {
		return nil
	}

	log.Info(nil, log.LOGGER_APP, fmt.Sprintf("current username is : %v", currUser.Username))
	return exterror.NewAuthServerError("Lack of permission due to not platform identity.")
}

func (SubSystemManagementService) RegisterSubSystem(subSystemDto *model.SimpleSubSystemDto, currUser *model.AuthenticatedUser) (*model.SimpleSubSystemDto, error) {

	err := validateStrictPermission(currUser)
	if err != nil {
		return nil, err
	}

	if len(subSystemDto.SystemCode) == 0 {
		return nil, exterror.Catch(exterror.New().AuthServer3016Error, nil)
		//throw new AuthServerException("3016", "Registering sub-system errors:system code cannot be blank.");
	}

	subSystem, err := db.SubSystemRepositoryInstance.FindOneBySystemCode(subSystemDto.SystemCode)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to find SubSystem", zap.String("systemCode", subSystemDto.SystemCode),
			zap.Error(err))
		return nil, err
	}

	if subSystem == nil {
		subSystem, err = db.SubSystemRepositoryInstance.FindOneBySystemName(subSystemDto.Name)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to find subSystem by system name", zap.String("subsystem name", subSystemDto.Name),
				zap.Error(err))
			return nil, err
		}
		//subSystem = subSystemRepository.findOneBySystemName(subSystemDto.getName());
	}

	if subSystem != nil {
		log.Debug(nil, log.LOGGER_APP, fmt.Sprintf("such sub-system already exists,system code %s", subSystemDto.SystemCode))
		return convertToSimpleSubSystemDto(subSystem), nil
	}

	log.Info(nil, log.LOGGER_APP, "About to create a new sub system:", log.JsonObj("subSystemDto", subSystemDto))

	keyPair, err := utils.InitAsymmetricKeyPair()
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to init asymmetric key paire", zap.Error(err))
		return nil, err
	}

	subSystem = &model.SysSubSystemEntity{
		Id: utils.Uuid(),
		//TODO
		//CreatedBy:   currUser.Username,
		Description: subSystemDto.Description,
		ApiKey:      keyPair.PrivateKey,
		PubApiKey:   keyPair.PublicKey,
		Name:        subSystemDto.Name,
		SystemCode:  subSystemDto.SystemCode,
		IsActive:    true,
		IsBlocked:   false,
	}

	result, err := db.Engine.Insert(subSystem)
	if err != nil || result == 0 {
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "failed to insert subsystem", log.JsonObj("subsystem", subSystem))
		}
		return nil, errors.New("failed to insert subsystem")
	}

	return convertToSimpleSubSystemDto(subSystem), nil
}

func convertToSimpleSubSystemDto(subSystem *model.SysSubSystemEntity) *model.SimpleSubSystemDto {
	return &model.SimpleSubSystemDto{
		ID:          subSystem.Id,
		Active:      subSystem.IsActive,
		Blocked:     subSystem.IsBlocked,
		Description: subSystem.Description,
		Name:        subSystem.Name,
		SystemCode:  subSystem.SystemCode,
		APIKey:      subSystem.ApiKey,
		PubKey:      subSystem.PubApiKey,
	}
}

func (SubSystemManagementService) RetrieveSubSystemApikey(systemCode string) (*model.SimpleSubSystemDto, error) {
	result := &model.SimpleSubSystemDto{}
	if len(systemCode) == 0 {
		return result, nil
	}

	subSystem, err := db.SubSystemRepositoryInstance.FindOneBySystemCode(systemCode)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to find SubSystem by systemcode", zap.String("systemcode", systemCode),
			zap.Error(err))
		return nil, err
	}

	if subSystem == nil {
		return result, nil
	}

	return &model.SimpleSubSystemDto{
		ID:          subSystem.Id,
		Active:      subSystem.IsActive,
		Blocked:     subSystem.IsBlocked,
		Description: subSystem.Description,
		Name:        subSystem.Name,
		SystemCode:  subSystem.SystemCode,
		APIKey:      subSystem.ApiKey,
	}, nil
}

func (SubSystemManagementService) RetrieveSubSystemByName(name string, currUser *model.AuthenticatedUser) (*model.SimpleSubSystemDto, error) {
	if err := validateStrictPermission(currUser); err != nil {
		return nil, err
	}
	subSystem, err := db.SubSystemRepositoryInstance.FindOneBySystemName(name)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to find Subsystem by system name", zap.String("system name", name),
			zap.Error(err))
		return nil, err
	}

	if subSystem == nil {
		return nil, nil
	}

	return convertToSimpleSubSystemDto(subSystem), nil
}

func (SubSystemManagementService) RetrieveAllSubSystems(currUser *model.AuthenticatedUser) ([]*model.SimpleSubSystemDto, error) {
	if err := validateStrictPermission(currUser); err != nil {
		return nil, err
	}
	subSystems := make([]*model.SysSubSystemEntity, 0)
	if err := db.Engine.Find(&subSystems); err != nil {
		log.Error(nil, log.LOGGER_APP, "failed to find subsystem", zap.Error(err))
		return nil, err
	}

	result := make([]*model.SimpleSubSystemDto, 0)
	if len(subSystems) == 0 {
		return nil, nil
	}

	for _, subSystem := range subSystems {
		d := convertToSimpleSubSystemDto(subSystem)
		result = append(result, d)
	}
	return result, nil
}
