package io.github.cdfn.skyblock.commons.messages.api;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Singleton
public class MessageHandlerRegistry {

  @SuppressWarnings("rawtypes")
  private final Multimap<Class<?>, Consumer> consumerMap;

  public MessageHandlerRegistry() {
    this.consumerMap = ArrayListMultimap.create();
  }

  public <T extends MessagePackSerializable> void addHandler(Class<T> clazz, Consumer<T> consumer) {
    this.consumerMap.put(clazz, consumer);
  }

  @SuppressWarnings("unchecked")
  public void callAll(Class<?> clazz, MessagePackSerializable message) {
    this.consumerMap.get(clazz).forEach(consumer -> consumer.accept(message));
  }
}
