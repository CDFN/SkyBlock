package io.github.cdfn.skyblock;


import com.google.inject.Inject;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.cdfn.skyblock.commons.config.WorkerConfig;
import io.github.cdfn.skyblock.commons.messages.ConfigMessages.ConfigRequest;
import io.github.cdfn.skyblock.commons.messages.api.MessageHandlerRegistry;
import io.github.cdfn.skyblock.commons.messages.api.MessagePublisher;
import io.github.cdfn.skyblock.commons.messages.api.MessagePubsubListener;
import io.github.cdfn.skyblock.commons.module.OkaeriConfigModule;
import io.github.cdfn.skyblock.commons.module.redis.RedisModule;
import io.github.cdfn.skyblock.messages.handler.ConfigRequestHandler;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

@Plugin(id = "skyblock", name = "SkyBlock", authors = {"CDFN"}, version = "1.0.0")
public class SkyBlockPlugin {

  private final ProxyServer server;
  private final Logger logger;
  private final Injector injector;

  @Inject
  public SkyBlockPlugin(ProxyServer server, Logger logger, Injector injector,
      @DataDirectory Path path) {
    this.server = server;
    this.logger = logger;
    this.injector = injector.createChildInjector(
        new RedisModule(path),
        new OkaeriConfigModule<>(path.resolve("worker.hjson"), WorkerConfig.class)
    );
  }

  @Subscribe
  public void onProxyInitialization(ProxyInitializeEvent event) {
    var client = this.injector.getInstance(RedisClient.class);
    this.setupMessaging(
        client,
        injector.getInstance(MessagePubsubListener.class),
        injector.getInstance(MessageHandlerRegistry.class)
    );
  }

  private void setupMessaging(RedisClient client, MessagePubsubListener listener, MessageHandlerRegistry registry) {
    try {
      var conn = client.connect().sync();
      logger.info("Redis response: {}", conn.ping());
    } catch (RedisConnectionException exception) {
      logger.error("Failed to connect to redis", exception);
      this.server.shutdown(Component.text("Failed to connect to Redis server"));
    }
    listener.register(client);
    registry.addHandler(
        ConfigRequest.class,
        injector.getInstance(ConfigRequestHandler.class)
    );
  }
}
