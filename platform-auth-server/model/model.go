package model

type SysSubSystemInfo struct {
	ID          string
	Name        string
	SystemCode  string
	APIKey      string
	PubAPIKey   string
	Active      bool
	Blocked     bool
	Authorities []string //roles
}

type SubSystemAuthenticationToken struct {
	Principal   string
	Credentials string
	Nonce       string
	Authorities []string
}

type AuthenticatedUser struct {
	Username string
	//Token              string
	GrantedAuthorities []string
}

type AsymmetricKeyPair struct {
	PrivateKey string
	PublicKey  string
}

type UmCredential struct {
	LoginId   string `json:"loginId"`
	UserToken string `json:"userToken"`
	PinSign   string `json:"pinSign"`
}

type UmPermissionUpload struct {
	SystemId    string `json:"systemId"`
	Address     string `json:"address"`
	CronExpress string `json:"cron_express"`
}

type AuthenticationResponse struct {
	UserId string `json:"userId"`
	//Auth         []AggAuth `json:"auth"`
	NeedRegister bool   `json:"needRegister"`
	Tokens       []*Jwt `json:"tokens"`
	//ProductCodes []string `json:"productCodes"`
}

type Jwt struct {
	Expiration string `json:"expiration,omitempty"`

	Token string `json:"token,omitempty"`

	TokenType string `json:"tokenType,omitempty"`
}

type SysUser struct {
	Username             string
	Password             string
	CompositeAuthorities []*CompositeAuthority
	AuthSource           string
	AuthContext          string
}

type CompositeAuthority struct {
	AuthorityType string
	Authority     string
}

type UmAuthContext struct {
	Protocol string
	Host     string
	Port     int
	Appid    string
	Appname  string
}

type RoleApplyParam struct {
	UserName   string   `json:"userName"`
	EmailAddr  string   `json:"emailAddr"`
	RoleIds    []string `json:"roleIds"`
	ExpireTime string   `json:"expireTime"` //角色过期时间,""表示永久生效
}

type ListRoleApplyResponse struct {
	PageInfo *PageInfo          `json:"pageInfo"` // 分页信息
	Contents []*RoleApplyDto    `json:"contents"` // 列表内容
	Entities []*RoleApplyEntity `json:"-"`        // 列表内容
}

type PlatSystemVariables struct {
	Id           string `json:"id"`           // 唯一标识
	PackageName  string `json:"packageName"`  // 包名
	Name         string `json:"name"`         // 变量名
	Value        string `json:"value"`        // 变量值
	DefaultValue string `json:"defaultValue"` // 默认值
	Scope        string `json:"scope"`        // 作用范围
	Source       string `json:"source"`       // 来源
	Status       string `json:"status"`       // 状态 -> active | inactive
}

type PlatSystemVariablesListPageData struct {
	PageInfo *PageInfo              `json:"pageInfo"` // 分页信息
	Contents []*PlatSystemVariables `json:"contents"` // 列表内容
}
