package service

import (
	"math/rand"
	"time"
)

const (
	DefLength   = 6
	letterBytes = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*"
)

var PasswordGeneratorInstance PasswordGenerator

type PasswordGenerator struct {
}

func (PasswordGenerator) RandomPassword(length int) string {
	if length < 0 || length > 16 {
		length = DefLength
	}
	ran_buf := []byte("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789")

	rand.Seed(time.Now().UnixNano())
	ranChars := make([]byte, length)
	for i := 0; i < length; i++ {
		ranIndex := rand.Intn(length)
		//int ranIndex = random.nextInt(RAN_CHARS.length);
		ranChars[i] = ran_buf[ranIndex]
	}
	return string(ranChars)
}

func (PasswordGenerator) GenerateStrongPassword(length int) string {
	rand.Seed(time.Now().UnixNano())
	b := make([]byte, length)
	// 标记已使用的字符类型
	var (
		largeRunes, smallRunes, digits, specialChars bool
	)
	for i := 0; i < length; {
		if !largeRunes && rand.Intn(4) == 0 {
			b[i] = byte(rand.Intn(26) + 'A')
			largeRunes = true
		} else if !smallRunes && rand.Intn(4) == 0 {
			b[i] = byte(rand.Intn(26) + 'a')
			smallRunes = true
		} else if !digits && rand.Intn(4) == 0 {
			b[i] = byte(rand.Intn(10) + '0')
			digits = true
		} else if !specialChars && rand.Intn(4) == 0 {
			b[i] = byte(letterBytes[rand.Intn(len(letterBytes)-10)])
			specialChars = true
		} else {
			b[i] = letterBytes[rand.Int63()%int64(len(letterBytes))]
		}
		i++
	}
	return string(b)
}
