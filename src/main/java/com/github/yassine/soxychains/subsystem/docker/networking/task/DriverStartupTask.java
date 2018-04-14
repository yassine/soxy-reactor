package com.github.yassine.soxychains.subsystem.docker.networking.task;

import com.github.dockerjava.api.model.Bind;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.NamespaceUtils;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerConfiguration;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;
import static io.reactivex.Observable.fromIterable;
import static java.lang.String.format;

@AutoService(Task.class)
@RunOn(Phase.START) @RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class DriverStartupTask implements Task {

  private final DockerProvider dockerProvider;
  private final DockerConfiguration dockerConfiguration;

  @Override
  public Single<Boolean> execute() {
    return fromIterable(dockerProvider.dockers())
      .flatMapMaybe(docker -> docker.runContainer(nameSpaceContainer(dockerConfiguration, SOXY_DRIVER_NAME), nameSpaceImage(dockerConfiguration, SOXY_DRIVER_NAME),
        (createContainerCmd) -> createContainerCmd.withNetworkMode("host")
          .withPrivileged(true)
          .withEnv(format("DRIVER_NAMESPACE='%s'", dockerConfiguration.getNamespace()))
          .withBinds(
            Bind.parse("/var/run/docker.sock:/var/run/docker.sock"),
            Bind.parse("/run/docker/plugins:/run/docker/plugins")
          )
          .withLabels(labelizeNamedEntity(SOXY_DRIVER_NAME, dockerConfiguration)),
        (containerID) -> {},
        (startContainerCmd) -> {},
        (containerID) -> {})
        .map(c -> true)
        .defaultIfEmpty(true))
      .reduce(true, (a,b) -> a && b);
  }

}