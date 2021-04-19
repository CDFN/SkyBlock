package io.github.cdfn.skyblock.commons.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class RedisConfig extends OkaeriConfig {

  @Comment("Redis URI client will attempt connect to")
  private String connectionUri = "redis://redis/";
  @Comment("Prefix for Redis messaging")
  private String prefix = "skyblock:message:";

  public String getConnectionUri() {
    return connectionUri;
  }

  public String getPrefix() {
    return prefix;
  }
}
