package service

import (
	"math/rand"
	"regexp"
	"time"
)

const (
	DefLength = 6
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
	if length < 3 {
		length = 3
	}
	rand.Seed(time.Now().UnixNano())
	b := make([]byte, length)
	const letterBytes = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*"
	// 标记已使用的字符类型
	var (
		largeRunes, smallRunes, digits, specialChars bool
		typeCnt                                      int
	)
	for i := 0; i < length; {
		oldLargeRunes, oldSmallRunes, oldDigits, oldSpecialChars, oldTypeCnt := largeRunes, smallRunes, digits, specialChars, typeCnt
		if !largeRunes && rand.Intn(4) == 0 {
			b[i] = byte(rand.Intn(26) + 'A')
			largeRunes = true
			typeCnt += 1
		} else if !smallRunes && rand.Intn(4) == 0 {
			b[i] = byte(rand.Intn(26) + 'a')
			smallRunes = true
			typeCnt += 1
		} else if !digits && rand.Intn(4) == 0 {
			b[i] = byte(rand.Intn(10) + '0')
			digits = true
			typeCnt += 1
		} else if !specialChars && rand.Intn(4) == 0 {
			b[i] = byte(letterBytes[rand.Intn(len(letterBytes)-(26+26+10))+26+26+10])
			specialChars = true
			typeCnt += 1
		} else {
			letterIdx := rand.Int63() % int64(len(letterBytes))
			b[i] = letterBytes[letterIdx]
			if letterIdx < 26 {
				if !smallRunes {
					smallRunes = true
					typeCnt += 1
				}
			} else if letterIdx < 26+26 {
				if !largeRunes {
					largeRunes = true
					typeCnt += 1
				}
			} else if letterIdx < 26+26+10 {
				if !digits {
					digits = true
					typeCnt += 1
				}
			} else {
				if !specialChars {
					specialChars = true
					typeCnt += 1
				}
			}
		}
		if length-i-1 < 3-typeCnt {
			// 剩余未产生字符数<缺失类型数，重新随当前字符
			largeRunes, smallRunes, digits, specialChars, typeCnt = oldLargeRunes, oldSmallRunes, oldDigits, oldSpecialChars, oldTypeCnt
			continue
		}
		i++
	}
	return string(b)
}

func (PasswordGenerator) CheckPasswordStrength(password string) bool {
	if len(password) < 8 {
		return false
	}

	var (
		uppercaseRegex = regexp.MustCompile(`[A-Z]`)
		lowercaseRegex = regexp.MustCompile(`[a-z]`)
		numberRegex    = regexp.MustCompile(`[0-9]`)
		specialRegex   = regexp.MustCompile(`[!@#$%^&*()_+\-=\[\]{}\\:;'",.<>/?|~]`)
	)

	typeCnt := 0
	if uppercaseRegex.MatchString(password) {
		typeCnt += 1
	}
	if lowercaseRegex.MatchString(password) {
		typeCnt += 1
	}
	if numberRegex.MatchString(password) {
		typeCnt += 1
	}
	if specialRegex.MatchString(password) {
		typeCnt += 1
	}
	// test code:
	// s := `!@#$%^&*()_+-=[]{}\:;'",.<>/?|~`
	// for _, c := range strings.Split(s, "") {
	// 	if !specialRegex.MatchString(c) {
	// 		fmt.Println(c)
	// 	}
	// }

	return typeCnt >= 3
}
