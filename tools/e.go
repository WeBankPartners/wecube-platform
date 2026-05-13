package main

import (
	"bytes"
	"crypto/aes"
	"crypto/md5"
	"encoding/base64"
	"encoding/hex"
	"flag"
	"fmt"
	"os"
	"strings"
)

const aesPrefix = "{AES}"

func generateKeyFromSeedAndSalt(seed, additionalSalt string) []byte {
	hash := md5.New()
	hash.Write([]byte(seed + additionalSalt))
	digest := hash.Sum(nil)
	md5Hex := hex.EncodeToString(digest)
	key := fmt.Sprintf("%16s", md5Hex[0:15])
	return []byte(key)
}

func pkcs5Padding(src []byte, blockSize int) []byte {
	padding := blockSize - (len(src) % blockSize)
	padText := bytes.Repeat([]byte{byte(padding)}, padding)
	return append(src, padText...)
}

func pkcs5UnPadding(src []byte) []byte {
	length := len(src)
	unPaddedData := int(src[length-1])
	if unPaddedData < length {
		return src[:(length - unPaddedData)]
	}
	return src
}

func EncryptWithAesECB(password, seed, additionalSalt string) string {
	key := generateKeyFromSeedAndSalt(seed, additionalSalt)
	plaintext := pkcs5Padding([]byte(password), aes.BlockSize)
	block, err := aes.NewCipher(key)
	if err != nil {
		fmt.Fprintln(os.Stderr, "create cipher error:", err)
		os.Exit(1)
	}
	encryptedData := make([]byte, len(plaintext))
	for i := 0; i < len(plaintext); i += aes.BlockSize {
		block.Encrypt(encryptedData[i:], plaintext[i:])
	}
	return base64.StdEncoding.EncodeToString(encryptedData)
}

func DecryptWithAesECB(password, seed, additionalSalt string) string {
	ciphertext := strings.TrimPrefix(password, aesPrefix)
	decodedData, err := base64.StdEncoding.DecodeString(ciphertext)
	if err != nil {
		fmt.Fprintln(os.Stderr, "base64 decode error:", err)
		os.Exit(1)
	}
	key := generateKeyFromSeedAndSalt(seed, additionalSalt)
	block, err := aes.NewCipher(key)
	if err != nil {
		fmt.Fprintln(os.Stderr, "create cipher error:", err)
		os.Exit(1)
	}
	decryptedData := make([]byte, len(decodedData))
	for i := 0; i < len(decodedData); i += aes.BlockSize {
		block.Decrypt(decryptedData[i:], decodedData[i:])
	}
	return string(pkcs5UnPadding(decryptedData))
}

func main() {
	mode := flag.String("mode", "decrypt", "操作模式: encrypt 或 decrypt")
	seed := flag.String("seed", "", "resource_password_seed（必填）")
	name := flag.String("name", "", "资源名称，即 resource_server.name 或 resource_item.name（必填）")
	password := flag.String("password", "", "待处理的密码")
	flag.Parse()

	if *seed == "" || *name == "" || *password == "" {
		fmt.Fprintln(os.Stderr, "用法: e -seed <seed> -name <resource_name> -password <password> [-mode encrypt|decrypt]")
		flag.PrintDefaults()
		os.Exit(1)
	}

	switch *mode {
	case "decrypt":
		fmt.Println(DecryptWithAesECB(*password, *seed, *name))
	case "encrypt":
		fmt.Println(aesPrefix + EncryptWithAesECB(*password, *seed, *name))
	default:
		fmt.Fprintln(os.Stderr, "未知 mode:", *mode)
		os.Exit(1)
	}
}
