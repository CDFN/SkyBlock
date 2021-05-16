package io.github.cdfn.skyblock.commons.message.util.sneaky;

public abstract class SneakyUtil {
  @SuppressWarnings("unchecked")
  static <E extends Exception> void sneakyThrow(Exception e) throws E {
    throw (E) e;
  }
}
