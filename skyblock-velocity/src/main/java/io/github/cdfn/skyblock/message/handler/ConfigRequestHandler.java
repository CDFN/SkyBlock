package io.github.cdfn.skyblock.message.handler;

import com.google.inject.Inject;
import io.github.cdfn.skyblock.commons.config.WorkerConfig;
import io.github.cdfn.skyblock.commons.message.ConfigMessages.ConfigRequest;
import io.github.cdfn.skyblock.commons.message.ConfigMessages.ConfigResponse;
import io.github.cdfn.skyblock.commons.message.api.MessagePublisher;
import io.github.cdfn.skyblock.commons.message.api.handler.MessageHandler;
import io.lettuce.core.RedisClient;

public record ConfigRequestHandler(@Inject RedisClient client, @Inject WorkerConfig workerConfig)
    implements MessageHandler<ConfigRequest> {

  @Override
  public void accept(ConfigRequest configRequest) {
    MessagePublisher.get(client).publish(new ConfigResponse(configRequest.getId(), workerConfig.saveToString()));
  }
}
