package io.github.cdfn.skyblock.messages.handler;

import com.google.inject.Inject;
import io.github.cdfn.skyblock.commons.config.WorkerConfig;
import io.github.cdfn.skyblock.commons.messages.ConfigMessages.ConfigResponse;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigResponseHandler implements Consumer<ConfigResponse> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigResponseHandler.class);
  private final WorkerConfig workerConfig;

  @Inject
  public ConfigResponseHandler(WorkerConfig workerConfig) {
    this.workerConfig = workerConfig;
  }

  @Override
  public void accept(ConfigResponse configResponse) {
    workerConfig.load(configResponse.getData());
    LOGGER.info("Loaded config from manager");
  }
}
