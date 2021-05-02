package io.github.cdfn.skyblock;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import io.github.cdfn.skyblock.commons.config.WorkerConfig;
import io.github.cdfn.skyblock.commons.messages.ConfigMessages.ConfigRequest;
import io.github.cdfn.skyblock.commons.messages.ConfigMessages.ConfigResponse;
import io.github.cdfn.skyblock.messages.PlayerDataMessages.PlayerDataRequest;
import io.github.cdfn.skyblock.messages.PlayerDataMessages.PlayerDataResponse;
import io.github.cdfn.skyblock.commons.messages.api.MessageHandlerRegistry;
import io.github.cdfn.skyblock.commons.messages.api.MessagePublisher;
import io.github.cdfn.skyblock.commons.messages.api.MessagePubsubListener;
import io.github.cdfn.skyblock.commons.module.OkaeriConfigModule;
import io.github.cdfn.skyblock.commons.module.redis.RedisModule;
import io.github.cdfn.skyblock.listener.DataSynchronizationListener;
import io.github.cdfn.skyblock.messages.handler.ConfigResponseHandler;
import io.github.cdfn.skyblock.messages.handler.PlayerDataRequestHandler;
import io.github.cdfn.skyblock.messages.handler.PlayerDataResponseHandler;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import kr.entree.spigradle.annotations.PluginMain;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;
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
        new OkaeriConfigModule<>(null, WorkerConfig.class),
        binder -> binder.bind(String.class).annotatedWith(Names.named("serverId")).toInstance(this.getServerId())
    );

    var client = injector.getInstance(RedisClient.class);
    this.setupMessaging(
        injector.getInstance(MessagePubsubListener.class),
        injector.getInstance(MessageHandlerRegistry.class),
        client
    );
    MessagePublisher.get(client).publish(new ConfigRequest(ThreadLocalRandom.current().nextInt()));

    this.getServer().getPluginManager().registerEvents(injector.getInstance(DataSynchronizationListener.class), this);
    this.setupSlimeWorldManager();
  }

  private void setupSlimeWorldManager() {
    var slimePlugin = (SlimePlugin) this.getServer().getPluginManager().getPlugin("SlimeWorldManager");
    if (slimePlugin == null) {
      this.getServer().shutdown();
      throw new UnknownDependencyException("No SlimeWorldManager found. Install it!");
    }

    this.injector = injector.createChildInjector(binder -> {
      binder.bind(SlimeLoader.class).toInstance(slimePlugin.getLoader("mysql"));
      binder.bind(SlimePlugin.class).toInstance(slimePlugin);
    });
  }

  private void setupMessaging(MessagePubsubListener listener, MessageHandlerRegistry registry, RedisClient client) {
    var logger = this.getSLF4JLogger();
    try {
      var conn = client.connect().sync();
      logger.info("Redis response: {}", conn.ping());
    } catch (RedisConnectionException exception) {
      logger.error("Failed to connect to redis", exception);
      this.getServer().shutdown();
    }
    listener.register(client);
    registry.addHandler(
        ConfigResponse.class,
        injector.getInstance(ConfigResponseHandler.class)
    );
    registry.addHandler(
        PlayerDataRequest.class,
        injector.getInstance(PlayerDataRequestHandler.class)
    );
    registry.addHandler(
        PlayerDataResponse.class,
        injector.getInstance(PlayerDataResponseHandler.class)
    );
  }

  private String getServerId() {
    var serverIdFilePath = this.getDataFolder().toPath().resolve("server_id.bin");
    var serverIdFile = serverIdFilePath.toFile();
    try {
      if (!serverIdFile.exists()) {
        var random = UUID.randomUUID().toString();
        Files.write(serverIdFilePath, random.getBytes(StandardCharsets.UTF_8));
        return random;
      }
      return new String(Files.readAllBytes(serverIdFilePath));
    } catch (IOException e) {
      this.getSLF4JLogger().error("Error while loading server id, can't start without it.", e);
      this.getServer().shutdown();
    }
    return null;
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
