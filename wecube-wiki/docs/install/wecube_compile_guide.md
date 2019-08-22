# WeCube编译指南

## 编译前准备
1. 准备一台linux主机,为加快编译速度，资源配置建议4核8G或以上;
2. 操作系统版本可以为ubuntu16.04以上或centos7.3以上。
3. 建议网络可通外网(需从外网下载文件)。
4. 安装git
   - yum 安装
   ```
   yum install -y git
   ```
   - 手动安装，请参考[git安装文档](https://github.com/WeBankPartners/we-cmdb/blob/master/cmdb-wiki/docs/install/git_install_guide.md)
5. 安装docker1.17.03.x以上版本，安装参考[docker安装文档](https://github.com/WeBankPartners/we-cmdb/blob/master/cmdb-wiki/docs/install/docker_install_guide.md)

## 编译过程
1. 通过github拉取WeCube代码
```
git clone https://github.com/WeBankPartners/wecube-platform.git
```
2. 进入代码目录，执行make build 等待编译完成，make build过程中会从外网拉取maven包和npm包，如果有更快的mvn源，建议修改build目录下的maven_setting.xml文件。
3. 编译完成后，执行make image，将制作WeCube的运行镜像。

