package io.github.cdfn.skyblock.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityPlayerDataManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(EntityPlayerDataManager.class);
  private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
  private static final String FORMAT = "net.minecraft.server." + VERSION + ".%s";

  private static final Class<?> NBT_TAG_COMPOUND_CLASS;
  private static final Constructor<?> NBT_TAG_COMPOUND_CONSTRUCTOR;

  private static final Class<?> CRAFT_PLAYER_CLASS;
  private static final Method CRAFT_PLAYER_GET_HANDLE_METHOD;

  private static final Class<?> ENTITY_PLAYER_CLASS;
  private static final Method ENTITY_PLAYER_SAVE_METHOD;

  private static final Class<?> NBT_COMPRESSED_STREAM_TOOLS_CLASS;
  private static final Method NBT_COMPRESSED_STREAM_TOOLS_TO_OUTPUT_STREAM_METHOD;

  static {
    try {
      NBT_TAG_COMPOUND_CLASS = Class.forName(String.format(FORMAT, "NBTTagCompound"));
      NBT_TAG_COMPOUND_CONSTRUCTOR = NBT_TAG_COMPOUND_CLASS.getConstructor();

      CRAFT_PLAYER_CLASS = Class.forName(String.format("org.bukkit.craftbukkit.%s.entity.CraftPlayer", VERSION));
      CRAFT_PLAYER_GET_HANDLE_METHOD = CRAFT_PLAYER_CLASS.getMethod("getHandle");

      ENTITY_PLAYER_CLASS = Class.forName(String.format(FORMAT, "EntityPlayer"));
      ENTITY_PLAYER_SAVE_METHOD = ENTITY_PLAYER_CLASS.getMethod("save", NBT_TAG_COMPOUND_CLASS);

      NBT_COMPRESSED_STREAM_TOOLS_CLASS = Class.forName(String.format(FORMAT, "NBTCompressedStreamTools"));
      NBT_COMPRESSED_STREAM_TOOLS_TO_OUTPUT_STREAM_METHOD = NBT_COMPRESSED_STREAM_TOOLS_CLASS.getMethod("a", NBT_TAG_COMPOUND_CLASS, OutputStream.class);
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      throw new RuntimeException("Your server software is most likely outdated. If you think that's a mistake, contact author.", e);
    }
  }

  // Replaces {uuid}.dat file with provided data.
  public static void saveToDatFile(UUID uuid, byte[] nbt) throws IOException {
    var playerDataFilePath = Bukkit.getServer().getWorlds().get(0)
        .getWorldFolder()
        .toPath()
        .resolve(Path.of("playerdata", uuid.toString() + ".dat"));
      Files.write(playerDataFilePath, nbt);
  }

  public static byte[] readPlayerNBT(Player player) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      var nbtTagCompound = ENTITY_PLAYER_SAVE_METHOD.invoke(CRAFT_PLAYER_GET_HANDLE_METHOD.invoke(player), NBT_TAG_COMPOUND_CONSTRUCTOR.newInstance());
      NBT_COMPRESSED_STREAM_TOOLS_TO_OUTPUT_STREAM_METHOD.invoke(null, nbtTagCompound, outputStream);

      return outputStream.toByteArray();
    } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
      player.kick(Component.text("Failed to save your data. Contact server admin.", NamedTextColor.DARK_RED));
      LOGGER.error("Failed to save data for UUID {}", player.getUniqueId(), e);
    }
    return null;
  }

}
