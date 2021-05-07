package io.github.cdfn.skyblock.commons.messages.api;

import io.github.cdfn.skyblock.commons.messages.util.sneaky.SneakyBiConsumer;
import io.github.cdfn.skyblock.commons.messages.util.sneaky.SneakyFunction;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unchecked"})
public interface AnnotationMessageSerializer extends MessagePackSerializable {

  Logger LOGGER = LoggerFactory.getLogger(AnnotationMessageSerializer.class);
  // Map<Class, Pair<SerializationStrategy, DeserializationStrategy>>
  Map<Class<?>, Map.Entry<BiConsumer, Function>> serializationStrategies = createStrategies();

  static Map<Class<?>, Map.Entry<BiConsumer, Function>> createStrategies() {
    var map = new HashMap<Class<?>, Map.Entry<BiConsumer, Function>>();
    map.put(BigInteger.class, Map.entry(
        SneakyBiConsumer.of((SneakyBiConsumer<MessageBufferPacker, BigInteger, IOException>) MessagePacker::packBigInteger),
        SneakyFunction.of((SneakyFunction<MessageUnpacker, BigInteger, IOException>) MessageUnpacker::unpackBigInteger)
    ));
    map.put(Boolean.class, Map.entry(
        SneakyBiConsumer.of((SneakyBiConsumer<MessageBufferPacker, Boolean, IOException>) MessagePacker::packBoolean),
        SneakyFunction.of((SneakyFunction<MessageUnpacker, Boolean, IOException>) MessageUnpacker::unpackBoolean)
    ));
    map.put(Byte.class, Map.entry(
        SneakyBiConsumer.of((SneakyBiConsumer<MessageBufferPacker, Byte, IOException>) MessagePacker::packByte),
        SneakyFunction.of((SneakyFunction<MessageUnpacker, Byte, IOException>) MessageUnpacker::unpackByte)
    ));
    map.put(Double.class, Map.entry(
        SneakyBiConsumer.of((SneakyBiConsumer<MessageBufferPacker, Double, IOException>) MessagePacker::packDouble),
        SneakyFunction.of((SneakyFunction<MessageUnpacker, Double, IOException>) MessageUnpacker::unpackDouble)
    ));
    map.put(Float.class, Map.entry(
        SneakyBiConsumer.of((SneakyBiConsumer<MessageBufferPacker, Float, IOException>) MessagePacker::packFloat),
        SneakyFunction.of((SneakyFunction<MessageUnpacker, Float, IOException>) MessageUnpacker::unpackFloat)
    ));
    map.put(Integer.class, Map.entry(
        SneakyBiConsumer.of((SneakyBiConsumer<MessageBufferPacker, Integer, IOException>) MessagePacker::packInt),
        SneakyFunction.of((SneakyFunction<MessageUnpacker, Integer, IOException>) MessageUnpacker::unpackInt)
    ));
    map.put(Long.class, Map.entry(
        SneakyBiConsumer.of((SneakyBiConsumer<MessageBufferPacker, Long, IOException>) MessagePacker::packLong),
        SneakyFunction.of((SneakyFunction<MessageUnpacker, Long, IOException>) MessageUnpacker::unpackLong)
    ));
    map.put(Short.class, Map.entry(
        SneakyBiConsumer.of((SneakyBiConsumer<MessageBufferPacker, Short, IOException>) MessagePacker::packShort),
        SneakyFunction.of((SneakyFunction<MessageUnpacker, Short, IOException>) MessageUnpacker::unpackShort)
    ));
    map.put(String.class, Map.entry(
        SneakyBiConsumer.of((SneakyBiConsumer<MessageBufferPacker, String, IOException>) MessagePacker::packString),
        SneakyFunction.of((SneakyFunction<MessageUnpacker, String, IOException>) MessageUnpacker::unpackString)
    ));
    map.put(byte[].class, Map.entry(
        SneakyBiConsumer.of((SneakyBiConsumer<MessageBufferPacker, byte[], IOException>) (packer, bytes) -> {
          packer.packArrayHeader(bytes.length);
          packer.writePayload(bytes);
        }),
        SneakyFunction.of((SneakyFunction<MessageUnpacker, byte[], IOException>) (unpacker) -> unpacker.readPayload(unpacker.unpackArrayHeader()))
    ));
    return map;
  }

  @Override
  default byte[] serialize() {
    try (var packer = MessagePack.newDefaultBufferPacker()) {
      for (Field field : this.getClass().getDeclaredFields()) {
        field.setAccessible(true);
        // Skip fields without MessagePackField annotation
        if (field.getAnnotation(MessagePackField.class) == null) {
          continue;
        }

        Object o = field.get(this);
        Class<?> type = field.getType();
        if (o == null) {
          packer.packNil();
          continue;
        }

        var strategy = serializationStrategies.get(type).getKey();
        if (strategy == null) {
          throw new UnsupportedOperationException(String.format("unknown type %s in %s", field.getType().getName(), this.getClass().getName()));
        }

        // Pack object using packer
        strategy.accept(packer, o);
      }
      return packer.toByteArray();
    } catch (IllegalAccessException | IOException e) {
      LOGGER.error("Failed to pack message", e);
    }
    return null;
  }

  @Override
  default void deserialize(byte[] bytes) {
    try (var unpacker = MessagePack.newDefaultUnpacker(bytes)) {
      for (Field field : this.getClass().getDeclaredFields()) {
        field.setAccessible(true);
        // Skip fields without MessagePackField annotation
        if (field.getAnnotation(MessagePackField.class) == null) {
          continue;
        }

        Class<?> type = field.getType();

        var strategy = serializationStrategies.get(type).getValue();
        if (strategy == null) {
          throw new UnsupportedOperationException(String.format("unknown type %s in %s", field.getType().getName(), this.getClass().getName()));
        }

        // Unpack value using unpacker and set field's value to unpacked one
        var result = strategy.apply(unpacker);
        field.set(this, result);
      }
    } catch (IllegalAccessException | IOException e) {
      LOGGER.error("Failed to unpack message", e);
    }
  }
}
