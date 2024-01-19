package tools

import (
	"bytes"
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/base64"
	"fmt"
)

func paddingPkcs5(origData []byte, blockSize int) []byte {
	n := blockSize - len(origData)%blockSize
	padded := bytes.Repeat([]byte{byte(n)}, n)
	return append(origData, padded...)
}

func unPaddingPkcs5(origData []byte, blockSize int) []byte {
	length := len(origData)
	padLength := int(origData[length-1])
	if padLength <= blockSize {
		origData = origData[:(length - padLength)]
	}
	return origData
}

// len 16 key(means AES-128) + key => len 16 iv
func AESCBCEncode(key string, content []byte) (result string, err error) {
	if len(key) != 32 {
		return "", fmt.Errorf("aes key length should be 32,not %d", len(key))
	}
	iv := key[16:]
	key = key[:16]
	block, _ := aes.NewCipher([]byte(key))
	blockMode := cipher.NewCBCEncrypter(block, []byte(iv))
	padContent := paddingPkcs5(content, blockMode.BlockSize())
	encryptData := make([]byte, len(padContent))
	blockMode.CryptBlocks(encryptData, padContent)
	result = base64.StdEncoding.EncodeToString(encryptData)
	return
}

// len 16 key(means AES-128) + key => len 16 iv
func AESCBCDecode(key, content string) (result []byte, err error) {
	if len(key) != 32 {
		return nil, fmt.Errorf("aes key length should be 32,not %d", len(key))
	}
	iv := key[16:]
	key = key[:16]
	b64DecodeData, decodeBase64Err := base64.StdEncoding.DecodeString(content)
	if decodeBase64Err != nil {
		err = fmt.Errorf("aes decode fail,base64 decode content error: %s", decodeBase64Err.Error())
		return
	}
	block, _ := aes.NewCipher([]byte(key))
	blockMode := cipher.NewCBCDecrypter(block, []byte(iv))
	result = make([]byte, len(b64DecodeData))
	blockMode.CryptBlocks(result, b64DecodeData)
	result = unPaddingPkcs5(result, blockMode.BlockSize())
	return
}

func RSAExtractPubKey(rsaPriPemContent string) (string, error) {
	rsaPemBytes, err := base64.StdEncoding.DecodeString(rsaPriPemContent)
	if err != nil {
		err = fmt.Errorf("rsa pem content decode base64 fail,%s", err.Error())
		return "", err
	}
	privateKeyInterface, err := x509.ParsePKCS8PrivateKey(rsaPemBytes)
	if err != nil {
		err = fmt.Errorf("parse private key fail,%s", err.Error())
		return "", err
	}
	privateKey := privateKeyInterface.(*rsa.PrivateKey)
	// 提取公钥
	publicKey := &privateKey.PublicKey

	// 将公钥转换为DER编码的PEM格式
	pubKeyBytes, err := x509.MarshalPKIXPublicKey(publicKey)
	if err != nil {
		return "", err
	}
	return base64.StdEncoding.EncodeToString(pubKeyBytes), nil
}

func RSAEncrypt(inputData []byte, rsaPemContent string) (string, error) {
	rsaPemBytes, err := base64.StdEncoding.DecodeString(rsaPemContent)
	if err != nil {
		err = fmt.Errorf("rsa pem content decode base64 fail,%s", err.Error())
		return "", err
	}
	publicKeyInf, err := x509.ParsePKIXPublicKey(rsaPemBytes)
	if err != nil {
		err = fmt.Errorf("parse public key fail,%s ", err.Error())
		return "", err
	}
	publicKey := publicKeyInf.(*rsa.PublicKey)
	encryptBytes, err := rsa.EncryptPKCS1v15(rand.Reader, publicKey, inputData)
	if err != nil {
		err = fmt.Errorf("decode fail,%s ", err.Error())
		return "", err
	}
	return base64.StdEncoding.EncodeToString(encryptBytes), nil
}

func RSADecrypt(inputString, rsaPemContent string) (string, error) {
	result := inputString
	inputBytes, err := base64.StdEncoding.DecodeString(inputString)
	if err != nil {
		err = fmt.Errorf("input string format to base64 fail,%s", err.Error())
		return inputString, err
	}
	rsaPemBytes, err := base64.StdEncoding.DecodeString(rsaPemContent)
	if err != nil {
		err = fmt.Errorf("rsa pem content decode base64 fail,%s", err.Error())
		return inputString, err
	}
	privateKeyInterface, err := x509.ParsePKCS8PrivateKey(rsaPemBytes)
	if err != nil {
		err = fmt.Errorf("parse private key fail,%s", err.Error())
		return result, err
	}
	privateKey := privateKeyInterface.(*rsa.PrivateKey)
	decodeBytes, err := rsa.DecryptPKCS1v15(rand.Reader, privateKey, inputBytes)
	if err != nil {
		err = fmt.Errorf("decode fail,%s", err.Error())
		return result, err
	}
	result = string(decodeBytes)
	return result, nil
}
