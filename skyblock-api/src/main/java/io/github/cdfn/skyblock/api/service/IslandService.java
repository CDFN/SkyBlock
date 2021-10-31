package io.github.cdfn.skyblock.api.service;

import io.github.cdfn.skyblock.api.base.Island;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandService {
  CompletableFuture<Island> createIsland(String name, UUID owner);
  CompletableFuture<Island> loadIsland(String name);
  CompletableFuture<Void> unloadIsland();
  CompletableFuture<Void> deleteIsland();
  CompletableFuture<Island> getIsland(String name);
}
