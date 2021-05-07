package io.github.cdfn.skyblock.commons.messages.api;

import java.io.IOException;

public interface MessagePackSerializable {

  byte[] serialize() throws IOException;

  void deserialize(byte[] bytes) throws IOException;
}
