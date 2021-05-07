package io.github.cdfn.skyblock.commons.messages.util.sneaky;

import java.util.function.BiConsumer;

public interface SneakyBiConsumer<T, U, E extends Exception> {
    static <T, U> BiConsumer<T, U> of(SneakyBiConsumer<? super T, ? super U, ? extends Exception> cons) {
      return (e, f) -> {
        try {
          cons.accept(e, f);
        } catch (Exception ex) {
          SneakyUtil.sneakyThrow(ex);
        }
      };
    }

    void accept(T t, U u) throws E;
}
