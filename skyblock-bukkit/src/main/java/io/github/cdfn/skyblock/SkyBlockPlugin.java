package io.github.cdfn.skyblock;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.github.cdfn.skyblock.commons.RedisClientFactory;
import io.github.cdfn.skyblock.commons.config.RedisConfig;
import io.github.cdfn.skyblock.commons.module.OkaeriConfigModule;
import io.lettuce.core.RedisConnectionException;
import kr.entree.spigradle.annotations.PluginMain;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

@PluginMain
public class SkyBlockPlugin extends JavaPlugin implements Module {

  private Injector injector;

  @Override
  public void onEnable() {
    this.injector = Guice.createInjector(
        OkaeriConfigModule.create(
            this.getDataFolder().toPath().resolve("redis.hjson"),
            RedisConfig.class
        )
    );

    var logger = this.getSLF4JLogger();
    var server = this.getServer();
    try {
      var client = RedisClientFactory.createRedisClient(injector.getInstance(RedisConfig.class));
      var conn = client.connect().sync();
      logger.info("Redis response: {}", conn.ping());
    } catch (RedisConnectionException exception) {
      logger.error("Failed to connect to redis", exception);
      server.shutdown();
    }
  }

  @Override
  public void configure(Binder binder) {
  }
}
