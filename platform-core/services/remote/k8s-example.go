package remote

// 使用示例：创建 StatefulSet（带 PVC）和 Service（ClusterIP 多端口）

/*
## 完整示例：创建 StatefulSet 和 Service

### 场景说明
创建一个完整的应用部署，包括：
1. StatefulSet：3副本，每个 Pod 带一个持久化存储卷
2. Service：ClusterIP 类型，暴露多个 TCP 端口

### 代码示例

package main

import (
	"context"
	"fmt"

	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/resource"
)

func main() {
	// 1. 创建 k8s 客户端
	client, err := remote.NewK8sClient(
		"https://kubernetes.default.svc:443", // K8s API 地址
		"your-bearer-token",                   // 认证 token
	)
	if err != nil {
		panic(fmt.Errorf("create k8s client fail: %v", err))
	}

	ctx := context.Background()

	// 2. 创建 Service（ClusterIP 类型，多个 TCP 端口）
	service := remote.NewServiceBuilder("my-app-service", "default").
		WithLabels(map[string]string{
			"app":     "myapp",
			"version": "v1",
		}).
		WithSelector(map[string]string{
			"app": "myapp", // 选择器匹配 StatefulSet 的 Pod 标签
		}).
		WithType(corev1.ServiceTypeClusterIP). // ClusterIP 类型
		AddPort("http", corev1.ProtocolTCP, 80, 8080).    // 端口1: http 服务
		AddPort("grpc", corev1.ProtocolTCP, 9090, 9090).  // 端口2: grpc 服务
		AddPort("metrics", corev1.ProtocolTCP, 9100, 9100). // 端口3: metrics 端口
		Build()

	createdSvc, err := client.CreateService(ctx, "default", service)
	if err != nil {
		panic(fmt.Errorf("create service fail: %v", err))
	}
	fmt.Printf("Service created: %s\n", createdSvc.Name)

	// 3. 创建容器配置
	container := remote.NewContainerBuilder("myapp", "nginx:1.21").
		WithImagePullPolicy(corev1.PullIfNotPresent).
		AddPort("http", corev1.ProtocolTCP, 8080).    // 容器端口1
		AddPort("grpc", corev1.ProtocolTCP, 9090).    // 容器端口2
		AddPort("metrics", corev1.ProtocolTCP, 9100). // 容器端口3
		AddEnv("APP_ENV", "production").
		AddEnv("LOG_LEVEL", "info").
		AddVolumeMount("data", "/var/lib/myapp"). // 挂载持久化卷
		WithResources(
			// Limits（资源上限）
			corev1.ResourceList{
				corev1.ResourceCPU:    resource.MustParse("2000m"),
				corev1.ResourceMemory: resource.MustParse("2Gi"),
			},
			// Requests（资源请求）
			corev1.ResourceList{
				corev1.ResourceCPU:    resource.MustParse("500m"),
				corev1.ResourceMemory: resource.MustParse("512Mi"),
			},
		).
		Build()

	// 4. 创建 StatefulSet（带 PVC 模板）
	sts := remote.NewStatefulSetBuilder("my-app", "default", 3). // 3个副本
		WithLabels(map[string]string{
			"app":     "myapp",
			"version": "v1",
		}).
		WithSelector(map[string]string{
			"app": "myapp",
		}).
		WithPodLabels(map[string]string{
			"app": "myapp", // Pod 标签要匹配 Service 的 Selector
		}).
		WithServiceName("my-app-service"). // 关联到 Headless Service（StatefulSet 必需）
		AddContainer(container).
		Build()

	// 5. 为 StatefulSet 添加 VolumeClaimTemplate（持久化卷声明模板）
	// 注意：Builder 模式暂不支持 VolumeClaimTemplate，需要手动添加
	sts.Spec.VolumeClaimTemplates = []corev1.PersistentVolumeClaim{
		{
			ObjectMeta: metav1.ObjectMeta{
				Name: "data", // 这个名称要和容器中的 VolumeMount 名称一致
			},
			Spec: corev1.PersistentVolumeClaimSpec{
				AccessModes: []corev1.PersistentVolumeAccessMode{
					corev1.ReadWriteOnce, // RWO 模式
				},
				Resources: corev1.ResourceRequirements{
					Requests: corev1.ResourceList{
						corev1.ResourceStorage: resource.MustParse("10Gi"), // 每个 Pod 分配 10Gi 存储
					},
				},
				// 可选：指定 StorageClass
				// StorageClassName: pointer.String("fast-ssd"),
			},
		},
	}

	// 6. 创建 StatefulSet
	createdSts, err := client.CreateStatefulSet(ctx, "default", sts)
	if err != nil {
		panic(fmt.Errorf("create statefulset fail: %v", err))
	}
	fmt.Printf("StatefulSet created: %s with %d replicas\n", createdSts.Name, *createdSts.Spec.Replicas)

	// 7. 查询创建的资源
	fmt.Println("\n=== 查询创建的资源 ===")

	// 查询 Service
	svc, err := client.GetService(ctx, "default", "my-app-service")
	if err != nil {
		panic(err)
	}
	fmt.Printf("Service: %s, Type: %s, Ports: %d\n",
		svc.Name, svc.Spec.Type, len(svc.Spec.Ports))

	// 查询 StatefulSet
	sts, err = client.GetStatefulSet(ctx, "default", "my-app")
	if err != nil {
		panic(err)
	}
	fmt.Printf("StatefulSet: %s, Replicas: %d/%d\n",
		sts.Name, sts.Status.ReadyReplicas, *sts.Spec.Replicas)

	// 8. 更新 StatefulSet 副本数（可选）
	// *sts.Spec.Replicas = 5
	// updatedSts, err := client.UpdateStatefulSet(ctx, "default", sts)
	// if err != nil {
	// 	panic(err)
	// }
	// fmt.Printf("StatefulSet updated to %d replicas\n", *updatedSts.Spec.Replicas)

	// 9. 删除资源（清理）
	// 注意：StatefulSet 删除后，PVC 不会自动删除，需要手动删除
	// err = client.DeleteStatefulSet(ctx, "default", "my-app")
	// if err != nil {
	// 	panic(err)
	// }
	// fmt.Println("StatefulSet deleted")

	// err = client.DeleteService(ctx, "default", "my-app-service")
	// if err != nil {
	// 	panic(err)
	// }
	// fmt.Println("Service deleted")

	fmt.Println("\n=== 部署完成 ===")
}

// 注意事项：
// 1. StatefulSet 需要一个 Headless Service（clusterIP: None）来进行网络标识
//    如果需要，可以额外创建一个 Headless Service：
//
//    headlessSvc := &corev1.Service{
//        ObjectMeta: metav1.ObjectMeta{
//            Name:      "my-app",
//            Namespace: "default",
//        },
//        Spec: corev1.ServiceSpec{
//            ClusterIP: "None", // Headless
//            Selector: map[string]string{
//                "app": "myapp",
//            },
//            Ports: []corev1.ServicePort{
//                {
//                    Name:     "http",
//                    Protocol: corev1.ProtocolTCP,
//                    Port:     8080,
//                },
//            },
//        },
//    }
//    client.CreateService(ctx, "default", headlessSvc)
//
// 2. PVC 会根据 VolumeClaimTemplate 自动创建，命名规则：
//    <volumeClaimTemplate.name>-<statefulset.name>-<pod-index>
//    例如：data-my-app-0, data-my-app-1, data-my-app-2
//
// 3. 如果未指定 StorageClassName，会使用集群的默认 StorageClass
//
// 4. StatefulSet 删除时不会自动删除 PVC，需要手动清理：
//    clientset := client.GetClientset()
//    pvcList, _ := clientset.CoreV1().PersistentVolumeClaims("default").List(ctx, metav1.ListOptions{
//        LabelSelector: "app=myapp",
//    })
//    for _, pvc := range pvcList.Items {
//        clientset.CoreV1().PersistentVolumeClaims("default").Delete(ctx, pvc.Name, metav1.DeleteOptions{})
//    }

*/
