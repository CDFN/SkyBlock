package io.github.cdfn.skyblock.api.base;

import java.util.UUID;
import org.bukkit.World;

public interface Island {
  String name();
  World world();
  UUID ownerUUID();
}
