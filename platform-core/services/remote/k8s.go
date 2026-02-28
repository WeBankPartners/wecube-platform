package remote

import (
	"context"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"strings"

	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	v1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
)

// K8sClient k8s客户端封装
type K8sClient struct {
	clientset *kubernetes.Clientset
	config    *rest.Config
}

// NewK8sClient 创建k8s客户端，使用官方client-go库
// apiUrl: k8s API服务器地址，例如 "https://kubernetes.default.svc"
// token: Bearer token用于认证
func NewK8sClient(apiUrl, token string) (*K8sClient, error) {
	config := &rest.Config{
		Host:        apiUrl,
		BearerToken: token,
		TLSClientConfig: rest.TLSClientConfig{
			Insecure: true, // 跳过TLS证书验证，适用于自签名证书
		},
	}

	// 注意：不要同时设置 TLSClientConfig 和自定义 Transport
	// client-go 会基于 TLSClientConfig 自动构建合适的 Transport

	clientset, err := kubernetes.NewForConfig(config)
	if err != nil {
		return nil, fmt.Errorf("create kubernetes clientset fail: %s", err.Error())
	}

	return &K8sClient{
		clientset: clientset,
		config:    config,
	}, nil
}

// CreateService 创建Service
func (c *K8sClient) CreateService(ctx context.Context, namespace string, service *corev1.Service) (*corev1.Service, error) {
	result, err := c.clientset.CoreV1().Services(namespace).Create(ctx, service, metav1.CreateOptions{})
	if err != nil {
		return nil, fmt.Errorf("create service fail: %s", err.Error())
	}
	return result, nil
}

// DeleteService 删除Service
func (c *K8sClient) DeleteService(ctx context.Context, namespace, name string) error {
	err := c.clientset.CoreV1().Services(namespace).Delete(ctx, name, metav1.DeleteOptions{})
	if err != nil {
		return fmt.Errorf("delete service fail: %s", err.Error())
	}
	return nil
}

// GetService 获取Service详情
func (c *K8sClient) GetService(ctx context.Context, namespace, name string) (*corev1.Service, error) {
	result, err := c.clientset.CoreV1().Services(namespace).Get(ctx, name, metav1.GetOptions{})
	if err != nil {
		return nil, fmt.Errorf("get service fail: %s", err.Error())
	}
	return result, nil
}

// ListServices 列出命名空间下的所有Service
func (c *K8sClient) ListServices(ctx context.Context, namespace string) (*corev1.ServiceList, error) {
	result, err := c.clientset.CoreV1().Services(namespace).List(ctx, metav1.ListOptions{})
	if err != nil {
		return nil, fmt.Errorf("list services fail: %s", err.Error())
	}
	return result, nil
}

// CreateStatefulSet 创建StatefulSet
func (c *K8sClient) CreateStatefulSet(ctx context.Context, namespace string, sts *appsv1.StatefulSet) (*appsv1.StatefulSet, error) {
	result, err := c.clientset.AppsV1().StatefulSets(namespace).Create(ctx, sts, metav1.CreateOptions{})
	if err != nil {
		return nil, fmt.Errorf("create statefulset fail: %s", err.Error())
	}
	return result, nil
}

// DeleteStatefulSet 删除StatefulSet
func (c *K8sClient) DeleteStatefulSet(ctx context.Context, namespace, name string) error {
	err := c.clientset.AppsV1().StatefulSets(namespace).Delete(ctx, name, metav1.DeleteOptions{})
	if err != nil {
		return fmt.Errorf("delete statefulset fail: %s", err.Error())
	}
	return nil
}

// GetStatefulSet 获取StatefulSet详情
func (c *K8sClient) GetStatefulSet(ctx context.Context, namespace, name string) (*appsv1.StatefulSet, error) {
	result, err := c.clientset.AppsV1().StatefulSets(namespace).Get(ctx, name, metav1.GetOptions{})
	if err != nil {
		return nil, fmt.Errorf("get statefulset fail: %s", err.Error())
	}
	return result, nil
}

// ListStatefulSets 列出命名空间下的所有StatefulSet
func (c *K8sClient) ListStatefulSets(ctx context.Context, namespace string) (*appsv1.StatefulSetList, error) {
	result, err := c.clientset.AppsV1().StatefulSets(namespace).List(ctx, metav1.ListOptions{})
	if err != nil {
		return nil, fmt.Errorf("list statefulsets fail: %s", err.Error())
	}
	return result, nil
}

