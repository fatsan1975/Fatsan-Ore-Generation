package com.fatsan1975.fug.model;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public enum HostFamily {
  STONE,
  DEEPSLATE,
  NETHERRACK,
  END_STONE;

  public static @Nullable HostFamily fromMaterial(final Material material) {
    return switch (material) {
      case STONE, GRANITE, DIORITE, ANDESITE, TUFF, CALCITE, DRIPSTONE_BLOCK -> STONE;
      case DEEPSLATE -> DEEPSLATE;
      case NETHERRACK -> NETHERRACK;
      case END_STONE -> END_STONE;
      default -> null;
    };
  }
}
