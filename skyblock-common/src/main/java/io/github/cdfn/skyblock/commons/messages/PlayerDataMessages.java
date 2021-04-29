package io.github.cdfn.skyblock.commons.messages;

import io.github.cdfn.skyblock.commons.messages.api.MessagePackSerializable;
import java.io.IOException;
import java.util.UUID;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

public class PlayerDataMessages {

  public static class PlayerDataRequest implements MessagePackSerializable {
    private UUID uuid;

    public PlayerDataRequest(UUID uuid){
      this.uuid = uuid;
    }

    @Override
    public MessageBufferPacker serialize() throws IOException {
      try(var packer = MessagePack.newDefaultBufferPacker()) {
        packer.packString(this.uuid.toString());
        return packer;
      }
    }

    @Override
    public void deserialize(byte[] bytes) throws IOException {
      try(var unpacker = MessagePack.newDefaultUnpacker(bytes)) {
        this.uuid = UUID.fromString(unpacker.unpackString());
      }
    }

    public UUID getUUID() {
      return uuid;
    }
  }

  public static class PlayerDataResponse implements MessagePackSerializable {
    private UUID uuid;
    private byte[] data;

    public PlayerDataResponse(UUID uuid, byte[] data){
      this.uuid = uuid;
      this.data = data;
    }

    @Override
    public MessageBufferPacker serialize() throws IOException {
      try(var packer = MessagePack.newDefaultBufferPacker()) {
        packer.packString(this.uuid.toString());
        packer.packArrayHeader(this.data.length);
        packer.writePayload(this.data);
        return packer;
      }

    }

    @Override
    public void deserialize(byte[] bytes) throws IOException {
      try(var unpacker = MessagePack.newDefaultUnpacker(bytes)) {
        this.uuid = UUID.fromString(unpacker.unpackString());
        this.data = unpacker.readPayload(unpacker.unpackArrayHeader());
      }
    }

    public UUID getUUID() {
      return uuid;
    }

    public byte[] getData() {
      return data;
    }

  }
}
