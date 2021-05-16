package io.github.cdfn.skyblock.commons.message.api.handler;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Singleton;
import io.github.cdfn.skyblock.commons.message.api.serializer.MessagePackSerializable;

@Singleton
public class MessageHandlerRegistry {

  @SuppressWarnings("rawtypes")
  private final Multimap<Class<?>, MessageHandler> consumerMap;

  public MessageHandlerRegistry() {
    this.consumerMap = ArrayListMultimap.create();
  }

  public <T extends MessagePackSerializable> void addHandler(Class<T> clazz, MessageHandler<T> consumer) {
    this.consumerMap.put(clazz, consumer);
  }

  @SuppressWarnings("unchecked")
  public void callAll(Class<?> clazz, MessagePackSerializable message) {
    var iterator = this.consumerMap.get(clazz).iterator();
    while (iterator.hasNext()) {
      var next = iterator.next();
      next.accept(message);
      if (next.isOneTime()) {
        iterator.remove();
      }
    }
  }
}
