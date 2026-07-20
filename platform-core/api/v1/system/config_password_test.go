package system

import (
	"strings"
	"testing"

	"github.com/WeBankPartners/wecube-platform/platform-core/common/encrypt"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func TestBuildAESCConfigPassword(t *testing.T) {
	oldConfig := models.Config
	defer func() {
		models.Config = oldConfig
	}()
	models.Config = &models.GlobalConfig{
		Auth: &models.AuthConfig{ConfigPasswordKey: "test-config-password-key"},
	}

	result, err := buildAESCConfigPassword(&models.ConfigPasswordEncryptRequest{
		Password:        "db-password",
		ConfirmPassword: "db-password",
	})
	if err != nil {
		t.Fatalf("buildAESCConfigPassword returned error: %v", err)
	}
	if !strings.HasPrefix(result.Ciphertext, encrypt.AESCPrefix) {
		t.Fatalf("ciphertext prefix invalid: %q", result.Ciphertext)
	}
	plainText, err := encrypt.DecryptWithAESC(result.Ciphertext, models.Config.Auth.ConfigPasswordKey)
	if err != nil {
		t.Fatalf("DecryptWithAESC returned error: %v", err)
	}
	if plainText != "db-password" {
		t.Fatalf("plainText = %q, want %q", plainText, "db-password")
	}
}

func TestBuildAESCConfigPasswordRejectsBlankPassword(t *testing.T) {
	oldConfig := models.Config
	defer func() {
		models.Config = oldConfig
	}()
	models.Config = &models.GlobalConfig{
		Auth: &models.AuthConfig{ConfigPasswordKey: "test-config-password-key"},
	}

	_, err := buildAESCConfigPassword(&models.ConfigPasswordEncryptRequest{
		Password:        "",
		ConfirmPassword: "",
	})
	if err == nil {
		t.Fatalf("buildAESCConfigPassword should reject blank password")
	}
}

func TestBuildAESCConfigPasswordRejectsMismatchedPassword(t *testing.T) {
	oldConfig := models.Config
	defer func() {
		models.Config = oldConfig
	}()
	models.Config = &models.GlobalConfig{
		Auth: &models.AuthConfig{ConfigPasswordKey: "test-config-password-key"},
	}

	_, err := buildAESCConfigPassword(&models.ConfigPasswordEncryptRequest{
		Password:        "db-password",
		ConfirmPassword: "another-password",
	})
	if err == nil {
		t.Fatalf("buildAESCConfigPassword should reject mismatched password")
	}
}

func TestBuildAESCConfigPasswordRejectsMissingConfigPasswordKey(t *testing.T) {
	oldConfig := models.Config
	defer func() {
		models.Config = oldConfig
	}()
	models.Config = &models.GlobalConfig{
		Auth: &models.AuthConfig{},
	}

	_, err := buildAESCConfigPassword(&models.ConfigPasswordEncryptRequest{
		Password:        "db-password",
		ConfirmPassword: "db-password",
	})
	if err == nil {
		t.Fatalf("buildAESCConfigPassword should reject missing config password key")
	}
}
