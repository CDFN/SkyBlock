package io.github.cdfn.skyblock.commons.module.redis;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.github.cdfn.skyblock.commons.config.RedisConfig;
import io.lettuce.core.RedisClient;

class RedisClientModule extends AbstractModule {

  @Provides
  public RedisClient getClient(RedisConfig config) {
    return RedisClient.create(config.getConnectionUri());
  }
}
