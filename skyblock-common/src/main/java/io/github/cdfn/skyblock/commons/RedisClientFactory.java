package io.github.cdfn.skyblock.commons;

import io.github.cdfn.skyblock.commons.config.RedisConfig;
import io.lettuce.core.RedisClient;

public class RedisClientFactory {

  public static RedisClient createRedisClient(RedisConfig config) {
    return RedisClient.create(config.getConnectionUri());
  }
}
