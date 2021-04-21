package io.github.cdfn.skyblock.commons.messages.api;

import io.github.cdfn.skyblock.commons.config.RedisConfig;
import io.github.cdfn.skyblock.commons.messages.util.StringByteCodec;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagePublisher {

  private static MessagePublisher instance;
  private static final Logger LOGGER = LoggerFactory.getLogger(MessagePublisher.class);
  private static RedisCommands<String, byte[]> commands;

  MessagePublisher() {
  }

  public void publish(MessagePackSerializable message) {
    var className = message.getClass().getName();
    try {
      commands.publish(
          RedisConfig.PREFIX + className,
          message.serialize().toByteArray()
      );
    } catch (IOException e) {
      LOGGER.error("error while publishing message {}", className, e);
    }
  }

  public static MessagePublisher get(RedisClient client) {
    if (instance == null) {
      initConnection(client);
      instance = new MessagePublisher();
    }
    return instance;
  }

  private static void initConnection(RedisClient client) {
    commands = client.connect(StringByteCodec.INSTANCE).sync();
  }
}
