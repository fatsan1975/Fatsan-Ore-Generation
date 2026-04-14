package com.fatsan1975.fug.generation;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public final class TestGenerationRegion implements GenerationRegion {
  private final int minX;
  private final int minY;
  private final int minZ;
  private final int maxX;
  private final int maxY;
  private final int maxZ;
  private final Material[][][] materials;
  private final NamespacedKey[][][] biomes;

  public TestGenerationRegion(
      final int minX,
      final int minY,
      final int minZ,
      final int maxX,
      final int maxY,
      final int maxZ,
      final Material fill,
      final NamespacedKey biome
  ) {
    this.minX = minX;
    this.minY = minY;
    this.minZ = minZ;
    this.maxX = maxX;
    this.maxY = maxY;
    this.maxZ = maxZ;
    this.materials = new Material[(maxX - minX) + 1][(maxY - minY) + 1][(maxZ - minZ) + 1];
    this.biomes = new NamespacedKey[(maxX - minX) + 1][(maxY - minY) + 1][(maxZ - minZ) + 1];
    for (int x = minX; x <= maxX; x++) {
      for (int y = minY; y <= maxY; y++) {
        for (int z = minZ; z <= maxZ; z++) {
          this.setType(x, y, z, fill);
          this.setBiome(x, y, z, biome);
        }
      }
    }
  }

  @Override
  public Material getType(final int x, final int y, final int z) {
    return this.materials[x - this.minX][y - this.minY][z - this.minZ];
  }

  @Override
  public void setType(final int x, final int y, final int z, final Material material) {
    this.materials[x - this.minX][y - this.minY][z - this.minZ] = material;
  }

  @Override
  public NamespacedKey getBiomeKey(final int x, final int y, final int z) {
    return this.biomes[x - this.minX][y - this.minY][z - this.minZ];
  }

  @Override
  public boolean isInRegion(final int x, final int y, final int z) {
    return x >= this.minX && x <= this.maxX
        && y >= this.minY && y <= this.maxY
        && z >= this.minZ && z <= this.maxZ;
  }

  public void setBiome(final int x, final int y, final int z, final NamespacedKey biome) {
    this.biomes[x - this.minX][y - this.minY][z - this.minZ] = biome;
  }
}
