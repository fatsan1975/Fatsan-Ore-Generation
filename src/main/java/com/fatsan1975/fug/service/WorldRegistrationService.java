package com.fatsan1975.fug.service;

import com.fatsan1975.fug.generation.OreGenerationPopulator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldRegistrationService implements Listener {
  private final JavaPlugin plugin;
  private final OreGenerationPopulator populator;
  private final Set<UUID> registeredWorlds = ConcurrentHashMap.newKeySet();

  public WorldRegistrationService(final JavaPlugin plugin, final OreGenerationPopulator populator) {
    this.plugin = plugin;
    this.populator = populator;
  }

  @EventHandler
  public void onWorldInit(final WorldInitEvent event) {
    this.register(event.getWorld());
  }

  public void registerLoadedWorlds() {
    for (final World world : Bukkit.getWorlds()) {
      this.register(world);
    }
  }

  public int registeredWorldCount() {
    return this.registeredWorlds.size();
  }

  private void register(final World world) {
    if (this.registeredWorlds.add(world.getUID()) && !world.getPopulators().contains(this.populator)) {
      world.getPopulators().add(this.populator);
    }
  }
}
