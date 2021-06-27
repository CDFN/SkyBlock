package io.github.cdfn.skyblock.module;

import com.google.inject.AbstractModule;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

public class SlimeWorldManagerModule extends AbstractModule {
  private final SlimePlugin swm;

  public SlimeWorldManagerModule(JavaPlugin plugin) {
    var swm = (SlimePlugin) plugin.getServer().getPluginManager().getPlugin("SlimeWorldManager");
    if(swm == null) {
      plugin.getServer().getPluginManager().disablePlugin(plugin);
      throw new UnknownDependencyException("SlimeWorldManager is not present, plugin can't work without it.");
    }
    this.swm = swm;
  }

  @Override
  protected void configure() {
    bind(SlimePlugin.class).toInstance(swm);
    bind(SlimeLoader.class).toInstance(swm.getLoader("mysql"));
  }
}
