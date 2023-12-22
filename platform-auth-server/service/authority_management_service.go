package service

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/db"
)

var AuthorityManagementServiceInstance AuthorityManagementService

type AuthorityManagementService struct {
}

func (a AuthorityManagementService) RegisterLocalAuthority(authDto model.SimpleAuthorityDto, curUser string) (*model.SimpleAuthorityDto, error) {
	if len(authDto.Code) == 0 {
		return nil, exterror.Catch(exterror.New().AuthServer3000Error, nil)
	}

	authority, err := db.AuthorityRepositoryInstance.FindNotDeletedOneByCode(authDto.Code)
	if err != nil {
		return nil, err
	}

	if authority != nil {
		log.Logger.Debug(fmt.Sprintf("authority %v to register already exists.", authDto.Code))
		/*		msg := fmt.Sprintf(
				"Authority registering failed,because authority code {%s} already exist.", authDto.Code);
		*/return nil, exterror.Catch(exterror.New().AuthServer3001Error.WithParam(authDto.Code), nil)
	}
	scope := authDto.Scope
	if len(scope) == 0 {
		scope = constant.SCOPE_GLOBAL
	}

	authority = &model.SysAuthorityEntity{
		Active:      true,
		Code:        authDto.Code,
		CreatedBy:   curUser,
		Deleted:     false,
		Description: authDto.Description,
		DisplayName: authDto.DisplayName,
		Scope:       scope,
	}
	db.Engine.Insert(authority)

	return convertToSimpleAuthorityDto(authority), nil

}

func convertToSimpleAuthorityDto(authority *model.SysAuthorityEntity) *model.SimpleAuthorityDto {
	return &model.SimpleAuthorityDto{
		Active:      authority.Active,
		Code:        authority.Code,
		Description: authority.Description,
		DisplayName: authority.DisplayName,
		ID:          authority.Id,
		Scope:       authority.Scope,
	}
}

func (a AuthorityManagementService) RetrieveAllLocalAuthorities() ([]*model.SimpleAuthorityDto, error) {
	result := make([]*model.SimpleAuthorityDto, 0)
	authorities, err := db.AuthorityRepositoryInstance.FindAllNotDeletedAuthorities()
	if err != nil {
		return nil, err
	}
	if len(authorities) == 0 {
		return result, nil
	}

	for _, authority := range authorities {
		dto := convertToSimpleAuthorityDto(authority)
		result = append(result, dto)
	}
	return result, nil
}
