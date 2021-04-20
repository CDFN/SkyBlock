package io.github.cdfn.skyblock;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import io.github.cdfn.skyblock.commons.config.WorkerConfig;
import io.github.cdfn.skyblock.commons.messages.ConfigMessages.ConfigRequest;
import io.github.cdfn.skyblock.commons.messages.ConfigMessages.ConfigResponse;
import io.github.cdfn.skyblock.commons.messages.api.MessageHandlerRegistry;
import io.github.cdfn.skyblock.commons.messages.api.MessagePublisher;
import io.github.cdfn.skyblock.commons.messages.api.MessagePubsubListener;
import io.github.cdfn.skyblock.commons.module.OkaeriConfigModule;
import io.github.cdfn.skyblock.commons.module.redis.RedisModule;
import io.github.cdfn.skyblock.messages.handler.ConfigResponseHandler;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
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
        new RedisModule(this.getDataFolder().toPath()),
        new OkaeriConfigModule<>(null, WorkerConfig.class)
    );

    var logger = this.getSLF4JLogger();
    var server = this.getServer();

    var client = injector.getInstance(RedisClient.class);
    try {
      var conn = client.connect().sync();
      logger.info("Redis response: {}", conn.ping());
    } catch (RedisConnectionException exception) {
      logger.error("Failed to connect to redis", exception);
      server.shutdown();
    }
    injector.getInstance(MessagePubsubListener.class).register();
    injector.getInstance(MessageHandlerRegistry.class).addHandler(
        ConfigResponse.class,
        injector.getInstance(ConfigResponseHandler.class)
    );
    MessagePublisher.create(client).publish(new ConfigRequest(ThreadLocalRandom.current().nextInt()));
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
