package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.google.common.base.Strings;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static io.reactivex.Maybe.fromFuture;
import static io.reactivex.Maybe.just;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;


@RequiredArgsConstructor @Slf4j
class DockerSupport implements Docker {

  private final SoxyChainsDockerClient docker;

  @Override
  public Maybe<Image> findImageByTag(String imageTag) {
    return Maybe.fromFuture( supplyAsync( () ->
      docker.listImagesCmd()
        .exec()
        .stream()
        .filter(image -> stream(ofNullable(image.getRepoTags()).orElse(new String[0]))
                          .anyMatch((v)-> !Strings.isNullOrEmpty(imageTag) && v.contains(imageTag)))
        .findAny()
        .map(Maybe::just)
        .orElse(Maybe.empty())
    ) )
    .flatMap(m -> m);
  }

  @Override
  public Maybe<String> createNetwork(String networkName, Consumer<CreateNetworkCmd> beforeCreate, Consumer<String> afterCreate) {
    return Maybe.fromFuture( supplyAsync( () ->
        docker.listNetworksCmd().exec().stream()
          .filter(network -> network.getName().equals(networkName))
          .findAny()
          .map( network -> {
              log.warn(format("Network '%s' alreadyExists", network));
              return Maybe.just(network.getId());
            }
          )
          .orElse( Maybe.just(1)
            .flatMap( value -> new SyncDockerExecutor<>(docker.createNetworkCmd().withName(networkName), docker.configuration())
              .withSuccessFormatter( (v) -> format("Successfully created network '%s' to id '%s' ", networkName, v.getId()) )
              .withErrorFormatter( (e) -> format("An occurred while creating network '%s' : '%s'", networkName, e.getMessage()) )
              .withBeforeExecute( beforeCreate )
              .withAfterExecute( (v) -> afterCreate.accept( v.getId() ) )
              .execute()
              .map(CreateNetworkResponse::getId)
              .subscribeOn(Schedulers.io())
            )
          )
        )
      )
    .flatMap(v -> v);
  }

  @Override
  public Maybe<Boolean> removeNetwork(String networkName, Consumer<RemoveNetworkCmd> beforeRemove, Consumer<String> afterRemove) {
    return Maybe.fromFuture(supplyAsync(() ->
      docker.listNetworksCmd().exec().stream()
        .filter(network -> network.getName().equals(networkName))
        .findAny()
        .map(network -> Maybe.just(1)
          .flatMap(v -> new SyncDockerExecutor<>(docker.removeNetworkCmd(network.getId()), docker.configuration() )
            .withSuccessFormatter( (voi) -> format("Successfully removed network '%s' with id '%s' ", networkName, network.getId()) )
            .withErrorFormatter( (e) -> format("An occurred while removing network '%s' : '%s'", networkName, e.getMessage()) )
            .withBeforeExecute(beforeRemove)
            .withAfterExecute((voi) -> afterRemove.accept( networkName ))
            .execute()
            .subscribeOn(Schedulers.io()))
          .map(v -> true)
        )
        .orElseGet(() -> {
          log.warn(format("Cannot remove network '%s'. The network doesn't exist", networkName));
          return Maybe.just(false);
        })
    ))
    .flatMap(v -> v);
  }

  @Override
  public Maybe<Container> startContainer(String containerName, String image, Consumer<CreateContainerCmd> beforeCreate, Consumer<String> afterCreate, Consumer<StartContainerCmd> beforeStart, Consumer<String> afterStart) {
    return fromFuture( supplyAsync( () -> {
        try{
          return docker.listContainersCmd()
            .withShowAll(true)
            .exec()
            .stream()
            .findAny()
            .map(
              container -> CreateContainerStatus.of(
                container,
                format("Skipping container '%s' creation. The container already exists.", containerName),
                container.getStatus().startsWith("Up")
              )
            )
            .orElseGet( () -> {
              CreateContainerCmd command = docker.createContainerCmd(image)
                .withName(containerName);
              beforeCreate.accept(command);
              String containerID = command.exec().getId();
              afterCreate.accept(containerID);
              Container container = docker.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .findAny().get();
              return CreateContainerStatus.of(container, format("Successfully created container '%s' with id '%s'.", containerName, containerID), false);
            });
        }catch (Exception e){
          log.error(format("Couldn't create container '%s' : %s", containerName, e.getMessage()));
          log.error(e.getMessage(), e);
          return CreateContainerStatus.of((Container) null, format("Couldn't create container '%s' : %s", containerName, e.getMessage()), false);
        }
      }
      )
    ).flatMap(createContainerStatus -> {
      log.info(createContainerStatus.message());
      if(createContainerStatus.container() != null && !createContainerStatus.isStarted()){
        try{
          StartContainerCmd command = docker.startContainerCmd(createContainerStatus.container().getId());
          beforeStart.accept(command);
          command.exec();
          log.info("Successfully started container '{}'", Arrays.toString(createContainerStatus.container().getNames()));
          afterStart.accept(containerName);
          return Maybe.just(createContainerStatus.container());
        }catch (Exception e){
          log.error(format("Couldn't start container '%s' : %s", containerName, e.getMessage()));
          log.error(e.getMessage(), e);
          return Maybe.empty();
        }
      }else if(createContainerStatus.container() != null && createContainerStatus.isStarted()){
        log.info(format("Skipping starting container '%s' as it is already started.", containerName));
        return Maybe.just(createContainerStatus.container());
      }
      else{
        return Maybe.empty();
      }
    });
  }

