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
step([$class: 'MavenCheck', check: 'true'])
```
or
```
mavenSNAPSHOTCheck check: 'true'
```

# Issues
The issues from [here](https://issues.jenkins-ci.org/issues/?jql=component%20%3D%20maven-snapshot-check-plugin).

