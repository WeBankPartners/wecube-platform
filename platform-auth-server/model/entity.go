package model

import "time"

const (
	StatusDeleted    = "Deleted"
	StatusNotDeleted = "NotDeleted"
)

type BaseTraceableEntity struct {
	Id          string    `xorm:"'ID' pk"`
	CreatedBy   string    `xorm:"'CREATED_BY'"`
	UpdatedBy   string    `xorm:"'UPDATED_BY'"`
	CreatedTime time.Time `xorm:"'CREATED_TIME'"`
	UpdatedTime time.Time `xorm:"'UPDATED_TIME'"`
}

type SysApiEntity struct {
	Id          string    `xorm:"'ID' pk"`
	CreatedBy   string    `xorm:"'CREATED_BY'"`
	UpdatedBy   string    `xorm:"'UPDATED_BY'"`
	CreatedTime time.Time `xorm:"'CREATED_TIME'"`
	UpdatedTime time.Time `xorm:"'UPDATED_TIME'"`

	Name       string `xorm:"'NAME'"`
	ApiUrl     string `xorm:"'API_URL'"`
	HttpMethod string `xorm:"'HTTP_METHOD'"`
	SystemId   int64  `xorm:"'SYSTEM_ID'"`
}

func (SysApiEntity) TableName() string {
	return "auth_sys_api"
}

type SysAuthorityEntity struct {
	Id          string    `xorm:"'ID' pk"`
	CreatedBy   string    `xorm:"'CREATED_BY'"`
	UpdatedBy   string    `xorm:"'UPDATED_BY'"`
	CreatedTime time.Time `xorm:"'CREATED_TIME'"`
	UpdatedTime time.Time `xorm:"'UPDATED_TIME'"`

	Active  bool `xorm:"'IS_ACTIVE'"`
	Deleted bool `xorm:"'IS_DELETED'"`

	Code        string `xorm:"'CODE'"`
	DisplayName string `xorm:"'DISPLAY_NAME'"`
	Scope       string `xorm:"'SCOPE'"`
	Description string `xorm:"'DESCRIPTION'"`
}

func (SysAuthorityEntity) TableName() string {
	return "auth_sys_authority"
}

type RoleAuthorityRsEntity struct {
	Id          string    `xorm:"'ID' pk"`
	CreatedBy   string    `xorm:"'CREATED_BY'"`
	UpdatedBy   string    `xorm:"'UPDATED_BY'"`
	CreatedTime time.Time `xorm:"'CREATED_TIME'"`
	UpdatedTime time.Time `xorm:"'UPDATED_TIME'"`

	Active  bool `xorm:"'IS_ACTIVE'"`
	Deleted bool `xorm:"'IS_DELETED'"`

	RoleID        string `xorm:"'ROLE_ID'"`
	RoleName      string `xorm:"'ROLE_NAME'"`
	AuthorityID   string `xorm:"'AUTHORITY_ID'"`
	AuthorityCode string `xorm:"'AUTHORITY_CODE'"`
}

func (RoleAuthorityRsEntity) TableName() string {
	return "auth_sys_role_authority"
}

type SysRoleEntity struct {
	Id          string    `xorm:"'ID' pk"`
	CreatedBy   string    `xorm:"'CREATED_BY'"`
	UpdatedBy   string    `xorm:"'UPDATED_BY'"`
	CreatedTime time.Time `xorm:"'CREATED_TIME'"`
	UpdatedTime time.Time `xorm:"'UPDATED_TIME'"`

	Active  bool `xorm:"'IS_ACTIVE'"`
	Deleted bool `xorm:"'IS_DELETED'"`

	Name          string `xorm:"'NAME'"`
	DisplayName   string `xorm:"'DISPLAY_NAME'"`
	EmailAddress  string `xorm:"'EMAIL_ADDR'"`
	Description   string `xorm:"'DESCRIPTION'"`
	Administrator string `xorm:"'administrator'"`
}

func (SysRoleEntity) TableName() string {
	return "auth_sys_role"
}

func (s *SysRoleEntity) GetRoleDeletedStatus() string {
	if s.Deleted {
		return StatusDeleted
	} else {
		return StatusNotDeleted
	}

}

