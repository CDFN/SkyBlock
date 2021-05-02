package io.github.cdfn.skyblock.util.playerdata;

public class PlayerData {
  private final byte[] data;
  private final byte[] advancements;
  private final byte[] statistics;

  public PlayerData(byte[] data, byte[] advancements, byte[] statistics) {
    this.data = data;
    this.advancements = advancements;
    this.statistics = statistics;
  }

  public byte[] getData() {
    return data;
  }

  public byte[] getAdvancements() {
    return advancements;
  }

  public byte[] getStatistics() {
    return statistics;
  }

}
