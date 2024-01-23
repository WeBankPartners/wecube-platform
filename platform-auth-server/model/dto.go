package model

const (
	ResponseStatusOk    = "OK"
	ResponseStatusError = "ERROR"
	ResponseMessageOk   = "success"
)

type ResponseWrap struct {
	ErrorCode int         `json:"-"`
	Status    string      `json:"status"`
	Message   string      `json:"message"`
	Data      interface{} `json:"data"`
}

type CredentialDto struct {
	Username   string `json:"username"`
	Password   string `json:"password"`
	ClientType string `json:"clientType"`
	Nonce      string `json:"nonce"`
}

type JwtTokenDto struct {
	Expiration string `json:"expiration"`
	Token      string `json:"token"`
	TokenType  string `json:"tokenType"`
}

type LoggerInfoDto struct {
	Level string `json:"level"`
	Path  string `json:"path"`
}

type RoleAuthoritiesDto struct {
	RoleId      string               `json:"roleId"`
	RoleName    string               `json:"roleName"`
	Authorities []SimpleAuthorityDto `json:"authorities"`
}

type ScopedAuthoritiesClaimDto struct {
	Name                  string                 `json:"name"`
	ScopedAuthoritiesDtos []ScopedAuthoritiesDto `json:"scopedAuthoritiesDtos"`
}

type ScopedAuthoritiesDto struct {
	Scope       string   `json:"scope"`
	Alg         string   `json:"alg"`
	Authorities []string `json:"authorities"`
}

type SimpleAuthorityDto struct {
	ID          string `json:"id"`
	Code        string `json:"code"`
	DisplayName string `json:"displayName"`
	Scope       string `json:"scope"`
	Description string `json:"description"`
	Active      bool   `json:"active"`
}

type SimpleLocalRoleDto struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	DisplayName string `json:"displayName"`
	Email       string `json:"email"`
	Status      string `json:"status"` // Deleted, NotDeleted
}

type SimpleLocalUserDto struct {
	ID          string                `json:"id"`
	Username    string                `json:"username"`
	Password    string                `json:"password"`
	NativeName  string                `json:"nativeName"`
	Title       string                `json:"title"`
	EmailAddr   string                `json:"emailAddr"`
	OfficeTelNo string                `json:"officeTelNo"`
	CellPhoneNo string                `json:"cellPhoneNo"`
	Department  string                `json:"department"`
	EnglishName string                `json:"englishName"`
	Active      bool                  `json:"active"`
	Blocked     bool                  `json:"blocked"`
	Deleted     bool                  `json:"deleted"`
	AuthSource  string                `json:"authSource"`
	AuthContext string                `json:"authContext"`
	Roles       []*SimpleLocalRoleDto `json:"roles"`
}

func (s *SimpleLocalUserDto) AddRoles(roles []*SimpleLocalRoleDto) {
	s.Roles = append(s.Roles, roles...)
}

type SimpleLocalUserPassDto struct {
	Username         string `json:"username"`
	OriginalPassword string `json:"originalPassword"`
	ChangedPassword  string `json:"changedPassword"`
}

type SimpleSubSystemDto struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	SystemCode  string `json:"systemCode"`
	Description string `json:"description"`
	APIKey      string `json:"apikey"`
	Active      bool   `json:"active"`
	Blocked     bool   `json:"blocked"`
	PubKey      string `json:"pubKey"`
}

type SubSystemTokenDto struct {
	SystemCode  string `json:"systemCode"`
	AccessToken string `json:"accessToken"`
	ExpireDate  string `json:"expireDate"`
	CreateDate  string `json:"createDate"` // 20200515
	Nonce       string `json:"nonce"`
}
