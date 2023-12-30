package service

import (
	"crypto/rsa"
	"encoding/base64"
	"errors"
	"fmt"
	"github.com/WeBankPartners/go-common-lib/cipher"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/utils"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/remote/api_um"
	"github.com/golang-jwt/jwt"
	"golang.org/x/crypto/bcrypt"
	"io/ioutil"
	"strings"
	"time"
)

var (
	jwtPrivateKey *rsa.PrivateKey
	jwtPublicKey  *rsa.PublicKey

	ErrRefreshToken = errors.New("failed to refreshToken")
)

const DelimiterSystemCodeAndNonce = ":"

var AuthServiceInstance AuthService

type AuthService struct {
}

func (AuthService) InitKey() error {
	var err error
	var privateKeyBytes, pubKeyBytes []byte

	log.Logger.Debug("loading auth jwt private key from file:" + model.Config.Auth.JwtPrivateKeyPath)
	privateKeyBytes, err = ioutil.ReadFile(model.Config.Auth.JwtPrivateKeyPath)
	if err != nil {
		return err
	}

	jwtPrivateKey, err = jwt.ParseRSAPrivateKeyFromPEM(privateKeyBytes)
	if err != nil {
		log.Logger.Error("Failed to init auth private key: ", log.Error(err))
		return errors.New("failed to init auth private key")
	}
	log.Logger.Info("loaded jwt private key successfully")

	log.Logger.Debug("loading auth jwt public key from file:" + model.Config.Auth.JwtPublicKeyPath)
	pubKeyBytes, err = ioutil.ReadFile(model.Config.Auth.JwtPublicKeyPath)
	if err != nil {
		return err
	}
	jwtPublicKey, err = jwt.ParseRSAPublicKeyFromPEM(pubKeyBytes)
	if err != nil {
		log.Logger.Error("Failed to init auth public key:", log.Error(err))
		return errors.New("failed to init auth public key")
	}
	log.Logger.Info("loaded jwt public key successfully")

	return nil
}

func (AuthService) Login(credential *model.CredentialDto) (*model.AuthenticationResponse, error) {

	if err := validateCredential(credential); err != nil {
		return nil, err
	}

	if credential.ClientType == constant.ClientTypeSubSystem {
		if authResp, err := authenticateSubSystem(credential); err != nil {
			return nil, err
		} else {
			return authResp, nil
		}

	} else {
		if authResp, err := authenticateUser(credential); err != nil {
			return nil, err
		} else {
			return authResp, nil
		}
	}
}

func (AuthService) RefreshToken(refreshToken string) ([]model.Jwt, error) {
	jwtToken, err := jwt.ParseWithClaims(refreshToken, &model.AuthClaims{}, func(token *jwt.Token) (interface{}, error) {
		return jwtPublicKey, nil
	})
	if err != nil {
		log.Logger.Error("Failed to refresh token:", log.Error(err))
		return nil, ErrRefreshToken
	}

	var claim *model.AuthClaims
	var ok bool
	if claim, ok = jwtToken.Claims.(*model.AuthClaims); !ok || !jwtToken.Valid {
		log.Logger.Info("failed to extract claims from jwt token")
		return nil, ErrRefreshToken
	}

	if constant.TypeRefreshToken != claim.Type {
		log.Logger.Warn("token type is not refresh token")
		return nil, ErrRefreshToken
	}
	//	loginId := claim.Subject

	jwts := packJwtTokens(claim.Subject, []string{}, claim.Authorities, "")
	return jwts, nil
}

func validateSubsystemClaimForRefresh(claim *model.AuthClaims) ([]model.Jwt, error) {
	systemCode := claim.Subject
	if isBlank(systemCode) {
		log.Logger.Warn("system code is blank")
		return nil, exterror.NewBadCredentialsError("system code is blank")
	}

	systemInfo, err := SubSystemInfoDataServiceImplInstance.retrieveSysSubSystemInfoWithSystemCode(systemCode)
	if err != nil {
		log.Logger.Error("failed to retrieve sub system info", log.String("systemCode", systemCode), log.Error(err))
		return nil, err
	}

	if systemInfo == nil {
		log.Logger.Error(fmt.Sprintf("such sub system {} is not available.", systemCode))
		return nil, errors.New("such sub system is not available.")
	}

	jwts := packJwtTokens(systemCode, []string{}, systemInfo.Authorities, systemCode)
	return jwts, nil
}

