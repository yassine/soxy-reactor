package com.github.yassine.soxychains.subsystem.service;

import com.github.yassine.artifacts.guice.scheduling.DependsOn;
import com.github.yassine.artifacts.guice.scheduling.TaskScheduler;
import com.github.yassine.soxychains.core.Phase;
import com.github.yassine.soxychains.core.RunOn;
import com.github.yassine.soxychains.core.Task;
import com.github.yassine.soxychains.subsystem.docker.client.DockerProvider;
import com.github.yassine.soxychains.subsystem.docker.config.DockerContext;
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkHelper;
import com.github.yassine.soxychains.subsystem.docker.networking.NetworkingConfiguration;
import com.github.yassine.soxychains.subsystem.docker.networking.task.NetworkingStartupTask;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import static com.github.yassine.soxychains.core.FluentUtils.AND_OPERATOR;
import static com.github.yassine.soxychains.plugin.PluginUtils.configClassOf;
import static com.github.yassine.soxychains.subsystem.docker.NamespaceUtils.*;
import static io.reactivex.Observable.fromCallable;
import static io.reactivex.Observable.fromIterable;

@Slf4j @DependsOn(NetworkingStartupTask.class)
@RunOn(Phase.START) @AutoService(Task.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = AccessLevel.PUBLIC)
public class ServicesStartTask implements Task{

  private final Set<ServicesPlugin> services;
  private final TaskScheduler taskScheduler;
  private final DockerProvider dockerProvider;
  private final DockerContext dockerContext;
  private final NetworkingConfiguration networkingConfiguration;
  private final NetworkHelper networkHelper;
  private final Injector injector;

  @Override @SuppressWarnings("unchecked")
  public Single<Boolean> execute() {
    // actions to come are executed on each host in parallel
    return dockerProvider.dockers()
      //Knowing that a service may require other services to start before actually starting, services will be started
      //as waves of tasks that can be executed in parallel.
      .flatMap(docker -> fromIterable(taskScheduler.scheduleInstances(services))
          //for each wave of services
          .flatMap(servicesWave -> fromCallable(() -> fromIterable(servicesWave)
              //for each service
              .flatMapMaybe(service ->
                //create & start the container that relates to the given service
                docker.runContainer(
                  //with a name of
                  nameSpaceContainer(dockerContext, configOf(service).serviceName()),
                  //and image
                  nameSpaceImage(dockerContext, configOf(service).imageName()),
                  // The pre-create container hook is used to allow services configuring the container before their creation
                  createContainer -> {
                    service.configureContainer(createContainer, configOf(service), dockerContext);
                    createContainer.withNetworkMode(nameSpaceNetwork(dockerContext, networkingConfiguration.getNetworkName()));
                    createContainer.withName(nameSpaceContainer(dockerContext, configOf(service).serviceName()));
                    createContainer.withImage(nameSpaceImage(dockerContext, configOf(service).imageName()));
                    createContainer.withLabels(labelizeNamedEntity(configOf(service).serviceName(), dockerContext));
                    networkHelper.getDNSAddress(docker).map(createContainer::withDns).toObservable().blockingSubscribe();
                  }
                ).map(containerId -> service)
                .subscribeOn(Schedulers.io())
              )
              // wait for programmatic startup check
              .flatMapSingle(service -> (Single<Boolean>) service.isReady(docker.hostConfiguration(), configOf(service)))
              // reduce (over each service) the results as a single boolean value
              .defaultIfEmpty(false).reduce(true, AND_OPERATOR).blockingGet())
          )
      // reduce (over each host) the results as a single boolean value
      ).reduce(true, AND_OPERATOR).subscribeOn(Schedulers.io());
  }

  @SuppressWarnings("unchecked")
  private ServicesPluginConfiguration configOf(ServicesPlugin plugin){
    return (ServicesPluginConfiguration) injector.getInstance(configClassOf((Class) plugin.getClass()));
  }
}
