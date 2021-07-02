package io.github.cdfn.skyblock.base;

import com.google.inject.Inject;
import io.github.cdfn.skyblock.api.base.Island;
import io.github.cdfn.skyblock.api.service.IslandService;
import io.github.cdfn.skyblock.base.service.BukkitIslandService;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class TestListener implements Listener {

  private final IslandService islandService;
  private final JavaPlugin plugin;
  private final BukkitScheduler scheduler;

  @Inject
  public TestListener(BukkitIslandService islandService, JavaPlugin plugin, BukkitScheduler scheduler) {
    this.islandService = islandService;
    this.plugin = plugin;
    this.scheduler = scheduler;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();

    islandService.deleteIsland("test_island_" + player.getName())
        .handle((ignored, ex) -> {
          if (ex != null) {
            ex.printStackTrace();
          }
          return null;
        })
        .thenRun(() -> {
          islandService.createIsland("test_island_" + player.getName(), player.getUniqueId())
              .handle((island, ex) -> {
                if (ex != null) {
                  ex.printStackTrace();
                  return Optional.<Island>empty();
                }
                return Optional.of(island);
              })
              .thenAccept(optionalIsland -> optionalIsland.ifPresentOrElse(
                  island -> {
                    scheduler.runTask(plugin, () -> {
                      player.teleport(plugin.getServer().getWorld(island.name()).getSpawnLocation());
                      player.sendMessage(Component.text(player.getWorld().getName()));
                    });
                  },
                  () -> player.sendMessage(Component.text("didnt work"))
              ));

        });
  }
}
