project {
  modelVersion '4.0.0'
  groupId 'com.github.yassine'
  artifactId 'soxy-chains'
  version '0.1.0-SNAPSHOT'
  licenses {
    license {
      name 'The Apache License, Version 2.0'
      url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
    }
  }
  properties{
    'project.build.sourceEncoding' 'UTF-8'
    'sonar.jacoco.reportPaths' '${project.build.directory}/coverage-reports/jacoco-ut.exec'
    'sonar.tests' '${project.basedir}/src/test/unit-tests,${project.basedir}/src/test/functional-tests'
    'sonar.links.homepage' 'https://github.com/yassine/soxy-chains'
    'sonar.links.scm' 'https://github.com/yassine/soxy-chains'
    'sonar.links.issue' 'https://github.com/yassine/soxy-chains'
    'sonar.projectName' 'soxy-chains'
    'sonar.projectVersion' '${project.version}'
    'sonar.projectKey' 'com.github.yassine:soxy-chains'
    'version.undertow' '2.0.4.Final'
    'version.jersey' '2.22.2'
  }
  dependencies {
    dependency 'com.ecwid.consul:consul-api:1.3.1'
    dependency 'com.fasterxml.jackson.core:jackson-annotations:2.8.5'
    dependency 'com.fasterxml.jackson.core:jackson-core:2.8.5'
    dependency 'com.fasterxml.jackson.core:jackson-databind:2.8.5'
    dependency 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.8.5'
    dependency 'org.glassfish.jersey.media:jersey-media-json-jackson:${version.jersey}'
    dependency{
      groupId 'com.github.docker-java'
      artifactId 'docker-java'
      version '3.0.14'
      exclusions{
        exclusion{
          groupId 'log4j'
          artifactId 'log4j'
        }
      }
    }
    dependency 'com.github.yassine:gobetween-java-client:0.1.2'
    dependency 'com.github.yassine:guice-artifacts:0.2.1'
    dependency 'com.google.auto.service:auto-service:1.0-rc3'
    dependency 'com.google.guava:guava:24.1-jre'
    dependency 'com.google.inject.extensions:guice-multibindings:4.1.0'
    dependency 'com.google.inject.extensions:guice-servlet:4.2.0'
    dependency 'com.google.inject:guice:4.2.0'
    dependency 'io.airlift:airline:0.8'
    dependency 'io.github.lukehutch:fast-classpath-scanner:2.0.8'
    dependency 'io.reactivex.rxjava2:rxjava:2.1.12'
    dependency 'net.jodah:typetools:0.5.0'
    dependency 'org.glassfish:javax.el:3.0.1-b08'
    dependency 'org.hibernate.validator:hibernate-validator:6.0.5.Final'
    dependency 'org.projectlombok:lombok:1.16.18:provided'
    dependency 'org.rapidoid:rapidoid-http-server:5.5.4'
    dependency{
      groupId 'com.machinezoo.noexception'
      artifactId 'noexception'
      version '1.2.0'
      exclusions{
        exclusion{
          groupId 'org.slf4j'
          artifactId 'slf4j-api'
        }
      }
    }
    //logging
    dependency 'org.apache.logging.log4j:log4j-api:2.10.0'
    dependency 'org.apache.logging.log4j:log4j-core:2.10.0'
    dependency 'org.apache.logging.log4j:log4j-slf4j-impl:2.10.0'
    dependency 'org.apache.logging.log4j:log4j-1.2-api:2.10.0'
    dependency 'org.slf4j:slf4j-api:1.7.21'
    //test
    dependency 'cglib:cglib-nodep:3.2.5:test'
    dependency 'com.jayway.jsonpath:json-path:2.4.0:test'
    dependency 'com.squareup.okhttp3:okhttp:3.10.0:test'
    dependency 'io.rest-assured:rest-assured:3.1.0:test'
    dependency 'junit:junit:4.12:test'
    dependency 'org.apache.commons:commons-csv:1.5:test'
    dependency 'org.assertj:assertj-core:3.9.0:test'
    dependency 'org.codehaus.groovy:groovy-all:2.4.13:test'
    dependency 'org.spockframework:spock-core:1.1-groovy-2.4:test'
    dependency 'org.spockframework:spock-guice:1.1-groovy-2.4:test'
  }
  build {
    pluginManagement {
      plugins {
        plugin {
          artifactId 'maven-compiler-plugin'
          version '3.7.0'
          configuration {
            source '8'
            target '8'
          }
        }
      }
    }
    plugins {
      plugin {
        artifactId 'maven-compiler-plugin'
      }
      plugin {
        groupId 'org.jacoco'
        artifactId 'jacoco-maven-plugin'
        version '0.8.0'
        executions {
          execution {
            id 'prepare-agent'
            phase 'test-compile'
            goals {
              goal 'prepare-agent'
            }
            configuration {
              propertyName 'surefireArgLine'
              destFile '${project.build.directory}/coverage-reports/jacoco-ut.exec'
              excludes {
                exclude '**/SoxyChainsDockerClientSupport.class'
              }
            }
          }
          execution {
            id 'post-test-reports'
            phase 'post-integration-test'
            goals {
              goal 'report'
            }
            configuration {
              dataFile '${project.build.directory}/coverage-reports/jacoco-ut.exec'
              outputDirectory '${project.reporting.outputDirectory}/code-coverage'
              excludes {
                exclude '**/SoxyChainsDockerClientSupport.class'
              }
            }
          }
        }
      }
      plugin {
        groupId 'org.codehaus.gmavenplus'
        artifactId 'gmavenplus-plugin'
        version '1.6'
        executions {
          execution {
            id 'generate-unit-tests'
            goals {
              goal 'compileTests'
              goal 'addTestSources'
            }
            configuration {
              testSources {
                testSource {
                  directory '${project.basedir}/src/test/unit-tests'
                  includes {
                    include '**/*.groovy'
                  }
                }
              }
              outputDirectory '${project.build.directory}/unit-tests'
            }
          }
          execution {
            id 'generate-functional-tests'
            goals {
              goal 'compileTests'
              goal 'addTestSources'
            }
            configuration {
              testSources {
                testSource {
                  directory '${project.basedir}/src/test/functional-tests'
                  includes {
                    include '**/*.groovy'
                  }
                }
              }
              outputDirectory '${project.build.directory}/functional-tests'
            }
          }
        }
      }
      plugin {
        artifactId 'maven-surefire-plugin'
        version '2.20.1'
        executions {
          execution {
            id 'functional-tests'
            goals {
              goal 'test'
            }
            configuration {
              testClassesDirectory '${project.build.directory}/functional-tests'
            }
          }
        }
        configuration{
          useFile 'false'
          testClassesDirectory '${project.build.directory}/unit-tests'
          includes {
            include '**/*Spec'
          }
          additionalClasspathElements {
            additionalClasspathElement '${project.build.testOutputDirectory}'
            additionalClasspathElement '${project.basedir}/src/test/resources'
          }
          argLine '${surefireArgLine}'
        }
      }
      plugin {
        groupId 'org.eluder.coveralls'
        artifactId 'coveralls-maven-plugin'
        version '4.3.0'
        configuration {
          repoToken '${env.COVERALLS_REPO_KEY}'
          jacocoReports '${project.reporting.outputDirectory}/code-coverage/jacoco.xml'
        }
      }
      plugin {
        groupId 'org.codehaus.mojo'
        artifactId 'sonar-maven-plugin'
        version '3.4.0.905'
      }
      plugin {
        groupId 'org.codehaus.mojo'
        artifactId 'properties-maven-plugin'
        version '1.0.0'
        executions {
          execution {
            phase 'initialize'
            goals {
              goal 'read-project-properties'
            }
            configuration {
              quiet 'true'
              files {
                file '${project.basedir}/dev-sonar.properties'
              }
            }
          }
        }
      }
    }
  }
  repositories {
    repository {
      id 'jitpack.io'
      url 'https://jitpack.io'
    }
  }
}