// UpdateStatefulSet 更新StatefulSet
func (c *K8sClient) UpdateStatefulSet(ctx context.Context, namespace string, sts *appsv1.StatefulSet) (*appsv1.StatefulSet, error) {
	result, err := c.clientset.AppsV1().StatefulSets(namespace).Update(ctx, sts, metav1.UpdateOptions{})
	if err != nil {
		return nil, fmt.Errorf("update statefulset fail: %s", err.Error())
	}
	return result, nil
}

// UpdateService 更新Service
func (c *K8sClient) UpdateService(ctx context.Context, namespace string, service *corev1.Service) (*corev1.Service, error) {
	result, err := c.clientset.CoreV1().Services(namespace).Update(ctx, service, metav1.UpdateOptions{})
	if err != nil {
		return nil, fmt.Errorf("update service fail: %s", err.Error())
	}
	return result, nil
}

// GetClientset 获取原生clientset，用于更复杂的操作
func (c *K8sClient) GetClientset() *kubernetes.Clientset {
	return c.clientset
}

// ========== Pod 相关方法 ==========

// GetPod 获取Pod详情
func (c *K8sClient) GetPod(ctx context.Context, namespace, name string) (*corev1.Pod, error) {
	result, err := c.clientset.CoreV1().Pods(namespace).Get(ctx, name, metav1.GetOptions{})
	if err != nil {
		return nil, fmt.Errorf("get pod fail: %s", err.Error())
	}
	return result, nil
}

// ListPods 列出命名空间下的所有Pod
func (c *K8sClient) ListPods(ctx context.Context, namespace string) (*corev1.PodList, error) {
	result, err := c.clientset.CoreV1().Pods(namespace).List(ctx, metav1.ListOptions{})
	if err != nil {
		return nil, fmt.Errorf("list pods fail: %s", err.Error())
	}
	return result, nil
}

// ListPodsByLabel 根据标签选择器列出Pod
func (c *K8sClient) ListPodsByLabel(ctx context.Context, namespace string, labelSelector string) (*corev1.PodList, error) {
	result, err := c.clientset.CoreV1().Pods(namespace).List(ctx, metav1.ListOptions{
		LabelSelector: labelSelector,
	})
	if err != nil {
		return nil, fmt.Errorf("list pods by label fail: %s", err.Error())
	}
	return result, nil
}

// DeletePod 删除Pod
func (c *K8sClient) DeletePod(ctx context.Context, namespace, name string) error {
	err := c.clientset.CoreV1().Pods(namespace).Delete(ctx, name, metav1.DeleteOptions{})
	if err != nil {
		return fmt.Errorf("delete pod fail: %s", err.Error())
	}
	return nil
}

// ========== ConfigMap 相关方法 ==========

// GetConfigMap 获取ConfigMap详情
func (c *K8sClient) GetConfigMap(ctx context.Context, namespace, name string) (*corev1.ConfigMap, error) {
	result, err := c.clientset.CoreV1().ConfigMaps(namespace).Get(ctx, name, metav1.GetOptions{})
	if err != nil {
		return nil, fmt.Errorf("get configmap fail: %s", err.Error())
	}
	return result, nil
}

// ListConfigMaps 列出命名空间下的所有ConfigMap
func (c *K8sClient) ListConfigMaps(ctx context.Context, namespace string) (*corev1.ConfigMapList, error) {
	result, err := c.clientset.CoreV1().ConfigMaps(namespace).List(ctx, metav1.ListOptions{})
	if err != nil {
		return nil, fmt.Errorf("list configmaps fail: %s", err.Error())
	}
	return result, nil
}

// CreateConfigMap 创建ConfigMap
func (c *K8sClient) CreateConfigMap(ctx context.Context, namespace string, cm *corev1.ConfigMap) (*corev1.ConfigMap, error) {
	result, err := c.clientset.CoreV1().ConfigMaps(namespace).Create(ctx, cm, metav1.CreateOptions{})
	if err != nil {
		return nil, fmt.Errorf("create configmap fail: %s", err.Error())
	}
	return result, nil
}

