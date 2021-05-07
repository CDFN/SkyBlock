package io.github.cdfn.skyblock.commons.messages.util.sneaky;

import java.util.function.Function;

public interface SneakyFunction<T, U, E extends Exception> {
  static <T, U> Function<T, U> of(SneakyFunction<? super T, U, ? extends Exception> cons) {
    return e -> {
      try {
        return cons.apply(e);
      } catch (Exception ex) {
        SneakyUtil.sneakyThrow(ex);
      }
      return null;
    };
  }

  U apply(T t) throws E;
}