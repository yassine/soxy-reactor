package com.github.yassine.soxychains.subsystem.service

import com.github.yassine.soxychains.SoxyChainsModule
import com.github.yassine.soxychains.subsystem.service.consul.ConsulConfiguration
import com.github.yassine.soxychains.subsystem.service.consul.ConsulService
import com.google.inject.Guice
import com.google.inject.Injector
import spock.lang.Specification

class ServicesModuleSpec extends Specification {
  def "it should create a binding for services configurations"() {
    setup:
    InputStream config = getClass().getResourceAsStream("service-module-config.yaml")
    when:
    Injector injector = Guice.createInjector(new SoxyChainsModule(config))
    ConsulConfiguration configuration = injector.getInstance(ConsulConfiguration.class)
    then:
    configuration != null
  }
  def "it should create a binding for services configurations when services configuration is overridden by the user"() {
    setup:
    InputStream config = getClass().getResourceAsStream("service-module-config-with-services.yaml")
    when:
    Injector injector = Guice.createInjector(new SoxyChainsModule(config))
    ConsulConfiguration configuration = injector.getInstance(ConsulConfiguration.class)
    then:
    configuration.imageName() == 'consul-test'
  }

  def "it should create a binding for services and inject their dependencies"() {
    setup:
    InputStream config = getClass().getResourceAsStream("service-module-config-with-services.yaml")
    when:
    Injector injector = Guice.createInjector(new SoxyChainsModule(config))
    ConsulService consulService = injector.getInstance(ConsulService.class)
    ConsulConfiguration configuration = consulService.configuration();
    then:
    consulService != null
    configuration != null
    configuration.imageName() == 'consul-test'
  }

}