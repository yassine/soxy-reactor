package com.github.yassine.soxychains.subsystem.docker.image.task;

import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
import com.github.yassine.soxychains.subsystem.docker.image.api.ImageRequirer;
import com.github.yassine.soxychains.subsystem.docker.image.resolver.DockerImageResolver;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.nameSpaceImage;
import static com.github.yassine.soxychains.subsystem.docker.image.task.ImageTaskUtils.getNecessaryImages;

/**
 * The install task would make sure that all the required docker images (network drivers, services, layer nodes)
 * are available in the hosts of the cluster.
 */
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
@RunOn(Phase.INSTALL) @AutoService(Task.class)
public class ImageInstallTask implements Task {

  private final Set<ImageRequirer> imageRequirer;
  private final DockerImageResolver dockerImageResolver;
  private final DockerProvider dockerProvider;
  private final DockerContext dockerContext;

  @Override
  public Single<Boolean> execute() {
    return dockerProvider.dockers()
            .flatMap(docker -> getNecessaryImages(imageRequirer)
              .flatMapMaybe(image -> docker.buildImage(nameSpaceImage(dockerContext, image.getName()),
                                                       imgCmd -> imgCmd.withTarInputStream(dockerImageResolver.resolve(image.getRoot(), image.getTemplateVars())).withForcerm(true))
                                            .map(StringUtils::isNoneEmpty)
              .defaultIfEmpty(false)).subscribeOn(Schedulers.io()))
            .reduce(true , AND_OPERATOR);
  }

}
