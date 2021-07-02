package io.github.cdfn.skyblock.base;

import io.github.cdfn.skyblock.api.base.Island;
import org.bukkit.World;

public class BukkitIsland implements Island {
  private String worldName;
  private World world;

  public BukkitIsland(String name, World world) {
    this.worldName = name;
    this.world = world;
  }
  @Override
  public String name() {
    return this.worldName;
  }

  @Override
  public World world() {
    return world;
  }
}
