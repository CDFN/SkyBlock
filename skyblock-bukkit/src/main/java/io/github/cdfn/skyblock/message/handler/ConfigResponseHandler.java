package io.github.cdfn.skyblock.message.handler;

import com.google.inject.Inject;
import io.github.cdfn.skyblock.commons.config.WorkerConfig;
import io.github.cdfn.skyblock.commons.message.ConfigMessages.ConfigResponse;
import io.github.cdfn.skyblock.commons.message.api.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ConfigResponseHandler(@Inject WorkerConfig workerConfig) implements MessageHandler<ConfigResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigResponseHandler.class);

  @Override
  public void accept(ConfigResponse configResponse) {
    workerConfig.load(configResponse.getData());
    LOGGER.info("Loaded config from manager");
  }

  @Override
  public boolean isOneTime() {
    return true;
  }
}
