package com.fatsan1975.fug.model;

import com.fatsan1975.fug.rule.CompiledOreRule;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public enum OreType {
  COAL(
      "coal",
      Material.COAL_ORE,
      Material.DEEPSLATE_COAL_ORE,
      EnumSet.of(World.Environment.NORMAL),
      EnumSet.of(HostFamily.STONE, HostFamily.DEEPSLATE),
      defaults(true, true, true, 18, 1.0, 1.0, 1.0, 1.0, 4, 17, 0.35, 0.82, false, true,
          -32, 160, 96, 64, DistributionProfile.TOP_HEAVY,
          air(com.fatsan1975.fug.model.AirExposureMode.IGNORE, 0.0, 0.0, 6, 0),
          realism(true, true, 4, 0.15, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.05),
          0.0, Material.AIR)
  ),
  IRON(
      "iron",
      Material.IRON_ORE,
      Material.DEEPSLATE_IRON_ORE,
      EnumSet.of(World.Environment.NORMAL),
      EnumSet.of(HostFamily.STONE, HostFamily.DEEPSLATE),
      defaults(true, true, true, 14, 1.0, 1.0, 1.0, 1.0, 4, 12, 0.42, 0.86, false, false,
          -64, 96, 16, -16, DistributionProfile.TRAPEZOID,
          air(com.fatsan1975.fug.model.AirExposureMode.PENALIZE, 0.2, 0.0, 2, 0),
          realism(true, true, 5, 0.25, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.20),
          0.015, Material.RAW_IRON_BLOCK)
  ),
  COPPER(
      "copper",
      Material.COPPER_ORE,
      Material.DEEPSLATE_COPPER_ORE,
      EnumSet.of(World.Environment.NORMAL),
      EnumSet.of(HostFamily.STONE, HostFamily.DEEPSLATE),
      defaults(true, true, true, 14, 1.0, 1.0, 1.0, 1.0, 5, 13, 0.45, 0.88, false, false,
          -16, 112, 48, 16, DistributionProfile.TRIANGULAR,
          air(com.fatsan1975.fug.model.AirExposureMode.PENALIZE, 0.12, 0.0, 2, 0),
          realism(true, true, 4, 0.0, 0.45, 0.0, 0.30, 0.0, 0.0, 0.0, 0.15),
          0.012, Material.RAW_COPPER_BLOCK)
  ),
  GOLD(
      "gold",
      Material.GOLD_ORE,
      Material.DEEPSLATE_GOLD_ORE,
      EnumSet.of(World.Environment.NORMAL),
      EnumSet.of(HostFamily.STONE, HostFamily.DEEPSLATE),
      defaults(true, true, true, 6, 1.0, 1.1, 1.0, 1.0, 4, 9, 0.40, 0.84, false, false,
          -64, 32, -16, -32, DistributionProfile.BOTTOM_HEAVY,
          air(com.fatsan1975.fug.model.AirExposureMode.PENALIZE, 0.18, 0.0, 2, 0),
          realism(true, true, 5, 0.0, 0.55, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08),
          0.0, Material.AIR)
  ),
  DIAMOND(
      "diamond",
      Material.DIAMOND_ORE,
      Material.DEEPSLATE_DIAMOND_ORE,
      EnumSet.of(World.Environment.NORMAL),
      EnumSet.of(HostFamily.STONE, HostFamily.DEEPSLATE),
      defaults(true, true, true, 8, 1.0, 1.25, 1.0, 0.92, 3, 8, 0.38, 0.80, false, false,
          -64, 16, -48, -56, DistributionProfile.BOTTOM_HEAVY,
          air(com.fatsan1975.fug.model.AirExposureMode.PENALIZE, 0.55, 0.0, 1, 0),
          realism(true, true, 5, 0.0, 0.0, 0.15, 0.12, 0.0, 0.0, 0.10, 0.0),
          0.0, Material.AIR)
  ),
  EMERALD(
      "emerald",
      Material.EMERALD_ORE,
      Material.DEEPSLATE_EMERALD_ORE,
      EnumSet.of(World.Environment.NORMAL),
      EnumSet.of(HostFamily.STONE, HostFamily.DEEPSLATE),
      defaults(true, true, true, 4, 1.0, 1.45, 1.0, 0.9, 1, 4, 0.28, 0.78, true, false,
          -16, 256, 128, 96, DistributionProfile.TRIANGULAR,
          air(com.fatsan1975.fug.model.AirExposureMode.PENALIZE, 0.30, 0.0, 1, 0),
          realism(true, true, 5, 1.05, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.05),
          0.0, Material.AIR)
  ),
  REDSTONE(
      "redstone",
      Material.REDSTONE_ORE,
      Material.DEEPSLATE_REDSTONE_ORE,
      EnumSet.of(World.Environment.NORMAL),
      EnumSet.of(HostFamily.STONE, HostFamily.DEEPSLATE),
      defaults(true, true, true, 9, 1.0, 1.0, 1.0, 1.0, 4, 10, 0.35, 0.85, false, false,
          -64, 16, -48, -56, DistributionProfile.BOTTOM_HEAVY,
          air(com.fatsan1975.fug.model.AirExposureMode.PENALIZE, 0.18, 0.0, 2, 0),
          realism(true, true, 4, 0.0, 0.0, 0.12, 0.0, 0.0, 0.0, 0.0, 0.06),
          0.0, Material.AIR)
  ),
  LAPIS(
      "lapis",
      Material.LAPIS_ORE,
      Material.DEEPSLATE_LAPIS_ORE,
      EnumSet.of(World.Environment.NORMAL),
      EnumSet.of(HostFamily.STONE, HostFamily.DEEPSLATE),
      defaults(true, true, true, 5, 1.0, 1.0, 1.0, 1.0, 3, 7, 0.32, 0.82, false, false,
          -64, 64, 0, -16, DistributionProfile.TRIANGULAR,
          air(com.fatsan1975.fug.model.AirExposureMode.IGNORE, 0.0, 0.0, 6, 0),
          realism(true, true, 4, 0.0, 0.0, 0.20, 0.15, 0.0, 0.0, 0.0, 0.0),
          0.0, Material.AIR)
  ),
  NETHER_QUARTZ(
      "nether_quartz",
      Material.NETHER_QUARTZ_ORE,
      null,
      EnumSet.of(World.Environment.NETHER),
      EnumSet.of(HostFamily.NETHERRACK),
      defaults(true, true, true, 16, 1.0, 1.0, 1.0, 1.0, 4, 12, 0.45, 0.88, false, true,
          10, 118, 64, 48, DistributionProfile.UNIFORM,
          air(com.fatsan1975.fug.model.AirExposureMode.IGNORE, 0.0, 0.0, 6, 0),
          realism(true, true, 3, 0.0, 0.0, 0.0, 0.0, 0.12, 0.0, 0.0, 0.0),
          0.0, Material.AIR)
  ),
  NETHER_GOLD(
      "nether_gold",
      Material.NETHER_GOLD_ORE,
      null,
      EnumSet.of(World.Environment.NETHER),
      EnumSet.of(HostFamily.NETHERRACK),
      defaults(true, true, true, 10, 1.0, 1.0, 1.0, 1.0, 3, 8, 0.42, 0.84, false, false,
          10, 116, 48, 36, DistributionProfile.UNIFORM,
          air(com.fatsan1975.fug.model.AirExposureMode.IGNORE, 0.0, 0.0, 6, 0),
          realism(true, true, 3, 0.0, 0.0, 0.0, 0.0, 0.18, 0.0, 0.0, 0.0),
          0.0, Material.AIR)
  ),
  ANCIENT_DEBRIS(
      "ancient_debris",
      Material.ANCIENT_DEBRIS,
      null,
      EnumSet.of(World.Environment.NETHER),
      EnumSet.of(HostFamily.NETHERRACK),
      defaults(true, true, true, 2, 1.0, 1.4, 1.0, 0.90, 1, 3, 0.20, 0.90, true, false,
          8, 22, 15, 119, DistributionProfile.DUAL_PEAK,
          air(com.fatsan1975.fug.model.AirExposureMode.FORBID, 1.0, 0.0, 0, 0),
          realism(true, true, 5, 0.0, 0.0, 0.0, 0.0, 0.20, 0.0, 0.0, 0.0),
          0.0, Material.AIR)
  );

  private static final Map<String, OreType> BY_KEY = Arrays.stream(values())
      .collect(Collectors.toMap(OreType::key, Function.identity()));
  private static final Map<Material, OreType> BY_EXISTING_MATERIAL = Arrays.stream(values())
      .flatMap(type -> type.existingMaterials.stream().map(material -> Map.entry(material, type)))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

  private final String key;
  private final Material normalMaterial;
  private final @Nullable Material deepslateMaterial;
  private final EnumSet<World.Environment> supportedEnvironments;
  private final EnumSet<HostFamily> defaultHostFamilies;
  private final Set<Material> existingMaterials;
  private final CompiledOreRule defaults;

  OreType(
      final String key,
      final Material normalMaterial,
      final @Nullable Material deepslateMaterial,
      final EnumSet<World.Environment> supportedEnvironments,
      final EnumSet<HostFamily> defaultHostFamilies,
      final CompiledOreRule defaults
  ) {
    this.key = key;
    this.normalMaterial = normalMaterial;
    this.deepslateMaterial = deepslateMaterial;
    this.supportedEnvironments = supportedEnvironments;
    this.defaultHostFamilies = defaultHostFamilies;
    this.existingMaterials = deepslateMaterial == null ? Set.of(normalMaterial) : Set.of(normalMaterial, deepslateMaterial);
    this.defaults = defaults;
  }

  public String key() {
    return this.key;
  }

  public Set<Material> existingMaterials() {
    return this.existingMaterials;
  }

  public boolean supports(final World.Environment environment) {
    return this.supportedEnvironments.contains(environment);
  }

  public EnumSet<HostFamily> defaultHostFamilies() {
    return EnumSet.copyOf(this.defaultHostFamilies);
  }

  public CompiledOreRule defaults() {
    return this.defaults.copy();
  }

  public Material resolvePlacementMaterial(final HostFamily hostFamily) {
    if (hostFamily == HostFamily.DEEPSLATE && this.deepslateMaterial != null) {
      return this.deepslateMaterial;
    }
    return this.normalMaterial;
  }

  public Material replacementMaterialForExisting(final Material oreMaterial) {
    if (oreMaterial == this.deepslateMaterial) {
      return Material.DEEPSLATE;
    }
    return switch (this) {
      case NETHER_QUARTZ, NETHER_GOLD, ANCIENT_DEBRIS -> Material.NETHERRACK;
      default -> Material.STONE;
    };
  }

  public static @Nullable OreType fromKey(final String input) {
    return BY_KEY.get(input.toLowerCase(Locale.ROOT));
  }

  public static @Nullable OreType fromExistingMaterial(final Material material) {
    return BY_EXISTING_MATERIAL.get(material);
  }

  private static CompiledOreRule defaults(
      final boolean enabled,
      final boolean rewriteExisting,
      final boolean generateNewVeins,
      final int attempts,
      final double attemptsMultiplier,
      final double rarity,
      final double frequencyMultiplier,
      final double spawnChance,
      final int minVeinSize,
      final int maxVeinSize,
      final double irregularity,
      final double density,
      final boolean largeButRare,
      final boolean smallButCommon,
      final int minY,
      final int maxY,
      final int peakY,
      final int secondaryPeakY,
      final DistributionProfile profile,
      final CompiledOreRule.AirExposureSettings airExposure,
      final CompiledOreRule.RealismSettings realism,
      final double rawBlockChance,
      final Material rawBlockMaterial
  ) {
    return new CompiledOreRule(
        null,
        enabled,
        rewriteExisting,
        generateNewVeins,
        attempts,
        attemptsMultiplier,
        frequencyMultiplier,
        rarity,
        spawnChance,
        1.0,
        minVeinSize,
        maxVeinSize,
        irregularity,
        density,
        largeButRare,
        smallButCommon,
        new CompiledOreRule.VerticalDistributionSettings(minY, maxY, peakY, secondaryPeakY, profile),
        null,
        airExposure,
        realism,
        rawBlockChance,
        rawBlockMaterial
    );
  }

  private static CompiledOreRule.AirExposureSettings air(
      final com.fatsan1975.fug.model.AirExposureMode mode,
      final double penalty,
      final double bonus,
      final int maxOpenFaces,
      final int minOpenFaces
  ) {
    return new CompiledOreRule.AirExposureSettings(mode, penalty, bonus, maxOpenFaces, minOpenFaces);
  }

  private static CompiledOreRule.RealismSettings realism(
      final boolean enabled,
      final boolean requireValidHost,
      final int minSolidNeighbors,
      final double mountainBonus,
      final double badlandsBonus,
      final double lushCavesBonus,
      final double dripstoneCavesBonus,
      final double netherBonus,
      final double endPenalty,
      final double deepDarkPenalty,
      final double tuffContextBonus
  ) {
    return new CompiledOreRule.RealismSettings(
        enabled,
        requireValidHost,
        minSolidNeighbors,
        mountainBonus,
        badlandsBonus,
        lushCavesBonus,
        dripstoneCavesBonus,
        netherBonus,
        endPenalty,
        deepDarkPenalty,
        tuffContextBonus
    );
  }
}
