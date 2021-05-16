package io.github.cdfn.skyblock.commons.message.api;

import com.google.inject.Inject;
import io.github.cdfn.skyblock.commons.config.RedisConfig;
import io.github.cdfn.skyblock.commons.message.api.handler.MessageHandlerRegistry;
import io.github.cdfn.skyblock.commons.message.api.serializer.MessagePackSerializable;
import io.github.cdfn.skyblock.commons.message.util.StringByteCodec;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

public class MessagePubsubListener implements RedisPubSubListener<String, byte[]> {

  private static Unsafe unsafe;
  static {
    try {
      var field = Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      unsafe = (Unsafe) field.get(null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(MessagePubsubListener.class);
  private final MessageHandlerRegistry registry;

  @Inject
  public MessagePubsubListener(MessageHandlerRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void message(String channel, byte[] message) {
  }

  @Override
  public void message(String pattern, String channel, byte[] message) {
    // Strip channel's prefix so we get class name
    var className = channel.replace(RedisConfig.PREFIX, "");
    try {
      var clazz = Class.forName(className);

      // Safety check for further dirty hacks
      if (!MessagePackSerializable.class.isAssignableFrom(clazz)) {
        LOGGER.error("{} does not implement MessagePackSerializable", className);
        return;
      }

      var messagePackSerializable = (MessagePackSerializable) unsafe.allocateInstance(clazz);
      messagePackSerializable.deserialize(message);

      registry.callAll(clazz, messagePackSerializable);
    } catch (ClassNotFoundException | InstantiationException | IOException e) {
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

  public void register(RedisClient client) {
    var conn = client.connectPubSub(StringByteCodec.INSTANCE);
    conn.addListener(this);
    // Subscribe for all messages with prefix
    conn.sync().psubscribe(RedisConfig.PREFIX + "*");
  }
}
