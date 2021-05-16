package io.github.cdfn.skyblock.commons.message.api.handler;

import io.github.cdfn.skyblock.commons.message.api.serializer.MessagePackSerializable;

public interface MessageHandler<T extends MessagePackSerializable> {

  void accept(T message);

  default boolean isOneTime() {
    return false;
  }
}
