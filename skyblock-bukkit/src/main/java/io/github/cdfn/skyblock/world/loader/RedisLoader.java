package io.github.cdfn.skyblock.world.loader;

import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import io.github.cdfn.skyblock.commons.messages.util.StringByteCodec;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.IOException;
import java.util.List;

public class RedisLoader implements SlimeLoader {

  private static final String WORLD_DATA_PREFIX = "skyblock_world_data_";
  private static final String WORLD_LOCK_PREFIX = "skyblock_world_lock_";
  private static final byte TRUE = 0x1;
  private static final byte FALSE = 0x0;

  private final RedisCommands<String, byte[]> connection;

  public RedisLoader(RedisClient client) {
    this.connection = client.connect(StringByteCodec.INSTANCE).sync();
  }

  @Override
  public byte[] loadWorld(String name, boolean readOnly) throws UnknownWorldException, WorldInUseException, IOException {
    if (!readOnly) {
      var lock = connection.get(WORLD_LOCK_PREFIX + name);
      if (lock == null) {
        throw new UnknownWorldException(name);
      }
      if (lock[0] == TRUE) {
        throw new WorldInUseException(name);
      }
    }

    var data = connection.get(WORLD_DATA_PREFIX + name);
    if (data == null) {
      throw new UnknownWorldException(name);
    }
    return data;
  }

  @Override
  public boolean worldExists(String name) throws IOException {
    return connection.get(WORLD_LOCK_PREFIX + name) != null;
  }

  @Override
  public List<String> listWorlds() throws IOException {
    return connection.keys(WORLD_LOCK_PREFIX + "*");
  }

  @Override
  public void saveWorld(String name, byte[] bytes, boolean lock) throws IOException {
    connection.set(WORLD_DATA_PREFIX + name, bytes);
    connection.set(WORLD_LOCK_PREFIX + name, new byte[]{lock ? TRUE : FALSE});
  }

  @Override
  public void unlockWorld(String name) throws UnknownWorldException, IOException {
    var exists = this.worldExists(name);
    if (!exists) {
      throw new UnknownWorldException(name);
    }
    connection.set(WORLD_LOCK_PREFIX + name, new byte[]{(byte) FALSE});
  }

  @Override
  public boolean isWorldLocked(String name) throws UnknownWorldException, IOException {
    var response = connection.get(WORLD_LOCK_PREFIX + name);
    if (response == null) {
      throw new UnknownWorldException(name);
    }
    return response[0] == TRUE;
  }

  @Override
  public void deleteWorld(String name) throws UnknownWorldException, IOException {
    var exists = this.worldExists(name);
    if (!exists) {
      throw new UnknownWorldException(name);
    }
    connection.del(WORLD_DATA_PREFIX + name, WORLD_LOCK_PREFIX + name);
  }
}
