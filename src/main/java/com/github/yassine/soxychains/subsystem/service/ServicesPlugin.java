package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.soxychains.plugin.Plugin;
import com.github.yassine.soxychains.subsystem.docker.image.config.DockerImage;
import com.github.yassine.soxychains.subsystem.docker.image.config.FloatingDockerImage;
import com.github.yassine.soxychains.subsystem.docker.image.config.RemoteDockerImage;
import lombok.SneakyThrows;
import net.jodah.typetools.TypeResolver;

import java.net.URI;
import java.util.Optional;

import static com.google.common.collect.Lists.reverse;
import static java.util.Arrays.asList;

/**
 * The main contract to fulfill for a given Service.
 * - Services typically run as container (not exclusively) on each host
 * - Services may declare dependencies through '@DependsOn' annotation from the 'guice-utils' module in order to require
 * another service to start before booting.
 *
 * (WIP: Spec may evolve yet)
 *
 * @param <CONFIG>
 */
public interface ServicesPlugin<CONFIG extends ServicesPluginConfiguration> extends Plugin<CONFIG> {

  default String configKey(){
    return reverse(asList(getClass().getPackage().getName().split("\\."))).get(0);
  }

  /**
   * A service would typically run as a container on a given host. If so, it should
   * @param config
   * @return
   */
  default Optional<DockerImage> getImage(CONFIG config){
    String path = "classpath://"+getClass().getPackage().getName().replaceAll("\\.","/");
    if (getClass().getResourceAsStream("Dockerfile") == null &&  getClass().getResourceAsStream("Dockerfile.template") == null){
      return Optional.of(new RemoteDockerImage(config.imageName()));
    }
    return Optional.of(new FloatingDockerImage(config.imageName(), URI.create(path)));
  }

  @SneakyThrows @SuppressWarnings("unchecked")
  default CONFIG defaultConfiguration(){
    Class[] types = TypeResolver.resolveRawArguments(ServicesPlugin.class, getClass());
    return (CONFIG) types[0].newInstance();
  }
}
