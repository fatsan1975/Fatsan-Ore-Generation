package com.fatsan1975.fug.model;

import org.bukkit.NamespacedKey;

public enum BiomeGroup {
  MOUNTAINS,
  BADLANDS,
  LUSH_CAVES,
  DRIPSTONE_CAVES,
  CAVES,
  NETHER,
  END,
  FROZEN,
  OCEAN,
  PLAINS,
  FOREST,
  JUNGLE,
  SWAMP,
  DEEP_DARK;

  public boolean matches(final NamespacedKey key) {
    final String value = key.getKey();
    return switch (this) {
      case MOUNTAINS -> value.equals("windswept_hills")
          || value.equals("windswept_gravelly_hills")
          || value.equals("windswept_forest")
          || value.equals("windswept_savanna")
          || value.equals("stony_peaks")
          || value.equals("jagged_peaks")
          || value.equals("frozen_peaks")
          || value.equals("snowy_slopes")
          || value.equals("grove")
          || value.equals("meadow");
      case BADLANDS -> value.contains("badlands");
      case LUSH_CAVES -> value.equals("lush_caves");
      case DRIPSTONE_CAVES -> value.equals("dripstone_caves");
      case CAVES -> value.endsWith("_caves") || value.equals("deep_dark");
      case NETHER -> value.equals("nether_wastes")
          || value.equals("warped_forest")
          || value.equals("crimson_forest")
          || value.equals("basalt_deltas")
          || value.equals("soul_sand_valley");
      case END -> value.startsWith("end_")
          || value.equals("the_end")
          || value.equals("small_end_islands")
          || value.equals("end_midlands")
          || value.equals("end_highlands")
          || value.equals("end_barrens");
      case FROZEN -> value.contains("snow")
          || value.contains("ice")
          || value.contains("frozen");
      case OCEAN -> value.contains("ocean")
          || value.contains("river")
          || value.contains("beach");
      case PLAINS -> value.equals("plains") || value.equals("sunflower_plains");
      case FOREST -> value.contains("forest")
          || value.contains("taiga")
          || value.contains("grove");
      case JUNGLE -> value.contains("jungle") || value.contains("bamboo");
      case SWAMP -> value.contains("swamp") || value.contains("mangrove");
      case DEEP_DARK -> value.equals("deep_dark");
    };
  }
}
