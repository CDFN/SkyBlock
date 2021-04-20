package io.github.cdfn.skyblock.messages.handler;

import com.google.inject.Inject;
import io.github.cdfn.skyblock.commons.config.WorkerConfig;
import io.github.cdfn.skyblock.commons.messages.ConfigMessages;
import io.github.cdfn.skyblock.commons.messages.ConfigMessages.ConfigRequest;
import io.github.cdfn.skyblock.commons.messages.ConfigMessages.ConfigResponse;
import io.github.cdfn.skyblock.commons.messages.api.MessagePublisher;
import io.github.cdfn.skyblock.commons.messages.util.StringByteCodec;
import io.lettuce.core.RedisClient;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigRequestHandler implements Consumer<ConfigRequest> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRequestHandler.class);
  private final RedisClient client;
  private final WorkerConfig workerConfig;

  @Inject
  public ConfigRequestHandler(RedisClient client, WorkerConfig workerConfig) {
    this.client = client;
    this.workerConfig = workerConfig;
  }

  @Override
  public void accept(ConfigRequest configRequest) {
    MessagePublisher.create(client).publish(new ConfigResponse(configRequest.getId(), workerConfig.saveToString()));
  }
}