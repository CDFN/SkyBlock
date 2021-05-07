package io.github.cdfn.skyblock.commons.messages;

import io.github.cdfn.skyblock.commons.messages.api.MessagePackSerializable;
import java.io.IOException;
import org.msgpack.core.MessagePack;

public class ConfigMessages {

  public static class ConfigRequest implements MessagePackSerializable {

    private int id;

    public ConfigRequest(int id) {
      this.id = id;
    }

    @Override
    public byte[] serialize() throws IOException {
      try(var packer = MessagePack.newDefaultBufferPacker()) {
        packer.packInt(this.id);
        return packer.toByteArray();
      }
    }

    @Override
    public void deserialize(byte[] bytes) throws IOException {
      try(var unpacker = MessagePack.newDefaultUnpacker(bytes)) {
        this.id = unpacker.unpackInt();
      }
    }

    public int getId() {
      return id;
    }
  }

  public static class ConfigResponse implements MessagePackSerializable {

    private int id;
    private String data;

    public ConfigResponse(int id, String data) {
      this.id = id;
      this.data = data;
    }

    @Override
    public byte[] serialize() throws IOException {
      try(var packer = MessagePack.newDefaultBufferPacker()) {
        packer.packInt(this.id);
        packer.packString(this.data);
        return packer.toByteArray();
      }
    }

    @Override
    public void deserialize(byte[] bytes) throws IOException {
      try(var unpacker = MessagePack.newDefaultUnpacker(bytes)) {
        this.id = unpacker.unpackInt();
        this.data = unpacker.unpackString();
      }
    }

    public int getId() {
      return id;
    }

    public String getData() {
      return data;
    }
  }
}