// UpdateConfigMap 更新ConfigMap
func (c *K8sClient) UpdateConfigMap(ctx context.Context, namespace string, cm *corev1.ConfigMap) (*corev1.ConfigMap, error) {
	result, err := c.clientset.CoreV1().ConfigMaps(namespace).Update(ctx, cm, metav1.UpdateOptions{})
	if err != nil {
		return nil, fmt.Errorf("update configmap fail: %s", err.Error())
	}
	return result, nil
}

// DeleteConfigMap 删除ConfigMap
func (c *K8sClient) DeleteConfigMap(ctx context.Context, namespace, name string) error {
	err := c.clientset.CoreV1().ConfigMaps(namespace).Delete(ctx, name, metav1.DeleteOptions{})
	if err != nil {
		return fmt.Errorf("delete configmap fail: %s", err.Error())
	}
	return nil
}

// ========== Secret 相关方法 ==========

// GetSecret 获取Secret详情
func (c *K8sClient) GetSecret(ctx context.Context, namespace, name string) (*corev1.Secret, error) {
	result, err := c.clientset.CoreV1().Secrets(namespace).Get(ctx, name, metav1.GetOptions{})
	if err != nil {
		return nil, fmt.Errorf("get secret fail: %s", err.Error())
	}
	return result, nil
}

// ListSecrets 列出命名空间下的所有Secret
func (c *K8sClient) ListSecrets(ctx context.Context, namespace string) (*corev1.SecretList, error) {
	result, err := c.clientset.CoreV1().Secrets(namespace).List(ctx, metav1.ListOptions{})
	if err != nil {
		return nil, fmt.Errorf("list secrets fail: %s", err.Error())
	}
	return result, nil
}

// CreateSecret 创建Secret
func (c *K8sClient) CreateSecret(ctx context.Context, namespace string, secret *corev1.Secret) (*corev1.Secret, error) {
	result, err := c.clientset.CoreV1().Secrets(namespace).Create(ctx, secret, metav1.CreateOptions{})
	if err != nil {
		return nil, fmt.Errorf("create secret fail: %s", err.Error())
	}
	return result, nil
}

// UpdateSecret 更新Secret
func (c *K8sClient) UpdateSecret(ctx context.Context, namespace string, secret *corev1.Secret) (*corev1.Secret, error) {
	result, err := c.clientset.CoreV1().Secrets(namespace).Update(ctx, secret, metav1.UpdateOptions{})
	if err != nil {
		return nil, fmt.Errorf("update secret fail: %s", err.Error())
	}
	return result, nil
}

// DeleteSecret 删除Secret
func (c *K8sClient) DeleteSecret(ctx context.Context, namespace, name string) error {
	err := c.clientset.CoreV1().Secrets(namespace).Delete(ctx, name, metav1.DeleteOptions{})
	if err != nil {
		return fmt.Errorf("delete secret fail: %s", err.Error())
	}
	return nil
}

// buildDockerConfigJSON 构建 Docker config.json 格式的认证配置
// registry: 镜像仓库地址，格式为 "host:port" 或 "host"，不要包含协议(http/https)和路径
// username: 用户名
// password: 密码
func buildDockerConfigJSON(registry, username, password string) ([]byte, error) {
	// 清理 registry 地址：移除协议前缀和路径
	registry = strings.TrimPrefix(registry, "https://")
	registry = strings.TrimPrefix(registry, "http://")
	// 移除路径部分，只保留 host:port
	if idx := strings.Index(registry, "/"); idx != -1 {
		registry = registry[:idx]
	}

	auth := base64.StdEncoding.EncodeToString(
		[]byte(username + ":" + password),
	)

	cfg := map[string]interface{}{
		"auths": map[string]interface{}{
			registry: map[string]string{
				"username": username,
				"password": password,
				"auth":     auth,
			},
		},
	}

	b, err := json.Marshal(cfg)
	if err != nil {
		return nil, fmt.Errorf("marshal docker config fail: %w", err)
	}
	return b, nil
}

