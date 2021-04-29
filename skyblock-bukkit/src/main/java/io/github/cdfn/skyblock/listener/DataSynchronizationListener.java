package io.github.cdfn.skyblock.listener;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.cdfn.skyblock.commons.messages.util.StringByteCodec;
import io.github.cdfn.skyblock.util.EntityPlayerDataManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.IOException;
import java.util.Arrays;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSynchronizationListener implements Listener {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataSynchronizationListener.class);
  private static final String LOCK_FORMAT = "skyblock-%s-server";
  private static final String DATA_FORMAT = "skyblock-%s-playerdata";

  private final byte[] serverId;
  private final RedisCommands<String, byte[]> redisConnection;

  @Inject
  public DataSynchronizationListener(@Named("serverId") String serverId, RedisClient client) {
    this.serverId = serverId.getBytes();
    this.redisConnection = client.connect(StringByteCodec.INSTANCE).sync();
  }

  @EventHandler
  public void onPreLogin(AsyncPlayerPreLoginEvent event) {
    var uuid = event.getUniqueId();
    var formattedLock = String.format(LOCK_FORMAT, uuid);
    var formattedData = String.format(DATA_FORMAT, uuid);

    // Fetch previous lock before creating new one. We'll use it to distinguish whether player
    // is switching from other server or not
    var existingLock = this.redisConnection.get(formattedLock);
    // Update lock for new server
    this.redisConnection.set(formattedLock, this.serverId);

    if (existingLock == null) {
      // Load playerdata from redis as player joins from "outside" (isn't switching from other server)
      var data = this.redisConnection.get(formattedData);
      if(data == null) {
        LOGGER.warn("Player with UUID {} has no data in redis. He may be playing first time or it may be a bug.", uuid);
        return;
      }

      try {
        // Overwrite existing .dat no matter what with redis data.
        EntityPlayerDataManager.saveToDatFile(uuid, data);
      } catch (IOException e) {
        LOGGER.error("Failed to save DAT file for UUID {}", uuid, e);
        event.disallow(Result.KICK_OTHER, Component.text("Failed to load your playerdata. Try again or contact server admin.", NamedTextColor.DARK_RED));
      }
      return;
    }

    // TODO: Lock thread until we receive player's data. Timeout after some time & kick / fallback from stored data?
    LOGGER.info("Unimplemented join: switching");
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    var player = event.getPlayer();
    var uuid = player.getUniqueId();

    var formattedLock = String.format(LOCK_FORMAT, uuid);

    // Fetch if lock has changed. If it hasn't changed, it means player left whole network.
    // Lock would contain other server id if he just switched.
    var result = this.redisConnection.get(formattedLock);
    if (result == null) {
      throw new IllegalStateException(String.format("player %s was not in redis while playing on server", player.getName()));
    }
    if (!Arrays.equals(result, serverId)) {
      // This means player just switched to other server.
      // TODO: Send message to other servers with player's uuid & data.
      LOGGER.info("Unimplemented onQuit: switching");
      return;
    }
    this.redisConnection.del(formattedLock);
    this.redisConnection.set(String.format(DATA_FORMAT, uuid), EntityPlayerDataManager.readPlayerNBT(player));
  }

}
