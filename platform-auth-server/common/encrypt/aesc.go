package encrypt

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"crypto/sha256"
	"encoding/base64"
	"fmt"
	"io"
	"strings"
)

const AESCPrefix = "{AESC}"

func EncryptWithAESC(password, configPasswordKey string) (string, error) {
	aesKey, err := buildAESCKey(configPasswordKey)
	if err != nil {
		return "", err
	}
	block, err := aes.NewCipher(aesKey)
	if err != nil {
		return "", err
	}
	gcm, err := cipher.NewGCM(block)
	if err != nil {
		return "", err
	}
	nonce := make([]byte, gcm.NonceSize())
	if _, err = io.ReadFull(rand.Reader, nonce); err != nil {
		return "", err
	}
	cipherData := gcm.Seal(nonce, nonce, []byte(password), nil)
	return AESCPrefix + base64.RawURLEncoding.EncodeToString(cipherData), nil
}

func DecryptWithAESC(cipherText, configPasswordKey string) (string, error) {
	if !strings.HasPrefix(cipherText, AESCPrefix) {
		return "", fmt.Errorf("AESC ciphertext prefix invalid")
	}
	rawData, err := base64.RawURLEncoding.DecodeString(strings.TrimPrefix(cipherText, AESCPrefix))
	if err != nil {
		return "", err
	}
	aesKey, err := buildAESCKey(configPasswordKey)
	if err != nil {
		return "", err
	}
	block, err := aes.NewCipher(aesKey)
	if err != nil {
		return "", err
	}
	gcm, err := cipher.NewGCM(block)
	if err != nil {
		return "", err
	}
	if len(rawData) < gcm.NonceSize() {
		return "", fmt.Errorf("AESC ciphertext too short")
	}
	nonce := rawData[:gcm.NonceSize()]
	cipherData := rawData[gcm.NonceSize():]
	plainData, err := gcm.Open(nil, nonce, cipherData, nil)
	if err != nil {
		return "", err
	}
	return string(plainData), nil
}

func buildAESCKey(configPasswordKey string) ([]byte, error) {
	if configPasswordKey == "" {
		return nil, fmt.Errorf("config password key is empty")
	}
	hash := sha256.Sum256([]byte(configPasswordKey))
	return hash[:], nil
}
