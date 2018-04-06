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
  }
  dependencies {
    dependency 'com.fasterxml.jackson.core:jackson-annotations:2.8.5'
    dependency 'com.fasterxml.jackson.core:jackson-core:2.8.5'
    dependency 'com.fasterxml.jackson.core:jackson-databind:2.8.5'
    dependency 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.8.5'
    dependency{
      groupId 'com.github.docker-java'
      artifactId 'docker-java'
      version '3.0.6'
      exclusions{
        exclusion{
          groupId 'log4j'
          artifactId 'log4j'
        }
      }
    }
    dependency 'com.github.yassine:guice-artifacts:0.2.0'
    dependency 'com.google.auto.service:auto-service:1.0-rc3'
    dependency 'com.google.guava:guava:21.0'
    dependency 'com.google.inject.extensions:guice-multibindings:4.1.0'
    dependency 'com.google.inject:guice:4.1.0'
    dependency 'io.airlift:airline:0.8'
    dependency 'io.github.lukehutch:fast-classpath-scanner:2.0.8'
    dependency 'io.reactivex.rxjava2:rxjava:2.1.0'
    dependency 'net.jodah:typetools:0.5.0'
    dependency 'org.glassfish:javax.el:3.0.1-b08'
    dependency 'org.hibernate.validator:hibernate-validator:6.0.5.Final'
    dependency 'org.projectlombok:lombok:1.16.18:provided'
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
    dependency 'org.spockframework:spock-core:1.1-groovy-2.4:test'
    dependency 'org.spockframework:spock-guice:1.1-groovy-2.4:test'
    dependency 'org.codehaus.groovy:groovy-all:2.4.13:test'
    dependency 'junit:junit:4.12:test'
    dependency 'cglib:cglib-nodep:3.2.5:test'
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
            additionalClasspathElement '${project.basedir}/src/test/resources'
            additionalClasspathElement '${project.build.testOutputDirectory}'
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
    }
  }
  repositories {
    repository {
      id 'jitpack.io'
      url 'https://jitpack.io'
    }
  }
}
