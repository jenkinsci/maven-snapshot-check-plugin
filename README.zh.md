# Maven SNAPSHOT Check Plugin

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins%2Fmaven-snapshot-check-plugin%2Fmaster)](https://ci.jenkins.io/job/Plugins/job/maven-snapshot-check-plugin/job/master/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/maven-snapshot-check.svg)](https://plugins.jenkins.io/maven-snapshot-check)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/maven-snapshot-check-plugin.svg?label=changelog)](https://github.com/jenkinsci/maven-snapshot-check-plugin/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/maven-snapshot-check.svg?color=blue)](https://plugins.jenkins.io/maven-snapshot-check)

该插件用来检查 pom.xml 是否包含 SNAPSHOT。


# 使用

## 自由风格 job 使用

在 `构建` 区域，增加 `Maven SNAPSHOT Check` 构建步骤

![add-build-step](images/add-build-step.png)

如果勾选了复选框，它将检查 pom.xml 中是否包含 SNAPSHOT。

![maven-snapshot-check-plugin-usage](images/maven-snapshot-check-plugin-usage.png)

如果匹配，该次构建将被标记为失败。

![job-build-console-output](images/job-build-console-output.png)

## Maven job 使用

在 `Pre Steps` 区域，增加 `Maven SNAPSHOT Check` 步骤
![pre-build-step](images/pre-build-step.png)

## 流水线 job 使用
```
step([$class: 'MavenSnapshotCheck', check: 'true'])
```
或者
```
mavenSnapshotCheck check: 'true'
```
或者，可以自定义 pomFiles 参数，用来只对指定的 pom 文件进行检查
（如果未定义 pomFiles 参数，它的默认值是 `pom.xml,**/pom.xml`）
```
mavenSnapshotCheck check: 'true', pomFiles: 'pom.xml,sub-dir/pom.xml'
```

# Bug 报告
请在 https://github.com/jenkinsci/maven-snapshot-check-plugin/issues 提交 bug 报告或新功能请求。

# 如何构建及测试？
* 构建插件：

`mvn package`

* 本地测试（调用本地附带了该插件的 Jenkins 实例）；

`mvn hpi:run`

更多详情请参考 https://jenkinsci.github.io/maven-hpi-plugin/ 。

# 相关博客
- [Jenkins 插件开发之旅：两天内从 idea 到发布 (上篇)](https://xie.infoq.cn/article/c398bb482db91bada8e40b5c8)
- [Jenkins 插件开发之旅：两天内从 idea 到发布 (下篇)](https://xie.infoq.cn/article/d394779a53d5cbd5fcfb97860)
