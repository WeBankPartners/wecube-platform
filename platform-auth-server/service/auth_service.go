package service

import (
	"bytes"
	"crypto/rsa"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"math/big"
	"strconv"
	"strings"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/utils"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/remote/api_platform"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/service/remote/api_um"
	"github.com/golang-jwt/jwt"
	"golang.org/x/crypto/bcrypt"
)

var (
	/*	jwtPrivateKey *rsa.PrivateKey
		jwtPublicKey  *rsa.PublicKey
	*/
	ErrRefreshToken = errors.New("failed to refreshToken")
)

const DelimiterSystemCodeAndNonce = ":"

var AuthServiceInstance AuthService

type AuthService struct {
}

func (AuthService) InitKey() error {
	signingKey := constant.DefaultJwtSigningKey
	if len(model.Config.Auth.SigningKey) > 0 {
		signingKey = model.Config.Auth.SigningKey
	}
	keyBytes, err := ioutil.ReadAll(base64.NewDecoder(base64.RawStdEncoding, bytes.NewBufferString(signingKey)))
	if err != nil {
		log.Logger.Error("Decode core token fail,base64 decode error", log.Error(err))
		return err
	}
	model.Config.Auth.SigningKeyBytes = keyBytes
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

func (AuthService) RefreshToken(refreshToken string) ([]*model.Jwt, error) {
	jwtToken, err := jwt.ParseWithClaims(refreshToken, &model.AuthClaims{}, func(token *jwt.Token) (interface{}, error) {
		return model.Config.Auth.SigningKeyBytes, nil
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
	authorities := make([]string, 0)
	user, err := LocalUserServiceInstance.loadUserByUsername(claim.Subject)
	if err != nil {
		return nil, err
	}
	if user != nil && len(user.CompositeAuthorities) > 0 {
		for _, authority := range user.CompositeAuthorities {
			authorities = append(authorities, authority.Authority)
		}
	} else {
		json.Unmarshal([]byte(claim.Authority), &authorities)
	}
	jwts := packJwtTokens(claim.Subject, []string{}, authorities, claim.NeedRegister)
	return jwts, nil
}

// func validateSubsystemClaimForRefresh(claim *model.AuthClaims) ([]*model.Jwt, error) {
// 	systemCode := claim.Subject
// 	if isBlank(systemCode) {
// 		log.Logger.Warn("system code is blank")
// 		return nil, exterror.NewBadCredentialsError("system code is blank")
// 	}

// 	systemInfo, err := SubSystemInfoDataServiceImplInstance.retrieveSysSubSystemInfoWithSystemCode(systemCode)
// 	if err != nil {
// 		log.Logger.Error("failed to retrieve sub system info", log.String("systemCode", systemCode), log.Error(err))
// 		return nil, err
// 	}

// 	if systemInfo == nil {
// 		log.Logger.Error(fmt.Sprintf("such sub system %s is not available.", systemCode))
// 		return nil, errors.New("such sub system is not available")
// 	}

// 	jwts := packJwtTokens(systemCode, []string{}, systemInfo.Authorities, claim.NeedRegister)
// 	return jwts, nil
// }

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

	/*	var encryptedPwd []byte
		if encryptedPwd, err = base64.StdEncoding.DecodeString(password); err != nil {
			log.Logger.Warn("base64 decode password error", log.Error(err))
			return nil, err
		}*/

	decryptedPassword, err := RSADecryptByPublic(password, subSystemPublicKey)
	if err != nil {
		log.Logger.Warn("failed to decrypt by public", log.Error(err))
		return nil, err
	}

	decryptedPasswordParts := strings.Split(string(decryptedPassword), DelimiterSystemCodeAndNonce)
	if len(decryptedPasswordParts) < 2 || (systemCode != decryptedPasswordParts[0]) || nonce != decryptedPasswordParts[1] {
		return nil, exterror.NewBadCredentialsError("Bad credential")
	}

	authResp, err := createAuthenticationResponse(credential, subSystemInfo.Authorities, false)

	return authResp, err
}

func RSADecryptByPublic(encryptString, publicKeyContent string) ([]byte, error) {
	encryptBytes, err := base64.StdEncoding.DecodeString(encryptString)
	if err != nil {
		err = fmt.Errorf("EncryptData decode from base64 fail,%s ", err.Error())
		return nil, err
	}

	block, _ := base64.StdEncoding.DecodeString(publicKeyContent)
	if block == nil {
		return nil, fmt.Errorf("public key illegal, base64 decode fail")
	}
	publicKeyInterface, parsePubErr := x509.ParsePKIXPublicKey(block)
	if parsePubErr != nil {
		return nil, fmt.Errorf("x509 parse public key error:%s ", parsePubErr.Error())
	}
	publicKey := publicKeyInterface.(*rsa.PublicKey)
	c := new(big.Int)
	m := new(big.Int)
	m.SetBytes(encryptBytes)
	e := big.NewInt(int64(publicKey.E))
	c.Exp(m, e, publicKey.N)
	out := c.Bytes()
	skip := 0
	for i := 2; i < len(out); i++ {
		if i+1 >= len(out) {
			break
		}
		if out[i] == 0xff && out[i+1] == 0 {
			skip = i + 2
			break
		}
	}
	return out[skip:], nil
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
		// 检查UM用户是否存在
		umExists, err := checkUmUserExists(credential)
		if err != nil {
			return nil, err
		}
		if umExists {
			// UM用户存在，返回需注册的token
			authResp, err := createAuthenticationResponse(credential, []string{}, true)
			return authResp, err
		}
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

	authResp, err := createAuthenticationResponse(credential, authorities, false)
	return authResp, err
}

func packJwtTokens(loginId string, roles []string, authorities []string, needRegister bool) []*model.Jwt {
	jwts := make([]*model.Jwt, 2)
	if accessToken, exp, err := buildAccessToken(loginId, roles, authorities, needRegister); err == nil {
		jwts[0] = &model.Jwt{Expiration: strconv.Itoa(int(exp)), Token: accessToken, TokenType: constant.TypeAccessToken}
	}
	if refreshToken, exp, err := buildRefreshToken(loginId, needRegister); err == nil {
		jwts[1] = &model.Jwt{Expiration: strconv.Itoa(int(exp)), Token: refreshToken, TokenType: constant.TypeRefreshToken}
	}

	return jwts
}

func buildAccessToken(loginId string, roles []string, authorities []string, needRegister bool) (string, int64, error) {
	if model.Config.Auth.SigningKeyBytes == nil {
		log.Logger.Error("jwt key is invalid")
		return "", 0, errors.New("failed to build refresh token")
	}
	issueAt := time.Now().UTC().Unix()
	exp := time.Now().Add(time.Minute * time.Duration(model.Config.Auth.AccessTokenMins)).UTC().Unix()
	token := jwt.NewWithClaims(jwt.SigningMethodHS512, model.AuthClaims{
		Subject:      loginId,
		IssuedAt:     issueAt,
		ExpiresAt:    exp,
		Type:         constant.TypeAccessToken,
		Roles:        roles,
		Authority:    utils.BuildArrayString(authorities),
		NeedRegister: needRegister,
		/*		LoginType:   loginType,
				Auth:        aggAuths,
				AdminType:   adminType,
				UserName:    userName,
		*/})
	if tokenString, err := token.SignedString(model.Config.Auth.SigningKeyBytes); err == nil {
		return tokenString, exp, nil
	} else {
		log.Logger.Error("Failed to build access token", log.Error(err))
		return "", 0, errors.New("failed to build access token")
	}
}

func buildRefreshToken(loginId string, needRegister bool) (string, int64, error) {
	if model.Config.Auth.SigningKeyBytes == nil {
		log.Logger.Error("jwt key is invalid")
		return "", 0, errors.New("failed to build refresh token")
	}

	issueAt := time.Now().UTC().Unix()
	exp := time.Now().Add(time.Minute * time.Duration(model.Config.Auth.RefreshTokenMins)).UTC().Unix()
	token := jwt.NewWithClaims(jwt.SigningMethodHS512, model.AuthClaims{
		Subject:      loginId,
		IssuedAt:     issueAt,
		ExpiresAt:    exp,
		Type:         constant.TypeRefreshToken,
		NeedRegister: needRegister,
		/*		LoginType: loginType,
				AdminType: adminType,
				UserName:  userName,
		*/})
	if tokenString, err := token.SignedString(model.Config.Auth.SigningKeyBytes); err == nil {
		return tokenString, exp, nil
	} else {
		log.Logger.Error("Failed to build refresh token", log.Error(err))
		return "", 0, errors.New("failed to build access token")
	}
}

func createAuthenticationResponse(credential *model.CredentialDto, authorities []string, needRegister bool) (*model.AuthenticationResponse, error) {
	jwts := packJwtTokens(credential.Username, []string{}, authorities, needRegister)
	return &model.AuthenticationResponse{
		UserId:       credential.Username,
		NeedRegister: needRegister,
		Tokens:       jwts,
	}, nil
}

func parseUserAuthContext(authContext string) map[string]string {
	keyValuePairs := strings.Split(authContext, ";")
	kvMap := make(map[string]string)
	for _, keyValuePair := range keyValuePairs {
		keyValue := strings.Split(keyValuePair, "=")
		if len(keyValue) == 2 {
			kvMap[keyValue[0]] = keyValue[1]
		}
	}
	return kvMap
}

func additionalAuthenticationChecks(user *model.SysUser, credential *model.CredentialDto) error {
	authSource := user.AuthSource
	if isBlank(authSource) {
		authSource = constant.AuthSourceLocal
	}

	if utils.EqualsIgnoreCase(constant.AuthSourceLocal, authSource) {
		return checkAuthentication(user, credential)
	}
	authCtxMap := parseUserAuthContext(user.AuthContext)
	if utils.EqualsIgnoreCase(constant.AuthSourceUm, authSource) {
		//umAuthenticationChecker.checkAuthentication(user, authToken)
		result, _, err := api_um.UmAuthenticate(authCtxMap, credential)
		if err != nil {
			errMsg := "failed to authenticate with token"
			log.Logger.Error(errMsg, log.Error(err))
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
	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(presentedPassword)); err != nil {
		log.Logger.Warn("failed to compare hash and password", log.Error(err))
		return exterror.NewBadCredentialsError("Bad credential:bad password.")
	}

	return nil
}

func GetUmAuthContext(username string) (string, error) {
	// 生成token
	accessToken, _, err := buildAccessToken(username, []string{}, []string{}, false)
	if err != nil {
		return "", err
	}
	// 获取 UM_AUTH_CONTEXT 系统参数
	queryParam := &model.QueryRequestParam{
		Filters: []*model.QueryRequestFilterObj{
			{
				Name:     "name",
				Operator: "contains",
				Value:    "UM_AUTH_CONTEXT",
			},
		},
		Paging: true,
		Pageable: &model.PageInfo{
			StartIndex: 0,
			PageSize:   10,
		},
	}
	queryResult, err := api_platform.QuerySystemVariables(accessToken, "", queryParam)
	if err != nil {
		return "", err
	}
	if queryResult == nil || len(queryResult.Contents) == 0 {
		return "", fmt.Errorf("UM_AUTH_CONTEXT not found")
	}
	return queryResult.Contents[0].Value, nil
}

func checkUmUserExists(credential *model.CredentialDto) (bool, error) {
	umAuthCtx, err := GetUmAuthContext(credential.Username)
	if err != nil {
		return false, err
	}
	authCtxMap := parseUserAuthContext(umAuthCtx)
	umExist, _, err := api_um.UmAuthenticate(authCtxMap, credential)
	if err != nil {
		return false, err
	}
	return umExist, nil
}
