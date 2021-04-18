package io.github.cdfn.skyblock.commons.messages;

import io.github.cdfn.skyblock.commons.messages.api.MessagePackSerializable;
import java.io.IOException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

public class ConfigMessages{

  private static final String CHANNEL_NAME_REQ = "config_messages_req";
  private static final String CHANNEL_NAME_RES = "config_messages_res";

  public static class ConfigRequest implements MessagePackSerializable {

    private int id;

    public ConfigRequest() {
    }

    public ConfigRequest(int id) {
      this.id = id;
    }

    @Override
    public MessageBufferPacker serialize() throws IOException {
      var packer = MessagePack.newDefaultBufferPacker();
      packer.packInt(this.id);
      packer.close();
      return packer;
    }

    @Override
    public void deserialize(byte[] bytes) throws IOException {
      var unpacker = MessagePack.newDefaultUnpacker(bytes);
      var returnValue = new ConfigRequest(unpacker.unpackInt());
      unpacker.close();
    }

    public int getId() {
      return id;
    }
  }

  public static class ConfigResponse implements MessagePackSerializable {

    private int id;
    private String data;

    public ConfigResponse() {
    }

    public ConfigResponse(int id, String data) {
      this.id = id;
      this.data = data;
    }

    @Override
    public MessageBufferPacker serialize() throws IOException {
      var packer = MessagePack.newDefaultBufferPacker();
      packer.packInt(this.id);
      packer.close();
      return packer;
    }

    @Override
    public void deserialize(byte[] bytes) throws IOException {
      var unpacker = MessagePack.newDefaultUnpacker(bytes);
      this.id = unpacker.unpackInt();
      this.data = unpacker.unpackString();
    }

    public int getId() {
      return id;
    }

    public String getData() {
      return data;
    }
  }
}
