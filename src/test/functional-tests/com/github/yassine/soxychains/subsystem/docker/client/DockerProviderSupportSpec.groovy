package com.github.yassine.soxychains.subsystem.docker.client

import com.github.dockerjava.api.DockerClient
import com.github.yassine.soxychains.ConfigurationModule
import com.github.yassine.soxychains.SoxyChainsModule
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration
import com.google.inject.AbstractModule
import com.google.inject.Inject
import spock.guice.UseModules
import spock.lang.Specification

@UseModules(TestModule)
class DockerProviderSupportSpec extends Specification {
  @Inject
  DockerContext dockerConfiguration
  @Inject
  DockerProvider dockerProvider

  def "getClient: it should return a configured docker client"() {
    when:
    DockerHostConfiguration host = dockerConfiguration.getHosts().get(0)
    DockerClient docker = dockerProvider.getClient(host)
    docker.pingCmd().exec()
    then:
    true //no exception
  }

  static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      InputStream config = getClass().getResourceAsStream("docker-provider-test.yaml")
      install(new SoxyChainsModule())
      install(new ConfigurationModule(config))
    }
  }
}
