# Maven SNAPSHOT Check Plugin

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins%2Fmaven-snapshot-check-plugin%2Fmaster)](https://ci.jenkins.io/job/Plugins/job/maven-snapshot-check-plugin/job/master/)
[![codecov](https://codecov.io/gh/jenkinsci/maven-snapshot-check-plugin/branch/master/graph/badge.svg?token=f6Wnfxauy7)](https://codecov.io/gh/jenkinsci/maven-snapshot-check-plugin)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/maven-snapshot-check.svg)](https://plugins.jenkins.io/maven-snapshot-check)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/maven-snapshot-check-plugin.svg?label=changelog)](https://github.com/jenkinsci/maven-snapshot-check-plugin/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/maven-snapshot-check.svg?color=blue)](https://plugins.jenkins.io/maven-snapshot-check)

[README 中文版](README.zh.md)

This plugin  used to check if pom.xml contains SNAPSHOT.


# Usage

## FreeStyle job usage

use the `Build Step` called `Maven SNAPSHOT Check` in `Build` section

![add-build-step](images/add-build-step.png)

If check the checkbox, it will check if pom.xml contains SNAPSHOT. 

![maven-snapshot-check-plugin-usage](images/maven-snapshot-check-plugin-usage.png)

Then it will marked the build failed if matches.

![job-build-console-output](images/job-build-console-output.png)

## Maven job usage

use the `pre-build step` called `Maven SNAPSHOT Check` in `Pre Steps` section
![pre-build-step](images/pre-build-step.png)

## Pipeline  job usage
```
step([$class: 'MavenSnapshotCheck', check: 'true'])
```
or
```
mavenSnapshotCheck check: 'true'
```
or, you can customize the pomFiles parameter to check only the specified pom files 
(If the pomFiles parameter is not defined, its default value is `pom.xml, * */pom.xml`)
```
mavenSnapshotCheck check: 'true', pomFiles: 'pom.xml,sub-dir/pom.xml'
```

# Bug reports
Please report bugs and feature requests at https://github.com/jenkinsci/maven-snapshot-check-plugin/issues.

# How to build and test
* Build the plugin:

`mvn package`

* Test locally (invokes a local Jenkins instance with the plugin installed):

`mvn hpi:run`

See https://jenkinsci.github.io/maven-hpi-plugin/ for details.
