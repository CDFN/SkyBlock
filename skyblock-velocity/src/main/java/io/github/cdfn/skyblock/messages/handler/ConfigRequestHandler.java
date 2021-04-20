package io.github.cdfn.skyblock.messages.handler;

import com.google.inject.Inject;
import io.github.cdfn.skyblock.commons.messages.ConfigMessages.ConfigRequest;
import io.lettuce.core.RedisClient;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigRequestHandler implements Consumer<ConfigRequest> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRequestHandler.class);
  private final RedisClient client;

  @Inject
  public ConfigRequestHandler(RedisClient client) {
    this.client = client;
  }

  @Override
  public void accept(ConfigRequest configRequest) {
    LOGGER.info("Received: " + configRequest.getId());
  }
}
