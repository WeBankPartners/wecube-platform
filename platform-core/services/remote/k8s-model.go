package remote

// k8s-model.go 提供辅助函数和构建器，简化 k8s 资源的创建
// 所有的 k8s 资源类型都使用官方的 k8s.io/api 包

import (
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
)

// ServiceBuilder Service构建器，简化Service创建
type ServiceBuilder struct {
	service *corev1.Service
}

// NewServiceBuilder 创建Service构建器
func NewServiceBuilder(name, namespace string) *ServiceBuilder {
	return &ServiceBuilder{
		service: &corev1.Service{
			ObjectMeta: metav1.ObjectMeta{
				Name:      name,
				Namespace: namespace,
				Labels:    make(map[string]string),
			},
			Spec: corev1.ServiceSpec{
				Selector: make(map[string]string),
				Ports:    []corev1.ServicePort{},
			},
		},
	}
}

// WithLabels 设置标签
func (b *ServiceBuilder) WithLabels(labels map[string]string) *ServiceBuilder {
	b.service.Labels = labels
	return b
}

// WithSelector 设置选择器
func (b *ServiceBuilder) WithSelector(selector map[string]string) *ServiceBuilder {
	b.service.Spec.Selector = selector
	return b
}

// WithType 设置Service类型
func (b *ServiceBuilder) WithType(serviceType corev1.ServiceType) *ServiceBuilder {
	b.service.Spec.Type = serviceType
	return b
}

// AddPort 添加端口
func (b *ServiceBuilder) AddPort(name string, protocol corev1.Protocol, port, targetPort int32) *ServiceBuilder {
	b.service.Spec.Ports = append(b.service.Spec.Ports, corev1.ServicePort{
		Name:       name,
		Protocol:   protocol,
		Port:       port,
		TargetPort: intstr.FromInt(int(targetPort)),
	})
	return b
}

// AddNodePort 添加NodePort端口
func (b *ServiceBuilder) AddNodePort(name string, protocol corev1.Protocol, port, targetPort, nodePort int32) *ServiceBuilder {
	b.service.Spec.Ports = append(b.service.Spec.Ports, corev1.ServicePort{
		Name:       name,
		Protocol:   protocol,
		Port:       port,
		TargetPort: intstr.FromInt(int(targetPort)),
		NodePort:   nodePort,
	})
	return b
}

// Build 构建Service对象
func (b *ServiceBuilder) Build() *corev1.Service {
	return b.service
}

// StatefulSetBuilder StatefulSet构建器
type StatefulSetBuilder struct {
	sts *appsv1.StatefulSet
}

// NewStatefulSetBuilder 创建StatefulSet构建器
func NewStatefulSetBuilder(name, namespace string, replicas int32) *StatefulSetBuilder {
	return &StatefulSetBuilder{
		sts: &appsv1.StatefulSet{
			ObjectMeta: metav1.ObjectMeta{
				Name:      name,
				Namespace: namespace,
				Labels:    make(map[string]string),
			},
			Spec: appsv1.StatefulSetSpec{
				Replicas:    &replicas,
				ServiceName: name,
				Selector: &metav1.LabelSelector{
					MatchLabels: make(map[string]string),
				},
				Template: corev1.PodTemplateSpec{
					ObjectMeta: metav1.ObjectMeta{
						Labels: make(map[string]string),
					},
					Spec: corev1.PodSpec{
						Containers: []corev1.Container{},
					},
				},
			},
		},
	}
}

// WithLabels 设置标签
func (b *StatefulSetBuilder) WithLabels(labels map[string]string) *StatefulSetBuilder {
	b.sts.Labels = labels
	return b
}

// WithSelector 设置选择器
func (b *StatefulSetBuilder) WithSelector(selector map[string]string) *StatefulSetBuilder {
	b.sts.Spec.Selector.MatchLabels = selector
	return b
}

// WithPodLabels 设置Pod标签
func (b *StatefulSetBuilder) WithPodLabels(labels map[string]string) *StatefulSetBuilder {
	b.sts.Spec.Template.Labels = labels
	return b
}

// WithServiceName 设置Headless Service名称
func (b *StatefulSetBuilder) WithServiceName(serviceName string) *StatefulSetBuilder {
	b.sts.Spec.ServiceName = serviceName
	return b
}

// WithImagePullSecrets 设置镜像拉取Secret
// secretNames: Secret 名称列表，例如: []string{"my-registry-secret"}
func (b *StatefulSetBuilder) WithImagePullSecrets(secretNames ...string) *StatefulSetBuilder {
	secrets := make([]corev1.LocalObjectReference, len(secretNames))
	for i, name := range secretNames {
		secrets[i] = corev1.LocalObjectReference{Name: name}
	}
	b.sts.Spec.Template.Spec.ImagePullSecrets = secrets
	return b
}

