package io.github.cdfn.skyblock.commons.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class WorkerConfig extends OkaeriConfig {

  @Comment("How many worlds each worker should serve")
  private Integer worldsLimit = 128;

  @Comment("Loader for SWM (file, mysql, mongo, redis)")
  private String loader = "redis";

  public Integer getWorldsLimit() {
    return worldsLimit;
  }
}
