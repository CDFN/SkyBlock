package io.github.cdfn.skyblock.commons.module.redis;

import com.google.inject.AbstractModule;
import io.github.cdfn.skyblock.commons.config.RedisConfig;
import io.github.cdfn.skyblock.commons.module.OkaeriConfigModule;
import java.nio.file.Path;

public class RedisModule extends AbstractModule {

  private final Path path;

  public RedisModule(Path path) {
    this.path = path;
  }

  @Override
  protected void configure() {
    install(new OkaeriConfigModule<>(path.resolve("redis.hjson"), RedisConfig.class));
    install(new RedisClientModule());
  }
}