// AddImagePullSecret 添加单个镜像拉取Secret
func (b *StatefulSetBuilder) AddImagePullSecret(secretName string) *StatefulSetBuilder {
	b.sts.Spec.Template.Spec.ImagePullSecrets = append(
		b.sts.Spec.Template.Spec.ImagePullSecrets,
		corev1.LocalObjectReference{Name: secretName},
	)
	return b
}

// AddContainer 添加容器
func (b *StatefulSetBuilder) AddContainer(container corev1.Container) *StatefulSetBuilder {
	b.sts.Spec.Template.Spec.Containers = append(b.sts.Spec.Template.Spec.Containers, container)
	return b
}

// Build 构建StatefulSet对象
func (b *StatefulSetBuilder) Build() *appsv1.StatefulSet {
	return b.sts
}

// ContainerBuilder 容器构建器
type ContainerBuilder struct {
	container *corev1.Container
}

// NewContainerBuilder 创建容器构建器
func NewContainerBuilder(name, image string) *ContainerBuilder {
	return &ContainerBuilder{
		container: &corev1.Container{
			Name:  name,
			Image: image,
			Ports: []corev1.ContainerPort{},
			Env:   []corev1.EnvVar{},
		},
	}
}

// WithImagePullPolicy 设置镜像拉取策略
func (b *ContainerBuilder) WithImagePullPolicy(policy corev1.PullPolicy) *ContainerBuilder {
	b.container.ImagePullPolicy = policy
	return b
}

// AddPort 添加容器端口
func (b *ContainerBuilder) AddPort(name string, protocol corev1.Protocol, containerPort int32) *ContainerBuilder {
	b.container.Ports = append(b.container.Ports, corev1.ContainerPort{
		Name:          name,
		Protocol:      protocol,
		ContainerPort: containerPort,
	})
	return b
}

// AddEnv 添加环境变量
func (b *ContainerBuilder) AddEnv(name, value string) *ContainerBuilder {
	b.container.Env = append(b.container.Env, corev1.EnvVar{
		Name:  name,
		Value: value,
	})
	return b
}

// AddEnvFromField 从字段引用添加环境变量（例如: metadata.name, metadata.namespace）
func (b *ContainerBuilder) AddEnvFromField(name, value string) *ContainerBuilder {
	b.container.Env = append(b.container.Env, corev1.EnvVar{
		Name: name,
		ValueFrom: &corev1.EnvVarSource{
			FieldRef: &corev1.ObjectFieldSelector{
				FieldPath: value,
			},
		},
	})
	return b
}

// AddEnvFromSecret 从Secret添加环境变量
// name: 环境变量名称
// secretName: Secret 名称
// secretKey: Secret 中的 key
func (b *ContainerBuilder) AddEnvFromSecret(name, secretName, secretKey string) *ContainerBuilder {
	b.container.Env = append(b.container.Env, corev1.EnvVar{
		Name: name,
		ValueFrom: &corev1.EnvVarSource{
			SecretKeyRef: &corev1.SecretKeySelector{
				LocalObjectReference: corev1.LocalObjectReference{
					Name: secretName,
				},
				Key: secretKey,
			},
		},
	})
	return b
}

// AddEnvFromConfigMap 从ConfigMap添加环境变量
// name: 环境变量名称
// configMapName: ConfigMap 名称
// configMapKey: ConfigMap 中的 key
func (b *ContainerBuilder) AddEnvFromConfigMap(name, configMapName, configMapKey string) *ContainerBuilder {
	b.container.Env = append(b.container.Env, corev1.EnvVar{
		Name: name,
		ValueFrom: &corev1.EnvVarSource{
			ConfigMapKeyRef: &corev1.ConfigMapKeySelector{
				LocalObjectReference: corev1.LocalObjectReference{
					Name: configMapName,
				},
				Key: configMapKey,
			},
		},
	})
	return b
}

// AddVolumeMount 添加卷挂载
func (b *ContainerBuilder) AddVolumeMount(name, mountPath string) *ContainerBuilder {
	b.container.VolumeMounts = append(b.container.VolumeMounts, corev1.VolumeMount{
		Name:      name,
		MountPath: mountPath,
	})
	return b
}

// WithResources 设置资源配置
func (b *ContainerBuilder) WithResources(limits, requests corev1.ResourceList) *ContainerBuilder {
	b.container.Resources = corev1.ResourceRequirements{
		Limits:   limits,
		Requests: requests,
	}
	return b
}

// Build 构建Container对象
func (b *ContainerBuilder) Build() corev1.Container {
	return *b.container
}
