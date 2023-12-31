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
	Tokens []Jwt `json:"tokens"`
	//ProductCodes []string `json:"productCodes"`
}

type Jwt struct {
	Expiration int64 `json:"expiration,omitempty"`

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
