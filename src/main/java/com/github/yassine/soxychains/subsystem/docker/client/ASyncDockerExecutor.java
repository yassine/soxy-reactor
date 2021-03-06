package com.github.yassine.soxychains.subsystem.docker.client;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.AsyncDockerCmd;
import com.github.yassine.soxychains.subsystem.docker.config.DockerHostConfiguration;
import com.google.common.base.Preconditions;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") @Slf4j
class ASyncDockerExecutor<C extends AsyncDockerCmd<C, A>, A, K extends ResultCallback<A>, R> {

  private final C cmd;
  private final DockerHostConfiguration configuration;
  private K callback;
  private Function<K, R> resultExtractor;
  private Function<R, String> successFormatter;
  private Function<Throwable, String> errorFormatter;
  private Optional<Consumer<C>> beforeExecute;
  private Optional<Consumer<R>> afterExecute;

  public ASyncDockerExecutor(C cmd, DockerHostConfiguration configuration) {
    this.cmd = cmd;
    this.configuration = configuration;
    this.successFormatter = result -> format("Successfully executed command '%s'", cmd.getClass().getSimpleName());
    this.errorFormatter = exception -> format("Error occurred while executing command '%s' : %s", cmd.getClass().getSimpleName(), exception.getMessage());
  }

  public Maybe<R> execute(){

    Preconditions.checkNotNull(cmd);
    Preconditions.checkNotNull(configuration);
    Preconditions.checkNotNull(callback);
    Preconditions.checkNotNull(resultExtractor);

    return Maybe.fromCallable(() -> {
      try{
        beforeExecute.ifPresent(before -> before.accept(cmd));
        cmd.exec(callback);
        R result = getResultExtractor().apply(callback);
        log.info(successFormatter.apply(result));
        afterExecute.ifPresent(after -> after.accept(result));
        return Maybe.just(result);
      }catch (Exception e){
        log.error(e.getMessage(), e);
        log.error(errorFormatter.apply(e));
        return Maybe.<R>empty();
      }
    }).flatMap(v -> v).subscribeOn(Schedulers.io());
  }

  Function<K, R> getResultExtractor(){
    return resultExtractor;
  }


  public ASyncDockerExecutor<C, A, K, R> withSuccessFormatter(Function<R, String> successFormatter){
    Preconditions.checkNotNull(successFormatter);
    this.successFormatter = successFormatter;
    return this;
  }

  public ASyncDockerExecutor<C, A, K, R> withErrorFormatter(Function<Throwable, String> errorFormatter){
    Preconditions.checkNotNull(errorFormatter);
    this.errorFormatter = errorFormatter;
    return this;
  }

  public ASyncDockerExecutor<C, A, K, R> withBeforeExecute(Consumer<C> beforeExecute){
    this.beforeExecute = Optional.ofNullable(beforeExecute);
    return this;
  }

  public ASyncDockerExecutor<C, A, K, R> withAfterExecute(Consumer<R> afterExecute){
    this.afterExecute = Optional.ofNullable(afterExecute);
    return this;
  }

  public ASyncDockerExecutor<C, A, K, R> withCallBack(K callBack){
    this.callback = callBack;
    return this;
  }

  public ASyncDockerExecutor<C, A, K, R> withResultExtractor(Function<K, R> resultExtractor){
    this.resultExtractor = resultExtractor;
    return this;
  }

}
