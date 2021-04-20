package io.github.cdfn.skyblock.commons.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Exclude;

public class RedisConfig extends OkaeriConfig {
  @Exclude
  public static final String PREFIX = "skyblock:message:";

  @Comment("Redis URI client will attempt connect to")
  private String connectionUri = "redis://redis/";

  public String getConnectionUri() {
    return connectionUri;
  }
}
