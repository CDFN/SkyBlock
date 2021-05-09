package io.github.cdfn.skyblock.messages;

import io.github.cdfn.skyblock.commons.messages.api.MessagePackSerializable;
import io.github.cdfn.skyblock.util.playerdata.PlayerData;
import java.io.IOException;
import java.util.UUID;
import org.msgpack.core.MessagePack;

public class PlayerDataMessages {

  public static class PlayerDataRequest implements MessagePackSerializable {
    private UUID uuid;

    public PlayerDataRequest(UUID uuid){
      this.uuid = uuid;
    }

    @Override
    public byte[] serialize() throws IOException {
      try(var packer = MessagePack.newDefaultBufferPacker()) {
        packer.packString(this.uuid.toString());
        return packer.toByteArray();
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
    private PlayerData playerData;

    public PlayerDataResponse(UUID uuid, PlayerData playerData){
      this.uuid = uuid;
      this.playerData = playerData;
    }

    @Override
    public byte[] serialize() throws IOException {
      try(var packer = MessagePack.newDefaultBufferPacker()) {
        packer.packString(this.uuid.toString());

        var data = this.playerData.getData();
        packer.packArrayHeader(data.length);
        packer.writePayload(data);

        var advancements = this.playerData.getAdvancements();
        packer.packArrayHeader(advancements.length);
        packer.writePayload(advancements);

        var statistics = this.playerData.getStatistics();
        packer.packArrayHeader(statistics.length);
        packer.writePayload(statistics);

        return packer.toByteArray();
      }

    }

    @Override
    public void deserialize(byte[] bytes) throws IOException {
      try(var unpacker = MessagePack.newDefaultUnpacker(bytes)) {
        this.uuid = UUID.fromString(unpacker.unpackString());
        this.playerData = new PlayerData(
            unpacker.readPayload(unpacker.unpackArrayHeader()),
            unpacker.readPayload(unpacker.unpackArrayHeader()),
            unpacker.readPayload(unpacker.unpackArrayHeader())
        );
      }
    }

    public UUID getUUID() {
      return uuid;
    }

    public PlayerData getPlayerData() {
      return this.playerData;
    }

  }
}
