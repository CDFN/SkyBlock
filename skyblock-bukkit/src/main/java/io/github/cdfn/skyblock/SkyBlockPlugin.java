package io.github.cdfn.skyblock;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import io.github.cdfn.skyblock.commons.module.redis.RedisModule;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import java.nio.file.Path;
import kr.entree.spigradle.annotations.PluginMain;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.slf4j.Logger;


@PluginMain
public class SkyBlockPlugin extends JavaPlugin implements Module {

  private Injector injector;

  @Override
  public void onEnable() {
    this.injector = Guice.createInjector(
        this,
        new RedisModule(this.getDataFolder().toPath())
    );

    var logger = this.getSLF4JLogger();
    var server = this.getServer();

    try {
      var client = injector.getInstance(RedisClient.class);
      var conn = client.connect().sync();
      logger.info("Redis response: {}", conn.ping());
    } catch (RedisConnectionException exception) {
      logger.error("Failed to connect to redis", exception);
      server.shutdown();
    }
  }

  @Override
  public void configure(Binder binder) {
    binder.bind(JavaPlugin.class).toInstance(this);
    binder.bind(SkyBlockPlugin.class).toInstance(this);
    binder.bind(Server.class).toInstance(this.getServer());
    binder.bind(Logger.class).toInstance(this.getSLF4JLogger());
    binder.bind(BukkitScheduler.class).toInstance(this.getServer().getScheduler());
    binder.bind(PluginManager.class).toInstance(this.getServer().getPluginManager());
    binder.bind(Path.class).annotatedWith(Names.named("data")).toInstance(this.getDataFolder().toPath());
  }
}
