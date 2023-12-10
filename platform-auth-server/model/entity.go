package model

type SysApiEntity struct {
	Name       string `xorm:"'NAME'"`
	ApiUrl     string `xorm:"'API_URL'"`
	HttpMethod string `xorm:"'HTTP_METHOD'"`
	SystemId   int64  `xorm:"'SYSTEM_ID'"`
}

type SysAuthorityEntity struct {
	Code        string `xorm:"'CODE'"`
	DisplayName string `xorm:"'DISPLAY_NAME'"`
	Scope       string `xorm:"'SCOPE'"`
	Description string `xorm:"'DESCRIPTION'"`
}

type RoleAuthorityRsEntity struct {
	RoleID        string `xorm:"'ROLE_ID'"`
	RoleName      string `xorm:"'ROLE_NAME'"`
	AuthorityID   string `xorm:"'AUTHORITY_ID'"`
	AuthorityCode string `xorm:"'AUTHORITY_CODE'"`
}

type SysRoleEntity struct {
	Name        string `xorm:"'NAME'"`
	DisplayName string `xorm:"'DISPLAY_NAME'"`
	EmailAddr   string `xorm:"'EMAIL_ADDR'"`
	Description string `xorm:"'DESCRIPTION'"`
}

type SubSystemAuthorityRsEntity struct {
	SubSystemID   string `xorm:"'SUB_SYSTEM_ID'"`
	SubSystemCode string `xorm:"'SUB_SYSTEM_CODE'"`
	AuthorityID   string `xorm:"'AUTHORITY_ID'"`
	AuthorityCode string `xorm:"'AUTHORITY_CODE'"`
}

type SysSubSystemEntity struct {
	Name        string `xorm:"'NAME'"`
	SystemCode  string `xorm:"'SYSTEM_CODE'"`
	ApiKey      string `xorm:"'API_KEY'"`
	PubApiKey   string `xorm:"'PUB_API_KEY'"`
	Description string `xorm:"'DESCRIPTION'"`
	IsActive    bool   `xorm:"'IS_ACTIVE'"`
	IsBlocked   bool   `xorm:"'IS_BLOCKED'"`
}

type SysUserEntity struct {
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

type UserRoleRsEntity struct {
	UserId   string `xorm:"'USER_ID'"`
	Username string `xorm:"'USERNAME'"`
	RoleId   string `xorm:"'ROLE_ID'"`
	RoleName string `xorm:"'ROLE_NAME'"`
}
