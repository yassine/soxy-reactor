package com.github.yassine.soxychains.core;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.machinezoo.noexception.Exceptions.sneak;
import static java.lang.Thread.sleep;
import static java.util.Optional.ofNullable;

@Slf4j
public class FluentUtils {

  private FluentUtils() {}

  public static <V> V runAndGet(Runnable runnable, V returnValue){
    runnable.run();
    return returnValue;
  }

  public static final BiFunction<Boolean, Boolean, Boolean> AND_OPERATOR = (a, b) -> a && b;

  public static <V> Single<V> runAndGetAsSingle(Runnable runnable, V returnValue){
    return Single.fromCallable(() -> runAndGet(runnable, returnValue));
  }

  public static <V> Maybe<V> runAndGetAsMaybe(Runnable runnable, V returnValue){
    return Maybe.fromCallable(() -> runAndGet(runnable, returnValue));
  }

  public static <V> Maybe<V> getWithRetry(Supplier<V> supplier, Function<Integer, String> successMessage, Function<Integer, String> errorMessage, final int maxRetries, final Long interval){
    return Maybe.fromCallable((() -> {
      final AtomicInteger ai = new AtomicInteger(maxRetries);
      while (ai.getAndDecrement() > 0) {
        try {
          V v = supplier.get();
          ofNullable(successMessage).ifPresent(msg -> log.info(successMessage.apply(maxRetries - ai.get())));
          return v;
        } catch (Exception e) {
          ofNullable(errorMessage).ifPresent(msg -> log.warn(errorMessage.apply(maxRetries - ai.get())));
          log.debug(e.getMessage(), e);
          if(ai.get() > 0){
            sneak().run(() -> sleep(interval));
          }
        }
      }
      throw new SoxyChainsException(ofNullable(errorMessage).map(msg -> errorMessage.apply(maxRetries - ai.get())).orElse(""));
    }));
  }

  //default @ 15 retries each 1 second
  public static <V> Maybe<V> getWithRetry(Supplier<V> supplier, Function<Integer, String> successMessage, Function<Integer, String> errorMessage){
    return getWithRetry(supplier, successMessage, errorMessage, 15, 1000L);
  }

  public static <S> S identity(S self){
    return self;
  }

}