  @Override
  public Maybe<Boolean> stopContainer(String containerName, Consumer<StopContainerCmd> beforeStop, Consumer<String> afterStop, Consumer<RemoveContainerCmd> beforeRemove, Consumer<String> afterRemove) {
    return fromFuture( supplyAsync( () -> {
        try{
          return docker.listContainersCmd()
            .withShowAll(true)
            .exec()
            .stream()
            .findAny()
            .map(container -> {
              if (container.getStatus().contains("Up")) {
                StopContainerCmd command = docker.stopContainerCmd(container.getId());
                beforeStop.accept(command);
                command.exec();
                log.info(format("Successfully stopped container '%s'.", containerName));
                afterStop.accept(containerName);
              }else{
                log.info(format("Skipping container '%s' stopping. The container is already stopped.", containerName));
              }
              return Pair.of(container.getId(), true);
            })
            .orElseGet( () -> {
              log.info(format("Skipping container '%s' stopping/removal. The container doesn't exist.", containerName));
              return Pair.of(null, false);
            });
        }catch (Exception e){
          log.error(format("Couldn't stop container '%s' : %s", containerName, e.getMessage()));
          log.error(e.getMessage(), e);
          return Pair.of( (String) null, false);
        }
      }
      )
    ).flatMap(pair -> {
      if(pair.getKey() != null){
        try{
          RemoveContainerCmd command = docker.removeContainerCmd(pair.getKey());
          beforeRemove.accept(command);
          command.exec();
          log.info(format("Successfully removed container '%s'.", containerName));
          afterRemove.accept(containerName);
          return Maybe.just(true);
        }catch (Exception e){
          log.error(format("Couldn't remove container '%s' : %s", containerName, e.getMessage()));
          log.error(e.getMessage(), e);
          return Maybe.just(false);
        }
      }else{
        return Maybe.just(false);
      }
    });
  }

  @Override
  public Maybe<Boolean> removeImage(String tag, Consumer<RemoveImageCmd> beforeRemove, Consumer<String> afterRemove) {
    return findImageByTag(tag)
      .flatMap(image ->
        Maybe.fromFuture(CompletableFuture.supplyAsync(()->{
          new SyncDockerExecutor<>(docker.removeImageCmd(image.getId()), docker.configuration() )
            .withSuccessFormatter( (v) -> format("Successfully removed image '%s'", tag) )
            .withErrorFormatter( (e) -> format("An occurred before removing image '%s' : '%s'", tag, e.getMessage()) )
            .withBeforeExecute(beforeRemove)
            .withAfterExecute((v) -> afterRemove.accept( tag ))
            .execute().blockingGet();
          return true;
        })).subscribeOn(Schedulers.io())
      ).defaultIfEmpty(false);
  }

  public Maybe<String> buildImage(String tag, Consumer<BuildImageCmd> beforeCreate, Consumer<String> afterCreate){
    return just(1)
      .flatMap(v -> (this.<BuildImageCmd, BuildResponseItem, BuildImageResultCallback, String>getAsyncExecutor(docker.buildImageCmd().withTag(tag), docker.configuration()))
        .withErrorFormatter((e) -> format("An error occurred before creating image '%s' : '%s'", tag, e.getMessage()))
        .withSuccessFormatter((id) -> format("Successfully created image '%s'. Image ID: '%s'", tag, id))
        .withResultExtractor(BuildImageResultCallback::awaitImageId)
        .withBeforeExecute(beforeCreate)
        .withAfterExecute(afterCreate)
        .withCallBack(new BuildImageResultCallback())
        .execute()
        .subscribeOn(Schedulers.io())
      );
  }

  <CMD extends AsyncDockerCmd<CMD, ITEM>, ITEM, CALLBACK extends ResultCallback<ITEM>, RESULT> ASyncDockerExecutor<CMD, ITEM, CALLBACK, RESULT> getAsyncExecutor(CMD command, DockerHostConfiguration config){
    return new ASyncDockerExecutor<>(command, config);
  }

}