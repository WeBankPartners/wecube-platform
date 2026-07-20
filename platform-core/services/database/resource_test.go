package database

import (
	"strings"
	"testing"

	"github.com/WeBankPartners/wecube-platform/platform-core/common/encrypt"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func TestBuildResourceItemPasswordUpdatesEncryptsPluginPasswordWithPluginSchema(t *testing.T) {
	originalConfig := models.Config
	t.Cleanup(func() {
		models.Config = originalConfig
	})
	models.Config = &models.GlobalConfig{
		Plugin: &models.PluginJsonConfig{ResourcePasswordSeed: "test-seed"},
	}

	resourcePassword, pluginPassword, updatePluginPassword := buildResourceItemPasswordUpdates(
		"new-password",
		"wecmdb",
		"wecmdb",
		&models.PluginMysqlInstances{SchemaName: "scmdbbmy"},
	)

	if !updatePluginPassword {
		t.Fatalf("updatePluginPassword = false, want true")
	}
	if !strings.HasPrefix(resourcePassword, models.AESPrefix) {
		t.Fatalf("resourcePassword is not AES encoded: %q", resourcePassword)
	}
	if !strings.HasPrefix(pluginPassword, models.AESPrefix) {
		t.Fatalf("pluginPassword is not AES encoded: %q", pluginPassword)
	}
	if resourcePassword == pluginPassword {
		t.Fatalf("resource and plugin passwords should use different salts")
	}

	decryptedResourcePassword := encrypt.DecryptWithAesECB(resourcePassword[len(models.AESPrefix):], models.Config.Plugin.ResourcePasswordSeed, "wecmdb")
	if decryptedResourcePassword != "new-password" {
		t.Fatalf("resource password decrypt = %q, want %q", decryptedResourcePassword, "new-password")
	}
	decryptedPluginPassword := encrypt.DecryptWithAesECB(pluginPassword[len(models.AESPrefix):], models.Config.Plugin.ResourcePasswordSeed, "scmdbbmy")
	if decryptedPluginPassword != "new-password" {
		t.Fatalf("plugin password decrypt = %q, want %q", decryptedPluginPassword, "new-password")
	}
}

func TestBuildResourceItemPasswordUpdatesDoesNotOverwritePluginPasswordForExistingCiphertextWhenNameUnchanged(t *testing.T) {
	originalConfig := models.Config
	t.Cleanup(func() {
		models.Config = originalConfig
	})
	models.Config = &models.GlobalConfig{
		Plugin: &models.PluginJsonConfig{ResourcePasswordSeed: "test-seed"},
	}
	existingCiphertext := models.AESPrefix + encrypt.EncryptWithAesECB("old-password", models.Config.Plugin.ResourcePasswordSeed, "wecmdb")

	resourcePassword, pluginPassword, updatePluginPassword := buildResourceItemPasswordUpdates(
		existingCiphertext,
		"wecmdb",
		"wecmdb",
		&models.PluginMysqlInstances{SchemaName: "scmdbbmy"},
	)

	if updatePluginPassword {
		t.Fatalf("updatePluginPassword = true, want false")
	}
	if pluginPassword != "" {
		t.Fatalf("pluginPassword = %q, want empty", pluginPassword)
	}
	if resourcePassword != existingCiphertext {
		t.Fatalf("resourcePassword changed, got %q want %q", resourcePassword, existingCiphertext)
	}
}

func TestBuildResourceItemPasswordUpdatesReencryptsResourceOnlyWhenNameChanged(t *testing.T) {
	originalConfig := models.Config
	t.Cleanup(func() {
		models.Config = originalConfig
	})
	models.Config = &models.GlobalConfig{
		Plugin: &models.PluginJsonConfig{ResourcePasswordSeed: "test-seed"},
	}
	existingCiphertext := models.AESPrefix + encrypt.EncryptWithAesECB("old-password", models.Config.Plugin.ResourcePasswordSeed, "old-name")

	resourcePassword, pluginPassword, updatePluginPassword := buildResourceItemPasswordUpdates(
		existingCiphertext,
		"old-name",
		"new-name",
		&models.PluginMysqlInstances{SchemaName: "schema-name"},
	)

	if updatePluginPassword {
		t.Fatalf("updatePluginPassword = true, want false")
	}
	if pluginPassword != "" {
		t.Fatalf("pluginPassword = %q, want empty", pluginPassword)
	}
	if resourcePassword == existingCiphertext {
		t.Fatalf("resourcePassword should be reencrypted when resource item name changes")
	}
	decryptedResourcePassword := encrypt.DecryptWithAesECB(resourcePassword[len(models.AESPrefix):], models.Config.Plugin.ResourcePasswordSeed, "new-name")
	if decryptedResourcePassword != "old-password" {
		t.Fatalf("resource password decrypt = %q, want %q", decryptedResourcePassword, "old-password")
	}
}

func TestBuildResourceItemPasswordUpdatesReencryptsResourceOnlyWhenNameChangedWithoutPlugin(t *testing.T) {
	originalConfig := models.Config
	t.Cleanup(func() {
		models.Config = originalConfig
	})
	models.Config = &models.GlobalConfig{
		Plugin: &models.PluginJsonConfig{ResourcePasswordSeed: "test-seed"},
	}
	existingCiphertext := models.AESPrefix + encrypt.EncryptWithAesECB("old-password", models.Config.Plugin.ResourcePasswordSeed, "old-name")

	resourcePassword, pluginPassword, updatePluginPassword := buildResourceItemPasswordUpdates(
		existingCiphertext,
		"old-name",
		"new-name",
		nil,
	)

	if updatePluginPassword {
		t.Fatalf("updatePluginPassword = true, want false")
	}
	if pluginPassword != "" {
		t.Fatalf("pluginPassword = %q, want empty", pluginPassword)
	}
	decryptedResourcePassword := encrypt.DecryptWithAesECB(resourcePassword[len(models.AESPrefix):], models.Config.Plugin.ResourcePasswordSeed, "new-name")
	if decryptedResourcePassword != "old-password" {
		t.Fatalf("resource password decrypt = %q, want %q", decryptedResourcePassword, "old-password")
	}
}

func TestBuildResourceItemPasswordUpdatesEncryptsChangedPasswordWhenNameUnchangedWithPlugin(t *testing.T) {
	originalConfig := models.Config
	t.Cleanup(func() {
		models.Config = originalConfig
	})
	models.Config = &models.GlobalConfig{
		Plugin: &models.PluginJsonConfig{ResourcePasswordSeed: "test-seed"},
	}

	resourcePassword, pluginPassword, updatePluginPassword := buildResourceItemPasswordUpdates(
		"new-password",
		"resource-name",
		"resource-name",
		&models.PluginMysqlInstances{SchemaName: "schema-name"},
	)

	if !updatePluginPassword {
		t.Fatalf("updatePluginPassword = false, want true")
	}
	decryptedResourcePassword := encrypt.DecryptWithAesECB(resourcePassword[len(models.AESPrefix):], models.Config.Plugin.ResourcePasswordSeed, "resource-name")
	if decryptedResourcePassword != "new-password" {
		t.Fatalf("resource password decrypt = %q, want %q", decryptedResourcePassword, "new-password")
	}
	decryptedPluginPassword := encrypt.DecryptWithAesECB(pluginPassword[len(models.AESPrefix):], models.Config.Plugin.ResourcePasswordSeed, "schema-name")
	if decryptedPluginPassword != "new-password" {
		t.Fatalf("plugin password decrypt = %q, want %q", decryptedPluginPassword, "new-password")
	}
}

func TestBuildResourceItemPasswordUpdatesEncryptsChangedPasswordWhenNameChangedWithPlugin(t *testing.T) {
	originalConfig := models.Config
	t.Cleanup(func() {
		models.Config = originalConfig
	})
	models.Config = &models.GlobalConfig{
		Plugin: &models.PluginJsonConfig{ResourcePasswordSeed: "test-seed"},
	}

	resourcePassword, pluginPassword, updatePluginPassword := buildResourceItemPasswordUpdates(
		"new-password",
		"old-name",
		"new-name",
		&models.PluginMysqlInstances{SchemaName: "schema-name"},
	)

	if !updatePluginPassword {
		t.Fatalf("updatePluginPassword = false, want true")
	}
	decryptedResourcePassword := encrypt.DecryptWithAesECB(resourcePassword[len(models.AESPrefix):], models.Config.Plugin.ResourcePasswordSeed, "new-name")
	if decryptedResourcePassword != "new-password" {
		t.Fatalf("resource password decrypt = %q, want %q", decryptedResourcePassword, "new-password")
	}
	decryptedPluginPassword := encrypt.DecryptWithAesECB(pluginPassword[len(models.AESPrefix):], models.Config.Plugin.ResourcePasswordSeed, "schema-name")
	if decryptedPluginPassword != "new-password" {
		t.Fatalf("plugin password decrypt = %q, want %q", decryptedPluginPassword, "new-password")
	}
}

func TestBuildResourceItemPasswordUpdatesEncryptsChangedPasswordWhenNameUnchangedWithoutPlugin(t *testing.T) {
	originalConfig := models.Config
	t.Cleanup(func() {
		models.Config = originalConfig
	})
	models.Config = &models.GlobalConfig{
		Plugin: &models.PluginJsonConfig{ResourcePasswordSeed: "test-seed"},
	}

	resourcePassword, pluginPassword, updatePluginPassword := buildResourceItemPasswordUpdates(
		"new-password",
		"resource-name",
		"resource-name",
		nil,
	)

	if updatePluginPassword {
		t.Fatalf("updatePluginPassword = true, want false")
	}
	if pluginPassword != "" {
		t.Fatalf("pluginPassword = %q, want empty", pluginPassword)
	}
	decryptedResourcePassword := encrypt.DecryptWithAesECB(resourcePassword[len(models.AESPrefix):], models.Config.Plugin.ResourcePasswordSeed, "resource-name")
	if decryptedResourcePassword != "new-password" {
		t.Fatalf("resource password decrypt = %q, want %q", decryptedResourcePassword, "new-password")
	}
}

func TestBuildResourceItemPasswordUpdatesEncryptsChangedPasswordWhenNameChangedWithoutPlugin(t *testing.T) {
	originalConfig := models.Config
	t.Cleanup(func() {
		models.Config = originalConfig
	})
	models.Config = &models.GlobalConfig{
		Plugin: &models.PluginJsonConfig{ResourcePasswordSeed: "test-seed"},
	}

	resourcePassword, pluginPassword, updatePluginPassword := buildResourceItemPasswordUpdates(
		"new-password",
		"old-name",
		"new-name",
		nil,
	)

	if updatePluginPassword {
		t.Fatalf("updatePluginPassword = true, want false")
	}
	if pluginPassword != "" {
		t.Fatalf("pluginPassword = %q, want empty", pluginPassword)
	}
	decryptedResourcePassword := encrypt.DecryptWithAesECB(resourcePassword[len(models.AESPrefix):], models.Config.Plugin.ResourcePasswordSeed, "new-name")
	if decryptedResourcePassword != "new-password" {
		t.Fatalf("resource password decrypt = %q, want %q", decryptedResourcePassword, "new-password")
	}
}
