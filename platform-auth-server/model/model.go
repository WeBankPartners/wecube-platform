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
	Username           string
	Token              string
	GrantedAuthorities []string
}

type AsymmetricKeyPair struct {
	PrivateKey string
	PublicKey  string
}
