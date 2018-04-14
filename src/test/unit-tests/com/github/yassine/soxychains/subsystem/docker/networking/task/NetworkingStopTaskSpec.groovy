package com.github.yassine.soxychains.subsystem.docker.networking.task

import com.github.dockerjava.api.command.RemoveNetworkCmd
import com.github.yassine.soxychains.SoxyChainsConfiguration
import com.github.yassine.soxychains.subsystem.docker.client.Docker
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkingConfiguration
import io.reactivex.Maybe
import spock.lang.Specification

import java.util.function.Consumer

class NetworkingStopTaskSpec extends Specification {

  Docker docker = Mock()
  DockerProvider dockerProvider = Mock()
  SoxyChainsConfiguration soxyChainsConfiguration = new SoxyChainsConfiguration()
  DockerConfiguration configuration = soxyChainsConfiguration.getDocker()
  NetworkingConfiguration networkingConfiguration = configuration.getNetworkingConfiguration()
  NetworkingStopTask task = new NetworkingStopTask(dockerProvider, configuration, networkingConfiguration, soxyChainsConfiguration)

  void setup () {
    dockerProvider.dockers() >> [ docker ]
  }

  def "execute: it should return true when networks are successfully created" () {
    setup:
    docker.removeNetwork(_ as String, _ as Consumer<RemoveNetworkCmd>, _ as Consumer<String>) >> Maybe.just(true)
    expect:
    task.execute().blockingGet()
  }

  def "execute: it should return true when networks fail to get created" () {
    setup:
    docker.removeNetwork(_ as String, _ as Consumer<RemoveNetworkCmd>, _ as Consumer<String>) >> Maybe.just(false)
    expect:
    !task.execute().blockingGet()
  }

}