func validateCredential(c *model.CredentialDto) error {
	if c == nil {
		return exterror.NewBadCredentialsError("credentials is empty.")
	}

	if isBlank(c.Username) || len(c.Password) == 0 {
		return exterror.NewBadCredentialsError("credentials is blank.")
	}
	return nil
}

func isBlank(s string) bool {
	return strings.TrimSpace(s) == ""
}

func retrieveSubSystemInfo(systemCode string) (*model.SysSubSystemInfo, error) {
	subSystemInfo, err := SubSystemInfoDataServiceImplInstance.retrieveSysSubSystemInfoWithSystemCode(systemCode)
	if err != nil {
		return nil, err
	}

	if subSystemInfo == nil {
		return nil, fmt.Errorf("%s does not exist", systemCode)
		//throw new UsernameNotFoundException(errMsg);
	}

	return subSystemInfo, nil
}
func authenticateSubSystem(credential *model.CredentialDto) (*model.AuthenticationResponse, error) {
	systemCode := credential.Username
	password := credential.Password
	nonce := credential.Nonce

	subSystemInfo, err := retrieveSubSystemInfo(systemCode)
	if err != nil {
		return nil, err
	}

	subSystemPublicKey := subSystemInfo.PubAPIKey

	if isBlank(subSystemPublicKey) {
		log.Logger.Warn(fmt.Sprintf("sub system public key is blank for system code:%v", systemCode))
		return nil, exterror.NewBadCredentialsError("Bad credential and failed to decrypt password.")
	}

	var encryptedPwd []byte
	if encryptedPwd, err = base64.StdEncoding.DecodeString(password); err != nil {
		log.Logger.Warn("base64 decode password error", log.Error(err))
		return nil, err
	}

	decryptedPassword, err := cipher.RSADecryptByPublic(encryptedPwd, []byte(subSystemPublicKey))
	if err != nil {
		log.Logger.Warn("failed to decrypt by public", log.Error(err))
		return nil, err
	}

	decryptedPasswordParts := strings.Split(string(decryptedPassword), DelimiterSystemCodeAndNonce)
	if len(decryptedPasswordParts) < 2 || (systemCode != decryptedPasswordParts[0]) || nonce != decryptedPasswordParts[1] {
		return nil, exterror.NewBadCredentialsError("Bad credential")
	}

	authResp, err := createAuthenticationResponse(credential, subSystemInfo.Authorities)

	return authResp, err
}

func authenticateUser(credential *model.CredentialDto) (*model.AuthenticationResponse, error) {
	username := credential.Username
	if isBlank(username) {
		log.Logger.Debug("blank user name")
		return nil, exterror.NewBadCredentialsError("Bad credential:blank username.")
	}

	user, err := LocalUserServiceInstance.loadUserByUsername(username)
	if err != nil {
		return nil, err
	}

	if user == nil {
		log.Logger.Debug("User does not exist", log.String("username", username))
		return nil, exterror.NewBadCredentialsError("Bad credential.")
	}

	if err := additionalAuthenticationChecks(user, credential); err != nil {
		log.Logger.Warn("failed to authenticate", log.String("username", user.Username), log.String("authSource", user.AuthSource),
			log.Error(err))
		return nil, err
	}

	authorities := make([]string, 0)
	for _, authority := range user.CompositeAuthorities {
		authorities = append(authorities, authority.Authority)
	}

	authResp, err := createAuthenticationResponse(credential, authorities)
	return authResp, err
}

func packJwtTokens(loginId string, roles []string, authorities []string, userName string) []model.Jwt {
	jwts := make([]model.Jwt, 2)
	if accessToken, exp, err := buildAccessToken(loginId, roles, authorities, userName); err == nil {
		jwts[0] = model.Jwt{Expiration: exp, Token: accessToken, TokenType: constant.TypeAccessToken}
	}
	if refreshToken, exp, err := buildRefreshToken(loginId, userName); err == nil {
		jwts[1] = model.Jwt{Expiration: exp, Token: refreshToken, TokenType: constant.TypeRefreshToken}
	}

	return jwts
}