// CreateOrUpdateImagePullSecret 创建或更新镜像拉取 Secret（幂等操作）
// namespace: 命名空间
// name: Secret 名称
// registry: 镜像仓库地址，例如: "192.168.1.100:5000" 或 "registry.example.com"
//           如果包含协议或路径会自动清理，例如: "https://192.168.1.100:5000/corg" -> "192.168.1.100:5000"
// username: 仓库用户名
// password: 仓库密码
func (c *K8sClient) CreateOrUpdateImagePullSecret(
	ctx context.Context,
	namespace, name string,
	registry, username, password string,
) error {
	sc := c.clientset.CoreV1().Secrets(namespace)

	// 清理 registry 地址
	registry = strings.TrimPrefix(registry, "https://")
	registry = strings.TrimPrefix(registry, "http://")
	if idx := strings.Index(registry, "/"); idx != -1 {
		registry = registry[:idx]
	}

	// 构建期望的认证信息（用于比较）
	expectedAuth := base64.StdEncoding.EncodeToString(
		[]byte(username + ":" + password),
	)

	secret, err := sc.Get(ctx, name, metav1.GetOptions{})
	if err != nil {
		if errors.IsNotFound(err) {
			// 不存在 → 创建
			dockerConfigJSON, err := buildDockerConfigJSON(registry, username, password)
			if err != nil {
				return fmt.Errorf("build docker config fail: %w", err)
			}

			_, err = sc.Create(ctx, &v1.Secret{
				ObjectMeta: metav1.ObjectMeta{
					Name:      name,
					Namespace: namespace,
				},
				Type: v1.SecretTypeDockerConfigJson,
				Data: map[string][]byte{
					corev1.DockerConfigJsonKey: dockerConfigJSON,
				},
			}, metav1.CreateOptions{})
			return err
		}
		return err
	}

	// 已存在 → 解析并比较内容
	if secret.Type != v1.SecretTypeDockerConfigJson {
		// 类型不对，需要更新
		dockerConfigJSON, err := buildDockerConfigJSON(registry, username, password)
		if err != nil {
			return fmt.Errorf("build docker config fail: %w", err)
		}
		secret.Type = v1.SecretTypeDockerConfigJson
		secret.Data = map[string][]byte{
			corev1.DockerConfigJsonKey: dockerConfigJSON,
		}
		_, err = sc.Update(ctx, secret, metav1.UpdateOptions{})
		return err
	}

	// 解析现有的 Docker config
	var existingConfig struct {
		Auths map[string]struct {
			Username string `json:"username"`
			Password string `json:"password"`
			Auth     string `json:"auth"`
		} `json:"auths"`
	}

	if err := json.Unmarshal(secret.Data[corev1.DockerConfigJsonKey], &existingConfig); err != nil {
		// 解析失败，重新生成
		dockerConfigJSON, err := buildDockerConfigJSON(registry, username, password)
		if err != nil {
			return fmt.Errorf("build docker config fail: %w", err)
		}
		secret.Data = map[string][]byte{
			corev1.DockerConfigJsonKey: dockerConfigJSON,
		}
		_, err = sc.Update(ctx, secret, metav1.UpdateOptions{})
		return err
	}

	// 比较内容是否一致
	authInfo, exists := existingConfig.Auths[registry]
	if exists &&
		authInfo.Username == username &&
		authInfo.Password == password &&
		authInfo.Auth == expectedAuth {
		// 完全一致，不更新
		return nil
	}

	// 不一致 → 更新
	dockerConfigJSON, err := buildDockerConfigJSON(registry, username, password)
	if err != nil {
		return fmt.Errorf("build docker config fail: %w", err)
	}
	secret.Data = map[string][]byte{
		corev1.DockerConfigJsonKey: dockerConfigJSON,
	}

	_, err = sc.Update(ctx, secret, metav1.UpdateOptions{})
	return err
}

// ========== PersistentVolumeClaim 相关方法 ==========

// GetPVC 获取PersistentVolumeClaim详情
func (c *K8sClient) GetPVC(ctx context.Context, namespace, name string) (*corev1.PersistentVolumeClaim, error) {
	result, err := c.clientset.CoreV1().PersistentVolumeClaims(namespace).Get(ctx, name, metav1.GetOptions{})
	if err != nil {
		return nil, fmt.Errorf("get pvc fail: %s", err.Error())
	}
	return result, nil
}

