package encrypt

import (
	"strings"
	"testing"
)

const testConfigPasswordKey = "test-config-password-key"

func TestAESCEncryptDecrypt(t *testing.T) {
	cipherText, err := EncryptWithAESC("db-password", testConfigPasswordKey)
	if err != nil {
		t.Fatalf("EncryptWithAESC returned error: %v", err)
	}
	if !strings.HasPrefix(cipherText, AESCPrefix) {
		t.Fatalf("ciphertext prefix = %q, want %q", cipherText[:len(AESCPrefix)], AESCPrefix)
	}

	plainText, err := DecryptWithAESC(cipherText, testConfigPasswordKey)
	if err != nil {
		t.Fatalf("DecryptWithAESC returned error: %v", err)
	}
	if plainText != "db-password" {
		t.Fatalf("plaintext = %q, want %q", plainText, "db-password")
	}
}

func TestAESCDecryptRejectsWrongConfigPasswordKey(t *testing.T) {
	cipherText, err := EncryptWithAESC("db-password", testConfigPasswordKey)
	if err != nil {
		t.Fatalf("EncryptWithAESC returned error: %v", err)
	}

	if _, err = DecryptWithAESC(cipherText, "another-config-password-key"); err == nil {
		t.Fatalf("DecryptWithAESC should reject ciphertext encrypted with another config password key")
	}
}

func TestAESCDecryptRejectsTamperedCiphertext(t *testing.T) {
	cipherText, err := EncryptWithAESC("db-password", testConfigPasswordKey)
	if err != nil {
		t.Fatalf("EncryptWithAESC returned error: %v", err)
	}

	tampered := cipherText[:len(cipherText)-1] + "A"
	if _, err = DecryptWithAESC(tampered, testConfigPasswordKey); err == nil {
		t.Fatalf("DecryptWithAESC should reject tampered ciphertext")
	}
}

func TestAESCRejectsEmptyConfigPasswordKey(t *testing.T) {
	if _, err := EncryptWithAESC("db-password", ""); err == nil {
		t.Fatalf("EncryptWithAESC should reject empty config password key")
	}
}
