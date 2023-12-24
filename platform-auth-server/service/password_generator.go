package service

import (
	"math/rand"
	"time"
)

const DefLength = 6

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
