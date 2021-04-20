package io.github.cdfn.skyblock.commons.messages.api;

import io.github.cdfn.skyblock.commons.config.RedisConfig;
import io.github.cdfn.skyblock.commons.messages.util.StringByteCodec;
import io.lettuce.core.RedisClient;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagePublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessagePublisher.class);
  private final RedisClient client;

  MessagePublisher(RedisClient client) {
    this.client = client;
  }

  public void publish(MessagePackSerializable message) {
    var className = message.getClass().getName();
    try {
      this.client.connectPubSub(StringByteCodec.INSTANCE)
          .sync()
          .publish(
              RedisConfig.PREFIX + className,
              message.serialize().toByteArray()
          );
    } catch (IOException e) {
      LOGGER.error("error while publishing message {}", className, e);
    }
  }

  public static MessagePublisher create(RedisClient client) {
    return new MessagePublisher(client);
  }
}
