def jenkinsVersion = '2.277.4'

def configurations = [
  [ platform: "linux", jdk: "8", jenkins: null ],
  [ platform: "linux", jdk: "8", jenkins: jenkinsVersion ],
  [ platform: "linux", jdk: "11", jenkins: jenkinsVersion ]
]

buildPlugin(configurations: configurations, timeout: 180)
