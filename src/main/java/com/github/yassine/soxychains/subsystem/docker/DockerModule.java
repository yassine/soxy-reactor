package com.github.yassine.soxychains.subsystem.docker;

import com.github.yassine.artifacts.guice.templating.TemplatingModule;
import com.github.yassine.soxychains.SoxyChainsContext;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProviderSupport;
import com.github.yassine.soxychains.subsystem.docker.client.HostManager;
import com.github.yassine.soxychains.subsystem.docker.client.HostManagerSupport;
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
import com.github.yassine.soxychains.subsystem.docker.image.DockerImageModule;
import com.github.yassine.soxychains.subsystem.docker.networking.DNSConfiguration;
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkingConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DockerModule extends AbstractModule{

  @Override
  protected void configure() {
    bind(DockerProvider.class).to(DockerProviderSupport.class);
    bind(HostManager.class).to(HostManagerSupport.class);
    install(new DockerImageModule());
    install(new TemplatingModule());
  }

  @Provides @Singleton
  DockerContext configuration(SoxyChainsContext configuration){
    return configuration.getDocker();
  }

  @Provides @Singleton
  NetworkingConfiguration configuration(DockerContext dockerContext){
    return dockerContext.getNetworkingConfiguration();
  }

  @Provides @Singleton
  DNSConfiguration configuration(NetworkingConfiguration networkingConfiguration){
    return networkingConfiguration.getDnsConfiguration();
  }
}
