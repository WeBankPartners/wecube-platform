package models

import (
	"testing"

	"github.com/WeBankPartners/wecube-platform/platform-core/common/encrypt"
)

func TestDecryptAESCDatabasePassword(t *testing.T) {
	cipherText, err := encrypt.EncryptWithAESC("db-password", "test-config-password-key")
	if err != nil {
		t.Fatalf("EncryptWithAESC returned error: %v", err)
	}

	plainText, encrypted, err := decryptAESCDatabasePassword(cipherText, "test-config-password-key")
	if err != nil {
		t.Fatalf("decryptAESCDatabasePassword returned error: %v", err)
	}
	if !encrypted {
		t.Fatalf("encrypted = false, want true")
	}
	if plainText != "db-password" {
		t.Fatalf("plainText = %q, want %q", plainText, "db-password")
	}
}

func TestDecryptAESCDatabasePasswordLeavesPlainValue(t *testing.T) {
	plainText, encrypted, err := decryptAESCDatabasePassword("db-password", "test-config-password-key")
	if err != nil {
		t.Fatalf("decryptAESCDatabasePassword returned error: %v", err)
	}
	if encrypted {
		t.Fatalf("encrypted = true, want false")
	}
	if plainText != "db-password" {
		t.Fatalf("plainText = %q, want %q", plainText, "db-password")
	}
}
