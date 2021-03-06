package com.github.yassine.soxychains.subsystem.service.gobetween;

import com.github.yassine.gobetween.GobetweenClient;
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.github.yassine.soxychains.subsystem.service.consul.ConsulConfiguration;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.net.URI;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class GobetweenProvider {

  private final DockerContext dockerContext;
  private final ConsulConfiguration consulConfiguration;
  private final GobetweenConfiguration gobetweenConfiguration;

  private final LoadingCache<DockerHostConfiguration, GobetweenClient> clientCache = CacheBuilder.newBuilder().build(new CacheLoader<DockerHostConfiguration, GobetweenClient>() {
    @Override
    public GobetweenClient load(DockerHostConfiguration dockerHostConfiguration) throws Exception {
    return com.github.yassine.gobetween.Gobetween.builder()
      .withURL(URI.create("http://"+dockerHostConfiguration.getHostname()+":"+gobetweenConfiguration.getApiPort()).toURL())
      .build();
    }
  });

  private final LoadingCache<DockerHostConfiguration, Gobetween> cache = CacheBuilder.newBuilder().build(new CacheLoader<DockerHostConfiguration, Gobetween>() {
    @Override @SneakyThrows
    public Gobetween load(DockerHostConfiguration dockerHostConfiguration) throws Exception {
    return new GobetweenSupport(clientCache.get(dockerHostConfiguration), dockerContext, dockerHostConfiguration, consulConfiguration);
    }
  });

  @SneakyThrows
  public Gobetween get(DockerHostConfiguration dockerHostConfiguration){
    return cache.get(dockerHostConfiguration);
  }

  @SneakyThrows
  public GobetweenClient getClient(DockerHostConfiguration dockerHostConfiguration){
    return clientCache.get(dockerHostConfiguration);
  }

}
