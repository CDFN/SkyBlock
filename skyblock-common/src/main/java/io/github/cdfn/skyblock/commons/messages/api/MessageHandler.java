package io.github.cdfn.skyblock.commons.messages.api;

public interface MessageHandler<T extends MessagePackSerializable> {

  void accept(T message);

  boolean isOneTime();
}
