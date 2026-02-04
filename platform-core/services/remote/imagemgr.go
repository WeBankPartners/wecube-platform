package remote

import (
	"fmt"
	"os"
	"os/exec"
	"strings"
)

// ImageManager 镜像管理器，基于 skopeo 实现
type ImageManager struct {
	registryURL string // 镜像仓库地址
	username    string // 认证用户名
	password    string // 认证密码
	insecure    bool   // 是否忽略 HTTPS 证书验证
}

// ImageUploadOptions 镜像上传选项
type ImageUploadOptions struct {
	LocalTarPath string // 本地镜像 tar 文件路径，例如: /xxx/xx/image.tar
	Name         string // 镜像名称，例如: myapp 或 corg/demo
	Tag          string // 镜像标签，例如: latest
	Insecure     bool   // 是否忽略 HTTPS 证书验证
	Force        bool   // 是否强制上传（支持重复上传）
}

// NewImageManager 创建镜像管理器
// registryURL: 镜像仓库地址，支持以下格式:
//   - "registry.example.com" 或 "192.168.1.100:5000"
//   - "https://192.168.1.100:5000/corg" (包含协议和项目路径)
//
// username: 仓库认证用户名
// password: 仓库认证密码
func NewImageManager(registryURL, username, password string) *ImageManager {
	return &ImageManager{
		registryURL: registryURL,
		username:    username,
		password:    password,
		insecure:    false,
	}
}

// SetInsecure 设置是否忽略 HTTPS 证书验证
func (m *ImageManager) SetInsecure(insecure bool) {
	m.insecure = insecure
}

// CheckSkopeoInstalled 检查 skopeo 是否已安装
func (m *ImageManager) CheckSkopeoInstalled() error {
	_, err := exec.LookPath("skopeo")
	if err != nil {
		return fmt.Errorf("skopeo not found, please install skopeo first: %s", err.Error())
	}
	return nil
}

// UploadImage 上传镜像到远程仓库
// 支持重复上传、HTTP/HTTPS、证书忽略
func (m *ImageManager) UploadImage(opts ImageUploadOptions) error {
	// 1. 检查 skopeo 是否安装
	if err := m.CheckSkopeoInstalled(); err != nil {
		return err
	}

	// 2. 检查本地 tar 文件是否存在
	if _, err := os.Stat(opts.LocalTarPath); os.IsNotExist(err) {
		return fmt.Errorf("local tar file not found: %s", opts.LocalTarPath)
	}

	// 3. 构建源地址 (docker-archive)
	sourceRef := fmt.Sprintf("docker-archive:%s", opts.LocalTarPath)

	// 4. 构建目标地址 (docker)
	// 判断协议
	protocol := "docker://"
	if strings.HasPrefix(m.registryURL, "http://") || strings.HasPrefix(m.registryURL, "https://") {
		// 已包含协议，直接使用
		protocol = ""
	}

	destRef := fmt.Sprintf("%s%s/%s:%s", protocol, m.registryURL, opts.Name, opts.Tag)

	// 5. 构建 skopeo copy 命令
	args := []string{"copy"}

	// 添加认证信息
	if m.username != "" && m.password != "" {
		args = append(args, "--dest-creds", fmt.Sprintf("%s:%s", m.username, m.password))
	}

	// 是否忽略证书验证
	if opts.Insecure || m.insecure {
		args = append(args, "--dest-tls-verify=false")
	}

	// 是否强制上传（支持重复上传）
	if opts.Force {
		args = append(args, "--dest-precompute-digests")
	}

	// 添加源和目标
	args = append(args, sourceRef, destRef)

	// 6. 执行 skopeo copy 命令
	cmd := exec.Command("skopeo", args...)

	// 设置环境变量（可选）
	cmd.Env = os.Environ()

	// 捕获输出
	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("skopeo copy fail: %s, output: %s", err.Error(), string(output))
	}

	return nil
}

// UploadImageSimple 简化的上传方法
// localTarPath: 本地 tar 文件路径
// imageName: 镜像名称，例如: myapp 或 corg/demo
// tag: 镜像标签
func (m *ImageManager) UploadImageSimple(localTarPath, imageName, tag string) error {
	return m.UploadImage(ImageUploadOptions{
		LocalTarPath: localTarPath,
		Name:         imageName,
		Tag:          tag,
		Insecure:     m.insecure,
		Force:        true, // 默认支持重复上传
	})
}

func (m *ImageManager) UploadImageSimple2(localTarPath, imageNameTag string) error {
	parts := strings.Split(imageNameTag, ":")
	if len(parts) != 2 {
		return fmt.Errorf("imageNameTag format error")
	}
	return m.UploadImage(ImageUploadOptions{
		LocalTarPath: localTarPath,
		Name:         parts[0],
		Tag:          parts[1],
		Insecure:     m.insecure,
		Force:        true, // 默认支持重复上传
	})
}

