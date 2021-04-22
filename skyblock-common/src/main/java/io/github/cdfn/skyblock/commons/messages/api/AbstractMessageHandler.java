package io.github.cdfn.skyblock.commons.messages.api;

public abstract class AbstractMessageHandler<T extends MessagePackSerializable> implements MessageHandler<T> {

  private final boolean oneTime;

  public AbstractMessageHandler(boolean oneTime) {
    this.oneTime = oneTime;
  }

  public AbstractMessageHandler() {
    this.oneTime = false;
  }

  public boolean isOneTime() {
    return oneTime;
  }
}
