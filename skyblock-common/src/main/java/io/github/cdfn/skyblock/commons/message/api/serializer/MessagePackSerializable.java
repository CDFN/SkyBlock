package io.github.cdfn.skyblock.commons.message.api.serializer;

import java.io.IOException;

public interface MessagePackSerializable {

  byte[] serialize() throws IOException;

  void deserialize(byte[] bytes) throws IOException;
}