func buildAccessToken(loginId string, roles []string, authorities []string, userName string) (string, int64, error) {
	if jwtPrivateKey == nil {
		log.Logger.Error("jwt private key is invalid")
		return "", 0, errors.New("failed to build refresh token")
	}

	issueAt := time.Now().UTC().Unix()
	exp := time.Now().Add(time.Minute * time.Duration(model.Config.Auth.AccessTokenMins)).UTC().Unix()
	token := jwt.NewWithClaims(jwt.SigningMethodRS512, model.AuthClaims{
		Subject:     loginId,
		IssuedAt:    issueAt,
		ExpiresAt:   exp,
		Type:        constant.TypeAccessToken,
		Roles:       roles,
		Authorities: authorities,
		/*		LoginType:   loginType,
				Auth:        aggAuths,
				AdminType:   adminType,
				UserName:    userName,
		*/})
	if tokenString, err := token.SignedString(jwtPrivateKey); err == nil {
		return tokenString, exp, nil
	} else {
		log.Logger.Error("Failed to build access token", log.Error(err))
		return "", 0, errors.New("failed to build access token")
	}

}

func buildRefreshToken(loginId, userName string) (string, int64, error) {
	if jwtPrivateKey == nil {
		log.Logger.Error("jwt private key is invalid")
		return "", 0, errors.New("failed to build refresh token")
	}

	issueAt := time.Now().UTC().Unix()
	exp := time.Now().Add(time.Hour * time.Duration(model.Config.Auth.RefreshTokenHours)).UTC().Unix()
	token := jwt.NewWithClaims(jwt.SigningMethodRS512, model.AuthClaims{
		Subject:   loginId,
		IssuedAt:  issueAt,
		ExpiresAt: exp,
		Type:      constant.TypeRefreshToken,
		/*		LoginType: loginType,
				AdminType: adminType,
				UserName:  userName,
		*/})
	if tokenString, err := token.SignedString(jwtPrivateKey); err == nil {
		return tokenString, exp, nil
	} else {
		log.Logger.Error("Failed to build refresh token", log.Error(err))
		return "", 0, errors.New("failed to build access token")
	}
}
func createAuthenticationResponse(credential *model.CredentialDto, authorities []string) (*model.AuthenticationResponse, error) {

	jwts := packJwtTokens(credential.Username, []string{}, authorities, credential.Username)
	return &model.AuthenticationResponse{
		UserId: credential.Username,
		Tokens: jwts,
	}, nil
}

func additionalAuthenticationChecks(user *model.SysUser, credential *model.CredentialDto) error {
	authSource := user.AuthSource
	if isBlank(authSource) {
		authSource = constant.AuthSourceLocal
	}

	if utils.EqualsIgnoreCase(constant.AuthSourceLocal, authSource) {
		checkAuthentication(user, credential)
		return nil
	}

	if utils.EqualsIgnoreCase(constant.AuthSourceUm, authSource) {
		//umAuthenticationChecker.checkAuthentication(user, authToken)
		result, _, err := api_um.UmAuthenticate(credential)
		if err != nil {
			errMsg := "failed to authenticate with token"
			log.Logger.Error(errMsg)
			rtErr := fmt.Errorf(errMsg)
			return rtErr
		}
		if !result {
			log.Logger.Warn("um authenticate failed")
			return exterror.NewBadCredentialsError("um authenticate failed.")
		}

		return nil
	}

	return exterror.NewBadCredentialsError("Unknown credential type.")
}

func checkAuthentication(user *model.SysUser, credential *model.CredentialDto) error {
	presentedPassword := credential.Password
	if err := bcrypt.CompareHashAndPassword([]byte(presentedPassword), []byte(user.Password)); err != nil {
		return exterror.NewBadCredentialsError("Bad credential:bad password.")
	}

	return nil
}
