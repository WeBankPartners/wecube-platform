package utils

import (
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/base64"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
)

func InitAsymmetricKeyPair() (*model.AsymmetricKeyPair, error) {
	key, err := rsa.GenerateKey(rand.Reader, 512)
	if err != nil {
		return nil, err
	}
	pubKeyBytes := x509.MarshalPKCS1PublicKey(&key.PublicKey)
	pubKeyString := base64.StdEncoding.EncodeToString(pubKeyBytes)

	priKeyBytes, err := x509.MarshalPKCS8PrivateKey(key)
	if err != nil {
		return nil, err
	}
	priKeyString := base64.StdEncoding.EncodeToString(priKeyBytes)
	return &model.AsymmetricKeyPair{
		PrivateKey: priKeyString,
		PublicKey:  pubKeyString,
	}, nil

}
