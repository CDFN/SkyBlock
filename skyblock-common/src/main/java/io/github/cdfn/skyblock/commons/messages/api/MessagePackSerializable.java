package io.github.cdfn.skyblock.commons.messages.api;

import java.io.IOException;
import org.msgpack.core.MessageBufferPacker;

public interface MessagePackSerializable {

  MessageBufferPacker serialize() throws IOException;

  void deserialize(byte[] bytes) throws IOException;
}
