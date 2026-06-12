#!/bin/bash
set -e -x
cd $(dirname $0)/../
echo "--- Running inside ARM64 container ---"

# 我们可以通过 uname -m 验证, 这将输出 aarch64
echo "Current architecture: $(uname -m)"

# 确保 CGO 启用, 您的 -linkmode external 标志需要它
export CGO_ENABLED=1

echo "Starting Go build..."

# 直接运行您的构建命令。
# Go 编译器会调用容器内的 arm64-gcc 来完成静态链接
go build -ldflags "-linkmode external -extldflags -static -s"

echo "--- Build successful! ---"