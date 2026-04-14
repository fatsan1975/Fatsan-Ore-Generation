package com.fatsan1975.fug;

import java.util.UUID;
import java.util.Set;
import org.bukkit.FeatureFlag;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

public record TestWorldInfo(
    String name,
    UUID uid,
    World.Environment environment,
    long seed,
    int minHeight,
    int maxHeight
) implements WorldInfo {
  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public java.util.UUID getUID() {
    return this.uid;
  }

  @Override
  public World.Environment getEnvironment() {
    return this.environment;
  }

  @Override
  public long getSeed() {
    return this.seed;
  }

  @Override
  public int getMinHeight() {
    return this.minHeight;
  }

  @Override
  public int getMaxHeight() {
    return this.maxHeight;
  }

  @Override
  public Set<FeatureFlag> getFeatureFlags() {
    return Set.of(FeatureFlag.VANILLA);
  }

  @Override
  public BiomeProvider vanillaBiomeProvider() {
    return new BiomeProvider() {
      @Override
      public org.bukkit.block.Biome getBiome(
          final org.bukkit.generator.WorldInfo worldInfo,
          final int x,
          final int y,
          final int z
      ) {
        throw new UnsupportedOperationException("Not required in unit tests.");
      }

      @Override
      public java.util.List<org.bukkit.block.Biome> getBiomes(final org.bukkit.generator.WorldInfo worldInfo) {
        throw new UnsupportedOperationException("Not required in unit tests.");
      }
    };
  }
}
