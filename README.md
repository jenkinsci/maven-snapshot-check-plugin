# Maven SNAPSHOT Check Plugin

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins%2Fmaven-snapshot-check-plugin%2Fmaster)](https://ci.jenkins.io/job/Plugins/job/maven-snapshot-check-plugin/job/master/)

This plugin  used to check if pom.xml contains SNAPSHOT.


# Usage

## freestyle job usage
If check the checkbox, it will check if pom.xml contains SNAPSHOT. 

![](images/maven-snapshot-check-plugin-usage.png)

Then it will marked the build failed if matches.

![](images/job-build-console-output.png)

## pipeline job usage
```
step([$class: 'MavenSnapshotCheck', check: 'true'])
```
or
```
mavenSnapshotCheck check: 'true'
```

# Bug reports
Please report bugs and feature requests at https://github.com/jenkinsci/maven-snapshot-check-plugin/issues.
