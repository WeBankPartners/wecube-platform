package jwt

import (
	"crypto/rsa"
	"encoding/json"
	"errors"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"github.com/golang-jwt/jwt"
	"io/ioutil"
	"strconv"
	"time"
)

var (
	jwtPrivateKey *rsa.PrivateKey
	jwtPublicKey  *rsa.PublicKey
)

func InitKey() error {
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

func BuildAccessToken(loginId string, authorities []string, clientType string, expireTime time.Time) (*model.JwtTokenDto, error) {
	if jwtPrivateKey == nil {
		log.Logger.Error("jwt private key is invalid")
		return nil, errors.New("failed to build refresh token")
	}

	authoritiesBytes, _ := json.Marshal(authorities)
	issueAt := time.Now().UTC().Unix()
	exp := expireTime.UTC().Unix()
	//exp := time.Now().Add(time.Minute * time.Duration(model.Config.Auth.AccessTokenMins)).UTC().Unix()
	token := jwt.NewWithClaims(jwt.SigningMethodHS512, model.AuthClaims{
		Subject:    loginId,
		IssuedAt:   issueAt,
		ExpiresAt:  exp,
		Type:       model.TypeAccessToken,
		ClientType: clientType,
		//Roles:      roles,
		Authority: string(authoritiesBytes),
	})
	if tokenString, err := token.SignedString(jwtPrivateKey); err == nil {
		return &model.JwtTokenDto{
			Expiration: strconv.Itoa(int(exp)),
			Token:      tokenString,
			TokenType:  model.TypeAccessToken,
		}, nil
	} else {
		log.Logger.Error("Failed to build access token", log.Error(err))
		return nil, errors.New("failed to build access token")
	}
}

func buildRefreshToken(loginId, clientType string) (*model.JwtTokenDto, error) {
	if jwtPrivateKey == nil {
		log.Logger.Error("jwt private key is invalid")
		return nil, errors.New("failed to build refresh token")
	}

	issueAt := time.Now().UTC().Unix()
	exp := time.Now().Add(time.Hour * time.Duration(model.Config.Auth.RefreshTokenHours)).UTC().Unix()
	token := jwt.NewWithClaims(jwt.SigningMethodHS512, model.AuthClaims{
		Subject:    loginId,
		IssuedAt:   issueAt,
		ExpiresAt:  exp,
		Type:       model.TypeAccessToken,
		ClientType: clientType,
	})
	if tokenString, err := token.SignedString(jwtPrivateKey); err == nil {
		return &model.JwtTokenDto{
			Expiration: strconv.Itoa(int(exp)),
			Token:      tokenString,
			TokenType:  model.TypeAccessToken,
		}, nil
	} else {
		log.Logger.Error("Failed to build refresh token", log.Error(err))
		return nil, errors.New("failed to build refresh token")
	}
}
