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
	// 创建一个MD5哈希对象
	hash := md5.New()

	// 计算MD5摘要
	hash.Write([]byte(seed + additionalSalt))
	digest := hash.Sum(nil)

	// 将摘要转换为十六进制字符串表示
	md5Hex := hex.EncodeToString(digest)
	key := fmt.Sprintf("%16s", md5Hex[0:15])
	return []byte(key)
}

func EncryptWithAesECB(password, seed, additionalSalt string) string {
	key := generateKeyFromSeedAndSalt(seed, additionalSalt)
	return encrypt([]byte(password), key)
}

// pkcs5Padding pkcs5Padding填充密码
func pkcs5Padding(src []byte, blockSize int) []byte {
	padding := blockSize - (len(src) % blockSize)
	padText := bytes.Repeat([]byte{byte(padding)}, padding)
	return append(src, padText...)
}

func encrypt(plaintext []byte, key []byte) string {
	block, err := aes.NewCipher(key) // 创建新的cipher对象
	if err != nil {
		panic("Error creating cipher block")
	}
	plaintext = pkcs5Padding(plaintext, aes.BlockSize)
	encryptedData := make([]byte, len(plaintext))
	for i := 0; i < len(plaintext); i += aes.BlockSize {
		block.Encrypt(encryptedData[i:], plaintext[i:]) // 进行加密操作
	}
	return base64.StdEncoding.EncodeToString(encryptedData) // 返回Base64编码后的结果
}

func DecryptWithAesECB(password, seed, additionalSalt string) string {
	key := generateKeyFromSeedAndSalt(seed, additionalSalt)
	return string(decrypt(password, key))
}

func decrypt(ciphertext string, key []byte) []byte {
	decodedData, _ := base64.StdEncoding.DecodeString(ciphertext) // Base64解码

	block, err := aes.NewCipher(key) // 创建新的cipher对象
	if err != nil {
		panic("Error creating cipher block")
	}

	decryptedData := make([]byte, len(decodedData))
	for i := 0; i < len(decodedData); i += aes.BlockSize {
		block.Decrypt(decryptedData[i:], decodedData[i:]) // 进行解密操作
	}
	unPaddedData := pkcs5UnPadding(decryptedData) // 去除填充内容
	return unPaddedData
}

func pkcs5UnPadding(src []byte) []byte {
	length := len(src)
	unPaddedData := int(src[length-1])
	if unPaddedData < length {
		return src[:(length - unPaddedData)]
	} else {
		return src
	}
}