// ListPVCs 列出命名空间下的所有PersistentVolumeClaim
func (c *K8sClient) ListPVCs(ctx context.Context, namespace string) (*corev1.PersistentVolumeClaimList, error) {
	result, err := c.clientset.CoreV1().PersistentVolumeClaims(namespace).List(ctx, metav1.ListOptions{})
	if err != nil {
		return nil, fmt.Errorf("list pvcs fail: %s", err.Error())
	}
	return result, nil
}

// CreatePVC 创建PersistentVolumeClaim
func (c *K8sClient) CreatePVC(ctx context.Context, namespace string, pvc *corev1.PersistentVolumeClaim) (*corev1.PersistentVolumeClaim, error) {
	result, err := c.clientset.CoreV1().PersistentVolumeClaims(namespace).Create(ctx, pvc, metav1.CreateOptions{})
	if err != nil {
		return nil, fmt.Errorf("create pvc fail: %s", err.Error())
	}
	return result, nil
}

// DeletePVC 删除PersistentVolumeClaim
func (c *K8sClient) DeletePVC(ctx context.Context, namespace, name string) error {
	err := c.clientset.CoreV1().PersistentVolumeClaims(namespace).Delete(ctx, name, metav1.DeleteOptions{})
	if err != nil {
		return fmt.Errorf("delete pvc fail: %s", err.Error())
	}
	return nil
}

// ========== Namespace 相关方法 ==========

// GetNamespace 获取Namespace详情
func (c *K8sClient) GetNamespace(ctx context.Context, name string) (*corev1.Namespace, error) {
	result, err := c.clientset.CoreV1().Namespaces().Get(ctx, name, metav1.GetOptions{})
	if err != nil {
		return nil, fmt.Errorf("get namespace fail: %s", err.Error())
	}
	return result, nil
}

// ListNamespaces 列出所有Namespace
func (c *K8sClient) ListNamespaces(ctx context.Context) (*corev1.NamespaceList, error) {
	result, err := c.clientset.CoreV1().Namespaces().List(ctx, metav1.ListOptions{})
	if err != nil {
		return nil, fmt.Errorf("list namespaces fail: %s", err.Error())
	}
	return result, nil
}

// CreateNamespace 创建Namespace
func (c *K8sClient) CreateNamespace(ctx context.Context, ns *corev1.Namespace) (*corev1.Namespace, error) {
	result, err := c.clientset.CoreV1().Namespaces().Create(ctx, ns, metav1.CreateOptions{})
	if err != nil {
		return nil, fmt.Errorf("create namespace fail: %s", err.Error())
	}
	return result, nil
}

// DeleteNamespace 删除Namespace
func (c *K8sClient) DeleteNamespace(ctx context.Context, name string) error {
	err := c.clientset.CoreV1().Namespaces().Delete(ctx, name, metav1.DeleteOptions{})
	if err != nil {
		return fmt.Errorf("delete namespace fail: %s", err.Error())
	}
	return nil
}

// ========== Deployment 相关方法 ==========

// GetDeployment 获取Deployment详情
func (c *K8sClient) GetDeployment(ctx context.Context, namespace, name string) (*appsv1.Deployment, error) {
	result, err := c.clientset.AppsV1().Deployments(namespace).Get(ctx, name, metav1.GetOptions{})
	if err != nil {
		return nil, fmt.Errorf("get deployment fail: %s", err.Error())
	}
	return result, nil
}

// ListDeployments 列出命名空间下的所有Deployment
func (c *K8sClient) ListDeployments(ctx context.Context, namespace string) (*appsv1.DeploymentList, error) {
	result, err := c.clientset.AppsV1().Deployments(namespace).List(ctx, metav1.ListOptions{})
	if err != nil {
		return nil, fmt.Errorf("list deployments fail: %s", err.Error())
	}
	return result, nil
}

// CreateDeployment 创建Deployment
func (c *K8sClient) CreateDeployment(ctx context.Context, namespace string, deployment *appsv1.Deployment) (*appsv1.Deployment, error) {
	result, err := c.clientset.AppsV1().Deployments(namespace).Create(ctx, deployment, metav1.CreateOptions{})
	if err != nil {
		return nil, fmt.Errorf("create deployment fail: %s", err.Error())
	}
	return result, nil
}

