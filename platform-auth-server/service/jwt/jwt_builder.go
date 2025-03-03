package jwt

import (
	"errors"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/utils"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/golang-jwt/jwt"
	"go.uber.org/zap"
	"strconv"
	"time"
)

/*var (
	jwtPrivateKey *rsa.PrivateKey
	jwtPublicKey  *rsa.PublicKey
)
*/
/*func InitKey() error {
	var err error
	var privateKeyBytes, pubKeyBytes []byte

	log.Debug(nil, log.LOGGER_APP,"loading auth jwt private key from file:" + model.Config.Auth.JwtPrivateKeyPath)
	privateKeyBytes, err = ioutil.ReadFile(model.Config.Auth.JwtPrivateKeyPath)
	if err != nil {
		return err
	}

	jwtPrivateKey, err = jwt.ParseRSAPrivateKeyFromPEM(privateKeyBytes)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "Failed to init auth private key: ", zap.Error(err))
		return errors.New("failed to init auth private key")
	}
	log.Info(nil, log.LOGGER_APP, "loaded jwt private key successfully")

	log.Debug(nil, log.LOGGER_APP,"loading auth jwt public key from file:" + model.Config.Auth.JwtPublicKeyPath)
	pubKeyBytes, err = ioutil.ReadFile(model.Config.Auth.JwtPublicKeyPath)
	if err != nil {
		return err
	}

	jwtPublicKey, err = jwt.ParseRSAPublicKeyFromPEM(pubKeyBytes)
	if err != nil {
		log.Error(nil, log.LOGGER_APP, "Failed to init auth public key:", zap.Error(err))
		return errors.New("failed to init auth public key")
	}
	log.Info(nil, log.LOGGER_APP, "loaded jwt public key successfully")

	return nil
}
*/
func BuildAccessToken(loginId string, authorities []string, clientType string, expireTime time.Time) (*model.JwtTokenDto, error) {
	if model.Config.Auth.SigningKeyBytes == nil {
		log.Error(nil, log.LOGGER_APP, "jwt key is invalid")
		return nil, errors.New("failed to build refresh token")
	}
	//authoritiesBytes, _ := json.Marshal(authorities)
	issueAt := time.Now().UTC().Unix()
	exp := expireTime.UTC().Unix()
	//exp := time.Now().Add(time.Minute * time.Duration(model.Config.Auth.AccessTokenMins)).UTC().Unix()
	token := jwt.NewWithClaims(jwt.SigningMethodHS512, model.AuthClaims{
		Subject:    loginId,
		IssuedAt:   issueAt,
		ExpiresAt:  exp,
		Type:       constant.TypeAccessToken,
		ClientType: clientType,
		//Roles:      roles,
		//Authority: string(authoritiesBytes),
		Authority: utils.BuildArrayString(authorities),
	})
	if tokenString, err := token.SignedString(model.Config.Auth.SigningKeyBytes); err == nil {
		return &model.JwtTokenDto{
			Expiration: strconv.Itoa(int(exp)),
			Token:      tokenString,
			TokenType:  constant.TypeAccessToken,
		}, nil
	} else {
		log.Error(nil, log.LOGGER_APP, "Failed to build access token", zap.Error(err))
		return nil, errors.New("failed to build access token")
	}
}

// func buildRefreshToken(loginId, clientType string) (*model.JwtTokenDto, error) {
// 	if model.Config.Auth.SigningKeyBytes == nil {
// 		log.Error(nil, log.LOGGER_APP, "jwt key is invalid")
// 		return nil, errors.New("failed to build refresh token")
// 	}
// 	issueAt := time.Now().UTC().Unix()
// 	exp := time.Now().Add(time.Minute * time.Duration(model.Config.Auth.RefreshTokenMins)).UTC().Unix()

// 	token := jwt.NewWithClaims(jwt.SigningMethodHS512, model.AuthClaims{
// 		Subject:    loginId,
// 		IssuedAt:   issueAt,
// 		ExpiresAt:  exp,
// 		Type:       constant.TypeAccessToken,
// 		ClientType: clientType,
// 	})
// 	if tokenString, err := token.SignedString(model.Config.Auth.SigningKeyBytes); err == nil {
// 		return &model.JwtTokenDto{
// 			Expiration: strconv.Itoa(int(exp)),
// 			Token:      tokenString,
// 			TokenType:  constant.TypeAccessToken,
// 		}, nil
// 	} else {
// 		log.Error(nil, log.LOGGER_APP, "Failed to build refresh token", zap.Error(err))
// 		return nil, errors.New("failed to build refresh token")
// 	}
// }
