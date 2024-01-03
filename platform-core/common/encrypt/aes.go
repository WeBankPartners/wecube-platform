package encrypt

import (
	"bytes"
	"crypto/aes"
	"crypto/md5"
	"encoding/base64"
	"encoding/hex"
	"fmt"
)

func generateKeyFromSeedAndSalt(seed, additionalSalt string) []byte {
	hash := md5.Sum([]byte(seed + additionalSalt))
	key := fmt.Sprintf("%16s", hex.EncodeToString(hash[:])[0:15])
	return []byte(key)
}

func EncryptWithAesECB(password, seed, additionalSalt string) (string, error) {
	key := generateKeyFromSeedAndSalt(seed, additionalSalt)
	return encrypt([]byte(password), key)
}

func encrypt(plaintext []byte, key []byte) (string, error) {
	block, err := aes.NewCipher(key) // 创建新的cipher对象
	if err != nil {
		return "", fmt.Errorf("Error creating cipher block ")
	}
	// 补齐明文长度为16字节（AES分组大小）的倍数
	paddingLen := len(plaintext) % aes.BlockSize
	if paddingLen > 0 {
		padText := bytes.Repeat([]byte{byte(len(plaintext))}, aes.BlockSize-paddingLen)
		plaintext = append(plaintext, padText...)
	}

	encryptedData := make([]byte, len(plaintext))
	for i := 0; i < len(plaintext); i += aes.BlockSize {
		block.Encrypt(encryptedData[i:], plaintext[i:]) // 进行加密操作
	}

	return base64.StdEncoding.EncodeToString(encryptedData), nil // 返回Base64编码后的结果
}

func DecryptWithAesECB(password, seed, additionalSalt string) (string, error) {
	key := generateKeyFromSeedAndSalt(seed, additionalSalt)
	return decrypt(password, key)
}

func decrypt(ciphertext string, key []byte) (string, error) {
	decodedData, _ := base64.StdEncoding.DecodeString(ciphertext) // Base64解码

	block, err := aes.NewCipher(key) // 创建新的cipher对象
	if err != nil {
		return "", fmt.Errorf("Error creating cipher block ")
	}

	decryptedData := make([]byte, len(decodedData))
	for i := 0; i < len(decodedData); i += aes.BlockSize {
		block.Decrypt(decryptedData[i:], decodedData[i:]) // 进行解密操作
	}
	unpaddedData := unPad(decryptedData) // 去除填充内容
	return string(unpaddedData), nil
}

func unPad(data []byte) []byte {
	length := int(data[len(data)-1])
	return data[:len(data)-length]
}
