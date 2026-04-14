package com.fatsan1975.fug.generation;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public interface GenerationRegion {
  Material getType(int x, int y, int z);

  void setType(int x, int y, int z, Material material);

  NamespacedKey getBiomeKey(int x, int y, int z);

  boolean isInRegion(int x, int y, int z);
}
