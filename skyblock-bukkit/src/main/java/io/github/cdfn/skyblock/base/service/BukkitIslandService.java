package io.github.cdfn.skyblock.base.service;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldAlreadyExistsException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import io.github.cdfn.skyblock.api.base.Island;
import io.github.cdfn.skyblock.api.exception.IslandUnloadException;
import io.github.cdfn.skyblock.api.exception.IslandLoadException;
import io.github.cdfn.skyblock.api.service.IslandService;
import io.github.cdfn.skyblock.base.BukkitIsland;
import io.github.cdfn.skyblock.module.SlimeWorldManagerModule;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class BukkitIslandService implements IslandService, Module {

  private static final SlimePropertyMap PROPERTIES = new SlimePropertyMap();
  private final JavaPlugin plugin;
  private final SlimePlugin slimePlugin;
  private final SlimeLoader slimeLoader;
  private final BukkitScheduler scheduler;

  @Inject
  public BukkitIslandService(JavaPlugin plugin, SlimePlugin slimePlugin, SlimeLoader slimeLoader, BukkitScheduler scheduler) {
    this.plugin = plugin;
    this.slimePlugin = slimePlugin;
    this.slimeLoader = slimeLoader;
    this.scheduler = scheduler;
  }

  @Override
  public CompletableFuture<Island> createIsland(String name, UUID owner) {
    var cf = new CompletableFuture<Island>();
    scheduler.runTaskAsynchronously(plugin, task -> {
      try {
        slimePlugin.createEmptyWorld(slimeLoader, name, false, PROPERTIES);
        this.loadIsland(name)
            .handle((island, e) -> {
              if (e != null) {
                cf.completeExceptionally(e);
              }
              return island;
            })
            .thenAccept(cf::complete);
      } catch (WorldAlreadyExistsException e) {
        cf.completeExceptionally(new IslandLoadException(String.format("world %s already exists", name), e));
      } catch (IOException e) {
        cf.completeExceptionally(new IslandLoadException(String.format("failed to create world %s", name), e));
      }
    });

    return cf;
  }

  @Override
  public CompletableFuture<Island> loadIsland(String name) {
    var cf = new CompletableFuture<Island>();
    scheduler.runTaskAsynchronously(plugin, task -> {
      try {
        var slimeWorld = slimePlugin.loadWorld(slimeLoader, name, false, PROPERTIES);
        // sync generation
        scheduler.runTask(plugin, () -> {
          slimePlugin.generateWorld(slimeWorld);
          cf.complete(new BukkitIsland(slimeWorld.getName(), plugin.getServer().getWorld(slimeWorld.getName())));
        });
      } catch (UnknownWorldException e) {
        cf.completeExceptionally(new IslandLoadException(String.format("unknown world %s", name), e));
      } catch (WorldInUseException e) {
        cf.completeExceptionally(new IslandLoadException(String.format("world %s is being used by other instance", name), e));
      } catch (IOException e) {
        cf.completeExceptionally(new IslandLoadException(String.format("error while loading world %s", name), e));
      } catch (CorruptedWorldException e) {
        cf.completeExceptionally(new IslandLoadException(String.format("world %s seems to be corrupted", name), e));
      } catch (NewerFormatException e) {
        cf.completeExceptionally(new IslandLoadException(String.format("world %s seems to be using newer format", name), e));
      }
    });
    return cf;
  }

  @Override
  public CompletableFuture<Void> unloadIsland(String name) {
    var cf = new CompletableFuture<Void>();
    scheduler.runTaskAsynchronously(plugin, task -> {
      try {
        slimeLoader.unlockWorld(name);
        cf.complete(null);
      } catch (UnknownWorldException e) {
        cf.completeExceptionally(new IslandUnloadException(String.format("no world %s", name), e));
      } catch (IOException e) {
        cf.completeExceptionally(new IslandUnloadException(String.format("failed to unload world %s", name), e));
      }
    });
    return cf;
  }

  @Override
  public CompletableFuture<Void> deleteIsland(String name) {
    var cf = new CompletableFuture<Void>();
    scheduler.runTaskAsynchronously(plugin, task ->
        this.unloadIsland(name).thenRun(() -> {
          try {
            slimeLoader.deleteWorld(name);
            cf.complete(null);
          } catch (UnknownWorldException e) {
            cf.completeExceptionally(new IslandUnloadException(String.format("world %s doesn't exist", name), e));
          } catch (IOException e) {
            cf.completeExceptionally(new IslandUnloadException(String.format("error while deleting world %s", name), e));
          }
        }));
    return cf;
  }

  @Override
  public void configure(Binder binder) {
    binder.install(new SlimeWorldManagerModule(plugin));
    binder.bind(IslandService.class).toInstance(this);
  }
}
