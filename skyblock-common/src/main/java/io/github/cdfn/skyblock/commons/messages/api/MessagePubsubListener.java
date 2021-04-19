package io.github.cdfn.skyblock.commons.messages.api;

import com.google.inject.Inject;
import io.github.cdfn.skyblock.commons.config.RedisConfig;
import io.lettuce.core.pubsub.RedisPubSubListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagePubsubListener implements RedisPubSubListener<String, byte[]> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessagePubsubListener.class);
  private final RedisConfig config;
  private final MessageHandlerRegistry registry;

  @Inject
  public MessagePubsubListener(RedisConfig config, MessageHandlerRegistry registry) {
    this.config = config;
    this.registry = registry;
  }

  @Override
  public void message(String channel, byte[] message) {
  }

  @Override
  @SuppressWarnings("unchecked")
  public void message(String pattern, String channel, byte[] message) {
    // Strip channel's prefix so we get class name
    var className = channel.replace(config.getPrefix(), "");
    try {
      var clazz = Class.forName(className);

      // Safety check for further dirty hacks
      if (!MessagePackSerializable.class.isAssignableFrom(clazz)) {
        LOGGER.error("{} does not implement MessagePackSerializable", className);
        return;
      }

      MessagePackSerializable messagePackSerializable = ((Class<MessagePackSerializable>) clazz)
          .getDeclaredConstructor().newInstance();
      messagePackSerializable.deserialize(message);

      registry.callAll(clazz, messagePackSerializable);
    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | IOException e) {
      LOGGER.error("error while handling message on channel {} with classname {}", channel, className, e);
    }
  }

  @Override
  public void subscribed(String channel, long count) {
  }

  @Override
  public void psubscribed(String pattern, long count) {
  }

  @Override
  public void unsubscribed(String channel, long count) {
  }

  @Override
  public void punsubscribed(String pattern, long count) {
  }
}
