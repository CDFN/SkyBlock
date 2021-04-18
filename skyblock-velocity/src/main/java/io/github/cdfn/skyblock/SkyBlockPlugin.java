package io.github.cdfn.skyblock;


import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(id = "skyblock", name = "SkyBlock", authors = {"CDFN"})
public class SkyBlockPlugin {

  private final ProxyServer server;
  private final Logger logger;

  @Inject
  public SkyBlockPlugin(ProxyServer server, Logger logger) {
    this.server = server;
    this.logger = logger;

  }

  @Subscribe
  public void onProxyInitialization(ProxyInitializeEvent event) {
  }
}
