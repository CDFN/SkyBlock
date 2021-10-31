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
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.AdvancementDataPlayer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.stats.ServerStatisticManager;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityPlayerDataManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(EntityPlayerDataManager.class);
  private static final Method FILE_TO_PATH_METHOD;

  private static final Field ADVANCEMENT_DATA_PLAYER_FILE_FIELD;
  private static final Method SERVER_STATISTIC_MANAGER_TO_JSON_STRING_METHOD;

  static {
    try {
      FILE_TO_PATH_METHOD = File.class.getMethod("toPath");

      ADVANCEMENT_DATA_PLAYER_FILE_FIELD = AdvancementDataPlayer.class.getDeclaredField("g");
      ADVANCEMENT_DATA_PLAYER_FILE_FIELD.setAccessible(true);

      SERVER_STATISTIC_MANAGER_TO_JSON_STRING_METHOD = ServerStatisticManager.class.getDeclaredMethod("b");
      SERVER_STATISTIC_MANAGER_TO_JSON_STRING_METHOD.setAccessible(true);
    } catch (NoSuchMethodException | NoSuchFieldException e) {
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

    Files.createDirectories(playerDataFilePath.getParent());
    Files.createDirectories(advancementsFilePath.getParent());
    Files.createDirectories(statisticsFilePath.getParent());

    Files.write(playerDataFilePath, playerData.data());
    Files.write(advancementsFilePath, playerData.advancements());
    Files.write(statisticsFilePath, playerData.statistics());
  }

  public static PlayerData readPlayerData(Player player) {
    var craftPlayer = (CraftPlayer) player;
    var entityPlayer = craftPlayer.getHandle();

    CompletableFuture<byte[]> advancementDataFuture = CompletableFuture.supplyAsync(() -> {
      try {
        AdvancementDataPlayer advancementDataPlayer = entityPlayer.getAdvancementData();
        // Save advancements to file
        advancementDataPlayer.b();
        return Files.readAllBytes((Path) FILE_TO_PATH_METHOD.invoke(ADVANCEMENT_DATA_PLAYER_FILE_FIELD.get(advancementDataPlayer)));
      } catch (InvocationTargetException | IllegalAccessException | IOException e) {
        player.kick(Component.text("Failed to save your advancement data. Contact server admin.", NamedTextColor.DARK_RED));
        LOGGER.error("error while saving or reading {}'s advancements.", player.getName());
        return null;
      }
    });

    CompletableFuture<byte[]> statisticsDataFuture = CompletableFuture.supplyAsync(() -> {
      try {
        var statisticManager = entityPlayer.getStatisticManager();
        var json = (String) SERVER_STATISTIC_MANAGER_TO_JSON_STRING_METHOD.invoke(statisticManager);
        return json.getBytes();
      } catch (InvocationTargetException | IllegalAccessException e) {
        player.kick(Component.text("Failed to save your advancement data. Contact server admin.", NamedTextColor.DARK_RED));
        LOGGER.error("error while saving or reading {}'s statistics.", player.getName());
        return null;
      }
    });

    CompletableFuture<byte[]> playerDataFuture = CompletableFuture.supplyAsync(() -> {
      try (var outputStream = new ByteArrayOutputStream()) {
        var nbtTagCompound = entityPlayer.save(new NBTTagCompound());
        // We don't want to persist player's location, defaults to 0/0/0 when loaded
        nbtTagCompound.remove("Pos");
        NBTCompressedStreamTools.a(nbtTagCompound, outputStream);

        return outputStream.toByteArray();
      } catch (IOException e) {
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