// UpdateDeployment 更新Deployment
func (c *K8sClient) UpdateDeployment(ctx context.Context, namespace string, deployment *appsv1.Deployment) (*appsv1.Deployment, error) {
	result, err := c.clientset.AppsV1().Deployments(namespace).Update(ctx, deployment, metav1.UpdateOptions{})
	if err != nil {
		return nil, fmt.Errorf("update deployment fail: %s", err.Error())
	}
	return result, nil
}

// DeleteDeployment 删除Deployment
func (c *K8sClient) DeleteDeployment(ctx context.Context, namespace, name string) error {
	err := c.clientset.AppsV1().Deployments(namespace).Delete(ctx, name, metav1.DeleteOptions{})
	if err != nil {
		return fmt.Errorf("delete deployment fail: %s", err.Error())
	}
	return nil
}

// ========== 资源存在性检查方法 ==========

// ServiceExists 检查Service是否存在
func (c *K8sClient) ServiceExists(ctx context.Context, namespace, name string) (bool, error) {
	_, err := c.GetService(ctx, namespace, name)
	if err != nil {
		if errors.IsNotFound(err) {
			return false, nil
		}
		return false, err
	}
	return true, nil
}

// StatefulSetExists 检查StatefulSet是否存在
func (c *K8sClient) StatefulSetExists(ctx context.Context, namespace, name string) (bool, error) {
	_, err := c.GetStatefulSet(ctx, namespace, name)
	if err != nil {
		if errors.IsNotFound(err) {
			return false, nil
		}
		return false, err
	}
	return true, nil
}

// PodExists 检查Pod是否存在
func (c *K8sClient) PodExists(ctx context.Context, namespace, name string) (bool, error) {
	_, err := c.GetPod(ctx, namespace, name)
	if err != nil {
		if errors.IsNotFound(err) {
			return false, nil
		}
		return false, err
	}
	return true, nil
}

// ========== 幂等操作方法 ==========

// CreateOrUpdateService 创建或更新Service（幂等操作）
// 如果Service已存在，则更新；不存在则创建
func (c *K8sClient) CreateOrUpdateService(ctx context.Context, namespace string, service *corev1.Service) (*corev1.Service, error) {
	existing, err := c.clientset.CoreV1().Services(namespace).Get(ctx, service.Name, metav1.GetOptions{})
	if err != nil {
		if errors.IsNotFound(err) {
			// 不存在，创建新的
			result, createErr := c.clientset.CoreV1().Services(namespace).Create(ctx, service, metav1.CreateOptions{})
			if createErr != nil {
				return nil, fmt.Errorf("create service fail: %s", createErr.Error())
			}
			return result, nil
		}
		return nil, fmt.Errorf("get service fail: %s", err.Error())
	}

	// 已存在，更新
	service.ResourceVersion = existing.ResourceVersion
	service.Spec.ClusterIP = existing.Spec.ClusterIP // 保留现有的 ClusterIP
	result, err := c.clientset.CoreV1().Services(namespace).Update(ctx, service, metav1.UpdateOptions{})
	if err != nil {
		return nil, fmt.Errorf("update service fail: %s", err.Error())
	}
	return result, nil
}

// CreateOrUpdateStatefulSet 创建或更新StatefulSet（幂等操作）
// 如果StatefulSet已存在，则更新；不存在则创建
func (c *K8sClient) CreateOrUpdateStatefulSet(ctx context.Context, namespace string, sts *appsv1.StatefulSet) (*appsv1.StatefulSet, error) {
	existing, err := c.clientset.AppsV1().StatefulSets(namespace).Get(ctx, sts.Name, metav1.GetOptions{})
	if err != nil {
		if errors.IsNotFound(err) {
			// 不存在，创建新的
			result, createErr := c.clientset.AppsV1().StatefulSets(namespace).Create(ctx, sts, metav1.CreateOptions{})
			if createErr != nil {
				return nil, fmt.Errorf("create statefulset fail: %s", createErr.Error())
			}
			return result, nil
		}
		return nil, fmt.Errorf("get statefulset fail: %s", err.Error())
	}

	// 已存在，更新
	sts.ResourceVersion = existing.ResourceVersion
	result, err := c.clientset.AppsV1().StatefulSets(namespace).Update(ctx, sts, metav1.UpdateOptions{})
	if err != nil {
		return nil, fmt.Errorf("update statefulset fail: %s", err.Error())
	}
	return result, nil
}