// InspectImage 检查远程镜像信息
func (m *ImageManager) InspectImage(imageName, tag string) (string, error) {
	// 检查 skopeo 是否安装
	if err := m.CheckSkopeoInstalled(); err != nil {
		return "", err
	}

	// 构建镜像地址
	protocol := "docker://"
	if strings.HasPrefix(m.registryURL, "http://") || strings.HasPrefix(m.registryURL, "https://") {
		protocol = ""
	}
	imageRef := fmt.Sprintf("%s%s/%s:%s", protocol, m.registryURL, imageName, tag)

	// 构建命令
	args := []string{"inspect"}

	// 添加认证
	if m.username != "" && m.password != "" {
		args = append(args, "--creds", fmt.Sprintf("%s:%s", m.username, m.password))
	}

	// 是否忽略证书
	if m.insecure {
		args = append(args, "--tls-verify=false")
	}

	args = append(args, imageRef)

	// 执行命令
	cmd := exec.Command("skopeo", args...)
	output, err := cmd.CombinedOutput()
	if err != nil {
		return "", fmt.Errorf("skopeo inspect fail: %s, output: %s", err.Error(), string(output))
	}

	return string(output), nil
}

// DeleteImage 删除远程镜像
func (m *ImageManager) DeleteImage(imageName, tag string) error {
	// 检查 skopeo 是否安装
	if err := m.CheckSkopeoInstalled(); err != nil {
		return err
	}

	// 构建镜像地址
	protocol := "docker://"
	if strings.HasPrefix(m.registryURL, "http://") || strings.HasPrefix(m.registryURL, "https://") {
		protocol = ""
	}
	imageRef := fmt.Sprintf("%s%s/%s:%s", protocol, m.registryURL, imageName, tag)

	// 构建命令
	args := []string{"delete"}

	// 添加认证
	if m.username != "" && m.password != "" {
		args = append(args, "--creds", fmt.Sprintf("%s:%s", m.username, m.password))
	}

	// 是否忽略证书
	if m.insecure {
		args = append(args, "--tls-verify=false")
	}

	args = append(args, imageRef)

	// 执行命令
	cmd := exec.Command("skopeo", args...)
	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("skopeo delete fail: %s, output: %s", err.Error(), string(output))
	}

	return nil
}

// CopyImage 从一个仓库复制镜像到另一个仓库
func (m *ImageManager) CopyImage(srcRegistry, srcRepo, srcTag, destRepo, destTag string) error {
	// 检查 skopeo 是否安装
	if err := m.CheckSkopeoInstalled(); err != nil {
		return err
	}

	// 构建源地址
	srcProtocol := "docker://"
	if strings.HasPrefix(srcRegistry, "http://") || strings.HasPrefix(srcRegistry, "https://") {
		srcProtocol = ""
	}
	sourceRef := fmt.Sprintf("%s%s/%s:%s", srcProtocol, srcRegistry, srcRepo, srcTag)

	// 构建目标地址
	destProtocol := "docker://"
	if strings.HasPrefix(m.registryURL, "http://") || strings.HasPrefix(m.registryURL, "https://") {
		destProtocol = ""
	}
	destRef := fmt.Sprintf("%s%s/%s:%s", destProtocol, m.registryURL, destRepo, destTag)

	// 构建命令
	args := []string{"copy"}

	// 添加目标仓库认证
	if m.username != "" && m.password != "" {
		args = append(args, "--dest-creds", fmt.Sprintf("%s:%s", m.username, m.password))
	}

	// 是否忽略证书
	if m.insecure {
		args = append(args, "--src-tls-verify=false", "--dest-tls-verify=false")
	}

	args = append(args, sourceRef, destRef)

	// 执行命令
	cmd := exec.Command("skopeo", args...)
	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("skopeo copy fail: %s, output: %s", err.Error(), string(output))
	}

	return nil
}

// ListTags 列出仓库中的所有标签（需要仓库支持）
func (m *ImageManager) ListTags(imageName string) ([]string, error) {
	// 检查 skopeo 是否安装
	if err := m.CheckSkopeoInstalled(); err != nil {
		return nil, err
	}

	// 构建仓库地址
	protocol := "docker://"
	if strings.HasPrefix(m.registryURL, "http://") || strings.HasPrefix(m.registryURL, "https://") {
		protocol = ""
	}
	repoRef := fmt.Sprintf("%s%s/%s", protocol, m.registryURL, imageName)

	// 构建命令
	args := []string{"list-tags"}

	// 添加认证
	if m.username != "" && m.password != "" {
		args = append(args, "--creds", fmt.Sprintf("%s:%s", m.username, m.password))
	}

	// 是否忽略证书
	if m.insecure {
		args = append(args, "--tls-verify=false")
	}

	args = append(args, repoRef)

	// 执行命令
	cmd := exec.Command("skopeo", args...)
	output, err := cmd.CombinedOutput()
	if err != nil {
		return nil, fmt.Errorf("skopeo list-tags fail: %s, output: %s", err.Error(), string(output))
	}

	// 解析输出（简单分割，实际可能需要 JSON 解析）
	tags := strings.Split(strings.TrimSpace(string(output)), "\n")
	return tags, nil
}

// ImageExists 检查镜像是否存在
func (m *ImageManager) ImageExists(imageName, tag string) (bool, error) {
	_, err := m.InspectImage(imageName, tag)
	if err != nil {
		if strings.Contains(err.Error(), "manifest unknown") ||
			strings.Contains(err.Error(), "not found") {
			return false, nil
		}
		return false, err
	}
	return true, nil
}
