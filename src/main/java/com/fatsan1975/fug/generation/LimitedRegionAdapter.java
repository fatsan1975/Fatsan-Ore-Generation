package com.fatsan1975.fug.generation;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.generator.LimitedRegion;

public record LimitedRegionAdapter(LimitedRegion region) implements GenerationRegion {
  @Override
  public Material getType(final int x, final int y, final int z) {
    return this.region.getType(x, y, z);
  }

  @Override
  public void setType(final int x, final int y, final int z, final Material material) {
    this.region.setType(x, y, z, material);
  }

  @Override
  public NamespacedKey getBiomeKey(final int x, final int y, final int z) {
    return this.region.getBiome(x, y, z).getKey();
  }

  @Override
  public boolean isInRegion(final int x, final int y, final int z) {
    return this.region.isInRegion(x, y, z);
  }
}
