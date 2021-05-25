package io.github.cdfn.skyblock.datasync.util;

import io.github.cdfn.skyblock.datasync.PlayerData;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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

  private static final Method FILE_TO_PATH_METHOD;

  private static final Class<?> NBT_TAG_COMPOUND_CLASS;
  private static final Constructor<?> NBT_TAG_COMPOUND_CONSTRUCTOR;

  private static final Class<?> CRAFT_PLAYER_CLASS;
  private static final Method CRAFT_PLAYER_GET_HANDLE_METHOD;

  private static final Class<?> ENTITY_PLAYER_CLASS;
  private static final Method ENTITY_PLAYER_SAVE_METHOD;
  private static final Method ENTITY_PLAYER_GET_ADVANCEMENT_DATA_METHOD;
  private static final Method ENTITY_PLAYER_GET_STATISTIC_MANAGER_METHOD;

  private static final Class<?> ADVANCEMENT_DATA_PLAYER_CLASS;
  private static final Field ADVANCEMENT_DATA_PLAYER_FILE_FIELD;
  private static final Method ADVANCEMENT_DATA_PLAYER_SAVE_TO_FILE_METHOD;

  private static final Class<?> SERVER_STATISTIC_MANAGER_CLASS;
  private static final Method SERVER_STATISTIC_MANAGER_TO_JSON_STRING_METHOD;

  private static final Class<?> NBT_COMPRESSED_STREAM_TOOLS_CLASS;
  private static final Method NBT_COMPRESSED_STREAM_TOOLS_TO_OUTPUT_STREAM_METHOD;

  static {
    try {
      FILE_TO_PATH_METHOD = File.class.getMethod("toPath");

      NBT_TAG_COMPOUND_CLASS = Class.forName(String.format(FORMAT, "NBTTagCompound"));
      NBT_TAG_COMPOUND_CONSTRUCTOR = NBT_TAG_COMPOUND_CLASS.getConstructor();

      CRAFT_PLAYER_CLASS = Class.forName(String.format("org.bukkit.craftbukkit.%s.entity.CraftPlayer", VERSION));
      CRAFT_PLAYER_GET_HANDLE_METHOD = CRAFT_PLAYER_CLASS.getMethod("getHandle");

      ENTITY_PLAYER_CLASS = Class.forName(String.format(FORMAT, "EntityPlayer"));
      ENTITY_PLAYER_SAVE_METHOD = ENTITY_PLAYER_CLASS.getMethod("save", NBT_TAG_COMPOUND_CLASS);
      ENTITY_PLAYER_GET_ADVANCEMENT_DATA_METHOD = ENTITY_PLAYER_CLASS.getMethod("getAdvancementData");
      ENTITY_PLAYER_GET_STATISTIC_MANAGER_METHOD = ENTITY_PLAYER_CLASS.getMethod("getStatisticManager");

      ADVANCEMENT_DATA_PLAYER_CLASS = Class.forName(String.format(FORMAT, "AdvancementDataPlayer"));
      ADVANCEMENT_DATA_PLAYER_FILE_FIELD = ADVANCEMENT_DATA_PLAYER_CLASS.getDeclaredField("f");
      ADVANCEMENT_DATA_PLAYER_FILE_FIELD.setAccessible(true);
      ADVANCEMENT_DATA_PLAYER_SAVE_TO_FILE_METHOD = ADVANCEMENT_DATA_PLAYER_CLASS.getMethod("b");

      SERVER_STATISTIC_MANAGER_CLASS = Class.forName(String.format(FORMAT, "ServerStatisticManager"));
      SERVER_STATISTIC_MANAGER_TO_JSON_STRING_METHOD = SERVER_STATISTIC_MANAGER_CLASS.getDeclaredMethod("b");
      SERVER_STATISTIC_MANAGER_TO_JSON_STRING_METHOD.setAccessible(true);

      NBT_COMPRESSED_STREAM_TOOLS_CLASS = Class.forName(String.format(FORMAT, "NBTCompressedStreamTools"));
      NBT_COMPRESSED_STREAM_TOOLS_TO_OUTPUT_STREAM_METHOD = NBT_COMPRESSED_STREAM_TOOLS_CLASS.getMethod("a", NBT_TAG_COMPOUND_CLASS, OutputStream.class);
    } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
      throw new RuntimeException("Your server software is most likely outdated. If you think that's a mistake, contact author.", e);
    }
  }

  // Replaces playerdata/{uuid}.dat, advancements/{uuid}.json and stats/{uuid}.json files with provided data.
  public static void saveToDatFile(UUID uuid, PlayerData playerData) throws IOException {
    var worldPath = Bukkit.getServer().getWorlds().get(0)
        .getWorldFolder()
        .toPath();

    var playerDataFilePath = worldPath.resolve(Path.of("playerdata", uuid + ".dat"));
    var advancementsFilePath = worldPath.resolve(Path.of("advancements", uuid + ".json"));
    var statisticsFilePath = worldPath.resolve(Path.of("stats", uuid + ".json"));

    Files.write(playerDataFilePath, playerData.data());
    Files.write(advancementsFilePath, playerData.advancements());
    Files.write(statisticsFilePath, playerData.statistics());
  }

  public static PlayerData readPlayerData(Player player) {
    Object entityPlayer;
    try {
      entityPlayer = CRAFT_PLAYER_GET_HANDLE_METHOD.invoke(player);
    } catch (InvocationTargetException | IllegalAccessException e) {
      LOGGER.error("error while getting {}'s handle", player.getName(), e);
      return null;
    }

    CompletableFuture<byte[]> advancementDataFuture = CompletableFuture.supplyAsync(() -> {
      try {
        Object advancementDataPlayer = ENTITY_PLAYER_GET_ADVANCEMENT_DATA_METHOD.invoke(entityPlayer);
        ADVANCEMENT_DATA_PLAYER_SAVE_TO_FILE_METHOD.invoke(advancementDataPlayer);
        return Files.readAllBytes((Path) FILE_TO_PATH_METHOD.invoke(ADVANCEMENT_DATA_PLAYER_FILE_FIELD.get(advancementDataPlayer)));
      } catch (InvocationTargetException | IllegalAccessException | IOException e) {
        player.kick(Component.text("Failed to save your advancement data. Contact server admin.", NamedTextColor.DARK_RED));
        LOGGER.error("error while saving or reading {}'s advancements.", player.getName());
        return null;
      }
    });

    CompletableFuture<byte[]> statisticsDataFuture = CompletableFuture.supplyAsync(() -> {
      try{
        Object statisticManager = ENTITY_PLAYER_GET_STATISTIC_MANAGER_METHOD.invoke(entityPlayer);
        var json = (String) SERVER_STATISTIC_MANAGER_TO_JSON_STRING_METHOD.invoke(statisticManager);
        return json.getBytes();
      } catch (InvocationTargetException | IllegalAccessException e) {
        player.kick(Component.text("Failed to save your advancement data. Contact server admin.", NamedTextColor.DARK_RED));
        LOGGER.error("error while saving or reading {}'s statistics.", player.getName());
        return null;
      }
    });

    CompletableFuture<byte[]> playerDataFuture = CompletableFuture.supplyAsync(() -> {
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        var nbtTagCompound = ENTITY_PLAYER_SAVE_METHOD.invoke(entityPlayer, NBT_TAG_COMPOUND_CONSTRUCTOR.newInstance());
        NBT_COMPRESSED_STREAM_TOOLS_TO_OUTPUT_STREAM_METHOD.invoke(null, nbtTagCompound, outputStream);

        return outputStream.toByteArray();
      } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
        player.kick(Component.text("Failed to save your data. Contact server admin.", NamedTextColor.DARK_RED));
        LOGGER.error("Failed to save data for UUID {}", player.getUniqueId(), e);
        return null;
      }
    });

    try {
      CompletableFuture.allOf(advancementDataFuture, statisticsDataFuture, playerDataFuture).get();
      return new PlayerData(playerDataFuture.get(), advancementDataFuture.get(), statisticsDataFuture.get());
    } catch (ExecutionException | InterruptedException e) {
      LOGGER.error("error while waiting for {}'s data", player.getName(), e);
      return null;
    }
  }

}