type SubSystemAuthorityRsEntity struct {
	Id          string    `xorm:"'ID' pk"`
	CreatedBy   string    `xorm:"'CREATED_BY'"`
	UpdatedBy   string    `xorm:"'UPDATED_BY'"`
	CreatedTime time.Time `xorm:"'CREATED_TIME'"`
	UpdatedTime time.Time `xorm:"'UPDATED_TIME'"`

	Active  bool `xorm:"'IS_ACTIVE'"`
	Deleted bool `xorm:"'IS_DELETED'"`

	SubSystemID   string `xorm:"'SUB_SYSTEM_ID'"`
	SubSystemCode string `xorm:"'SUB_SYSTEM_CODE'"`
	AuthorityID   string `xorm:"'AUTHORITY_ID'"`
	AuthorityCode string `xorm:"'AUTHORITY_CODE'"`
}

func (SubSystemAuthorityRsEntity) TableName() string {
	return "auth_sys_sub_system_authority"
}

type SysSubSystemEntity struct {
	Id          string    `xorm:"'ID' pk"`
	CreatedBy   string    `xorm:"'CREATED_BY'"`
	UpdatedBy   string    `xorm:"'UPDATED_BY'"`
	CreatedTime time.Time `xorm:"'CREATED_TIME'"`
	UpdatedTime time.Time `xorm:"'UPDATED_TIME'"`

	Name        string `xorm:"'NAME'"`
	SystemCode  string `xorm:"'SYSTEM_CODE'"`
	ApiKey      string `xorm:"'API_KEY'"`
	PubApiKey   string `xorm:"'PUB_API_KEY'"`
	Description string `xorm:"'DESCRIPTION'"`
	IsActive    bool   `xorm:"'IS_ACTIVE'"`
	IsBlocked   bool   `xorm:"'IS_BLOCKED'"`
}

func (SysSubSystemEntity) TableName() string {
	return "auth_sys_sub_system"
}

type SysUserEntity struct {
	Id          string    `xorm:"'ID' pk"`
	CreatedBy   string    `xorm:"'CREATED_BY'"`
	UpdatedBy   string    `xorm:"'UPDATED_BY'"`
	CreatedTime time.Time `xorm:"'CREATED_TIME'"`
	UpdatedTime time.Time `xorm:"'UPDATED_TIME'"`

	Username    string `xorm:"'USERNAME'"`
	EnglishName string `xorm:"'ENGLISH_NAME'"`
	LocalName   string `xorm:"'LOCAL_NAME'"`
	Department  string `xorm:"'DEPT'"`
	Title       string `xorm:"'TITLE'"`
	EmailAddr   string `xorm:"'EMAIL_ADDR'"`
	OfficeTelNo string `xorm:"'OFFICE_TEL_NO'"`
	CellPhoneNo string `xorm:"'CELL_PHONE_NO'"`
	Password    string `xorm:"'PASSWORD'"`
	IsActive    bool   `xorm:"'IS_ACTIVE'"`
	IsBlocked   bool   `xorm:"'IS_BLOCKED'"`
	IsDeleted   bool   `xorm:"'IS_DELETED'"`
	AuthSource  string `xorm:"'AUTH_SRC'"`
	AuthContext string `xorm:"'AUTH_CTX'"`
}

func (SysUserEntity) TableName() string {
	return "auth_sys_user"
}

type UserRoleRsEntity struct {
	Id          string    `xorm:"'ID' pk"`
	CreatedBy   string    `xorm:"'CREATED_BY'"`
	UpdatedBy   string    `xorm:"'UPDATED_BY'"`
	CreatedTime time.Time `xorm:"'CREATED_TIME'"`
	UpdatedTime time.Time `xorm:"'UPDATED_TIME'"`

	Active    bool   `xorm:"'IS_ACTIVE'"`
	Deleted   bool   `xorm:"'IS_DELETED'"`
	AdminFlag bool   `xorm:"'IS_ADMIN'"`
	UserId    string `xorm:"'USER_ID'"`
	Username  string `xorm:"'USERNAME'"`
	RoleId    string `xorm:"'ROLE_ID'"`
	RoleName  string `xorm:"'ROLE_NAME'"`
}

func (UserRoleRsEntity) TableName() string {
	return "auth_sys_user_role"
}
