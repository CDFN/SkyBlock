package io.github.cdfn.skyblock.commons.message.util.sneaky;

import java.util.function.Consumer;

public interface SneakyConsumer<T, E extends Exception> {
  static <T> Consumer<T> of(SneakyConsumer<? super T, ? extends Exception> cons) {
    return e -> {
      try {
        cons.accept(e);
      } catch (Exception ex) {
        SneakyUtil.sneakyThrow(ex);
      }
    };
  }

  void accept(T t) throws E;
}
