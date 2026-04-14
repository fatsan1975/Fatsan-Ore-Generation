package com.fatsan1975.fug.rule;

import com.fatsan1975.fug.config.PluginConfiguration;
import com.fatsan1975.fug.model.AirExposureMode;
import com.fatsan1975.fug.model.BiomeGroup;
import com.fatsan1975.fug.model.DistributionProfile;
import com.fatsan1975.fug.model.HostFamily;
import com.fatsan1975.fug.model.OreType;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

public record CompiledOreRule(
    @Nullable OreType oreType,
    boolean enabled,
    boolean rewriteExisting,
    boolean generateNewVeins,
    int attempts,
    double attemptsMultiplier,
    double frequencyMultiplier,
    double rarity,
    double spawnChance,
    double veinSizeMultiplier,
    int minVeinSize,
    int maxVeinSize,
    double irregularity,
    double density,
    boolean largeButRare,
    boolean smallButCommon,
    VerticalDistributionSettings verticalDistribution,
    @Nullable HostRules hostRules,
    AirExposureSettings airExposure,
    RealismSettings realism,
    double rawBlockChance,
    Material rawBlockMaterial
) {

  public CompiledOreRule copy() {
    return new CompiledOreRule(
        this.oreType,
        this.enabled,
        this.rewriteExisting,
        this.generateNewVeins,
        this.attempts,
        this.attemptsMultiplier,
        this.frequencyMultiplier,
        this.rarity,
        this.spawnChance,
        this.veinSizeMultiplier,
        this.minVeinSize,
        this.maxVeinSize,
        this.irregularity,
        this.density,
        this.largeButRare,
        this.smallButCommon,
        this.verticalDistribution,
        this.hostRules == null ? null : this.hostRules.copy(),
        this.airExposure,
        this.realism,
        this.rawBlockChance,
        this.rawBlockMaterial
    );
  }

  public int resolveAttempts(final Random random) {
    double scaled = this.attempts * this.attemptsMultiplier * this.frequencyMultiplier / Math.max(0.01D, this.rarity);
    if (this.largeButRare) {
      scaled *= 0.55D;
    }
    if (this.smallButCommon) {
      scaled *= 1.60D;
    }
    final int base = (int) Math.floor(scaled);
    final double fraction = scaled - base;
    return Math.max(0, base + (random.nextDouble() < fraction ? 1 : 0));
  }

  public int resolveVeinSize(final Random random) {
    int min = (int) Math.round(this.minVeinSize * this.veinSizeMultiplier);
    int max = (int) Math.round(this.maxVeinSize * this.veinSizeMultiplier);
    if (this.largeButRare) {
      min = (int) Math.round(min * 1.6D);
      max = (int) Math.round(max * 1.8D);
    }
    if (this.smallButCommon) {
      min = Math.max(1, (int) Math.round(min * 0.70D));
      max = Math.max(min, (int) Math.round(max * 0.78D));
    }
    if (max < min) {
      max = min;
    }
    return min + random.nextInt(Math.max(1, (max - min) + 1));
  }

  public double existingRetentionChance() {
    return Math.clamp(this.frequencyMultiplier / Math.max(0.05D, this.rarity) * this.spawnChance, 0.0D, 1.0D);
  }

  public double biomeRealismMultiplier(final NamespacedKey biomeKey) {
    if (!this.realism.enabled) {
      return 1.0D;
    }
    double modifier = 1.0D;
    if (BiomeGroup.MOUNTAINS.matches(biomeKey)) {
      modifier += this.realism.mountainBonus;
    }
    if (BiomeGroup.BADLANDS.matches(biomeKey)) {
      modifier += this.realism.badlandsBonus;
    }
    if (BiomeGroup.LUSH_CAVES.matches(biomeKey)) {
      modifier += this.realism.lushCavesBonus;
    }
    if (BiomeGroup.DRIPSTONE_CAVES.matches(biomeKey)) {
      modifier += this.realism.dripstoneCavesBonus;
    }
    if (BiomeGroup.NETHER.matches(biomeKey)) {
      modifier += this.realism.netherBonus;
    }
    if (BiomeGroup.END.matches(biomeKey)) {
      modifier -= this.realism.endPenalty;
    }
    if (BiomeGroup.DEEP_DARK.matches(biomeKey)) {
      modifier -= this.realism.deepDarkPenalty;
    }
    return Math.max(0.0D, modifier);
  }

  public double airExposureMultiplier(final int openFaces) {
    return switch (this.airExposure.mode) {
      case IGNORE -> 1.0D;
      case PENALIZE -> openFaces > this.airExposure.maxOpenFaces
          ? Math.max(0.0D, 1.0D - this.airExposure.penalty * (openFaces - this.airExposure.maxOpenFaces))
          : 1.0D;
      case BOOST -> openFaces >= this.airExposure.minOpenFaces ? 1.0D + this.airExposure.bonus : 1.0D;
      case FORBID -> openFaces > this.airExposure.maxOpenFaces ? 0.0D : 1.0D;
      case REQUIRE -> openFaces >= this.airExposure.minOpenFaces ? 1.0D + this.airExposure.bonus : 0.0D;
    };
  }

  public boolean hostAllowed(final Material hostMaterial) {
    if (this.hostRules == null) {
      return false;
    }
    return this.hostRules.isAllowed(hostMaterial);
  }

  public Material resolvePlacedMaterial(final Material hostMaterial, final double specialNoise) {
    if (this.oreType == null) {
      throw new IllegalStateException("Resolved ore type is missing.");
    }
    if (this.rawBlockChance > 0.0D && this.rawBlockMaterial != Material.AIR && specialNoise <= this.rawBlockChance) {
      return this.rawBlockMaterial;
    }
    final HostFamily family = HostFamily.fromMaterial(hostMaterial);
    return family == null ? this.oreType.resolvePlacementMaterial(HostFamily.STONE) : this.oreType.resolvePlacementMaterial(family);
  }

  public Builder toBuilder(final OreType targetType) {
    return new Builder(targetType, this);
  }

  public static Builder builder(final OreType oreType, final CompiledOreRule baseRule) {
    return new Builder(oreType, baseRule);
  }

  public record VerticalDistributionSettings(
      int minY,
      int maxY,
      int peakY,
      int secondaryPeakY,
      DistributionProfile profile
  ) {
    public int clampMin(final int worldMinHeight) {
      return Math.max(worldMinHeight, Math.min(this.minY, this.maxY));
    }

    public int clampMax(final int worldMaxHeight) {
      return Math.min(worldMaxHeight - 1, Math.max(this.minY, this.maxY));
    }

    public int pickY(final Random random, final int worldMinHeight, final int worldMaxHeight) {
      final int min = clampMin(worldMinHeight);
      final int max = clampMax(worldMaxHeight);
      if (max <= min) {
        return min;
      }
      final int range = max - min;
      return switch (this.profile) {
        case UNIFORM -> min + random.nextInt(range + 1);
        case TRIANGULAR -> min + (int) Math.round(range * ((random.nextDouble() + random.nextDouble()) * 0.5D));
        case TRAPEZOID -> {
          final double center = normalizedPeak(this.peakY, min, max);
          final double sample = Math.clamp(center + (random.nextDouble() - 0.5D) * 0.65D, 0.0D, 1.0D);
          yield min + (int) Math.round(range * sample);
        }
        case BOTTOM_HEAVY -> min + (int) Math.round(range * Math.pow(random.nextDouble(), 1.85D));
        case TOP_HEAVY -> min + (int) Math.round(range * (1.0D - Math.pow(random.nextDouble(), 1.85D)));
        case CENTER_HEAVY -> min + (int) Math.round(range * ((random.nextDouble() + random.nextDouble() + random.nextDouble()) / 3.0D));
        case DUAL_PEAK -> {
          final int chosenPeak = random.nextBoolean() ? this.peakY : this.secondaryPeakY;
          final double peak = normalizedPeak(chosenPeak, min, max);
          final double sample = Math.clamp(peak + random.nextGaussian() * 0.12D, 0.0D, 1.0D);
          yield min + (int) Math.round(range * sample);
        }
      };
    }

    private static double normalizedPeak(final int peak, final int min, final int max) {
      if (max <= min) {
        return 0.5D;
      }
      return Math.clamp((peak - min) / (double) (max - min), 0.0D, 1.0D);
    }
  }

  public record HostRules(
      EnumSet<HostFamily> families,
      Set<Material> allowedMaterials,
      Set<Material> forbiddenMaterials,
      boolean stoneFamilyAware
  ) {
    public HostRules copy() {
      return new HostRules(EnumSet.copyOf(this.families), Set.copyOf(this.allowedMaterials), Set.copyOf(this.forbiddenMaterials),
          this.stoneFamilyAware);
    }

    public boolean isAllowed(final Material material) {
      if (this.forbiddenMaterials.contains(material)) {
        return false;
      }
      if (!this.allowedMaterials.isEmpty() && this.allowedMaterials.contains(material)) {
        return true;
      }
      final HostFamily family = HostFamily.fromMaterial(material);
      return family != null && this.families.contains(family);
    }
  }

  public record AirExposureSettings(
      AirExposureMode mode,
      double penalty,
      double bonus,
      int maxOpenFaces,
      int minOpenFaces
  ) {
  }

  public record RealismSettings(
      boolean enabled,
      boolean requireValidHost,
      int minSolidNeighbors,
      double mountainBonus,
      double badlandsBonus,
      double lushCavesBonus,
      double dripstoneCavesBonus,
      double netherBonus,
      double endPenalty,
      double deepDarkPenalty,
      double tuffContextBonus
  ) {
  }

  public static final class Builder {
    private final OreType oreType;
    private boolean enabled;
    private boolean rewriteExisting;
    private boolean generateNewVeins;
    private int attempts;
    private double attemptsMultiplier;
    private double frequencyMultiplier;
    private double rarity;
    private double spawnChance;
    private double veinSizeMultiplier;
    private int minVeinSize;
    private int maxVeinSize;
    private double irregularity;
    private double density;
    private boolean largeButRare;
    private boolean smallButCommon;
    private VerticalDistributionSettings verticalDistribution;
    private HostRules hostRules;
    private AirExposureSettings airExposure;
    private RealismSettings realism;
    private double rawBlockChance;
    private Material rawBlockMaterial;

    Builder(final OreType oreType, final CompiledOreRule baseRule) {
      this.oreType = oreType;
      this.enabled = baseRule.enabled;
      this.rewriteExisting = baseRule.rewriteExisting;
      this.generateNewVeins = baseRule.generateNewVeins;
      this.attempts = baseRule.attempts;
      this.attemptsMultiplier = baseRule.attemptsMultiplier;
      this.frequencyMultiplier = baseRule.frequencyMultiplier;
      this.rarity = baseRule.rarity;
      this.spawnChance = baseRule.spawnChance;
      this.veinSizeMultiplier = baseRule.veinSizeMultiplier;
      this.minVeinSize = baseRule.minVeinSize;
      this.maxVeinSize = baseRule.maxVeinSize;
      this.irregularity = baseRule.irregularity;
      this.density = baseRule.density;
      this.largeButRare = baseRule.largeButRare;
      this.smallButCommon = baseRule.smallButCommon;
      this.verticalDistribution = baseRule.verticalDistribution;
      this.hostRules = baseRule.hostRules == null
          ? new HostRules(oreType.defaultHostFamilies(), Set.of(), Set.of(), true)
          : baseRule.hostRules.copy();
      this.airExposure = baseRule.airExposure;
      this.realism = baseRule.realism;
      this.rawBlockChance = baseRule.rawBlockChance;
      this.rawBlockMaterial = baseRule.rawBlockMaterial;
    }

    public Builder applyPatch(final PluginConfiguration.OreRulePatch patch) {
      if (patch.enabled() != null) {
        this.enabled = patch.enabled();
      }
      if (patch.rewriteExisting() != null) {
        this.rewriteExisting = patch.rewriteExisting();
      }
      if (patch.generateNewVeins() != null) {
        this.generateNewVeins = patch.generateNewVeins();
      }
      if (patch.frequencyMultiplier() != null) {
        this.frequencyMultiplier = patch.frequencyMultiplier();
      }
      if (patch.rarity() != null) {
        this.rarity = patch.rarity();
      }
      if (patch.spawnChance() != null) {
        this.spawnChance = patch.spawnChance();
      }
      if (patch.attempts() != null) {
        this.attempts = patch.attempts();
      }
      if (patch.attemptsMultiplier() != null) {
        this.attemptsMultiplier = patch.attemptsMultiplier();
      }
      if (patch.veinSizeMultiplier() != null) {
        this.veinSizeMultiplier = patch.veinSizeMultiplier();
      }
      if (patch.minVeinSize() != null) {
        this.minVeinSize = patch.minVeinSize();
      }
      if (patch.maxVeinSize() != null) {
        this.maxVeinSize = patch.maxVeinSize();
      }
      if (patch.irregularity() != null) {
        this.irregularity = patch.irregularity();
      }
      if (patch.density() != null) {
        this.density = patch.density();
      }
      if (patch.largeButRare() != null) {
        this.largeButRare = patch.largeButRare();
      }
      if (patch.smallButCommon() != null) {
        this.smallButCommon = patch.smallButCommon();
      }
      if (patch.verticalDistribution() != null) {
        this.applyVerticalPatch(patch.verticalDistribution());
      }
      if (patch.hostRules() != null) {
        this.applyHostPatch(patch.hostRules());
      }
      if (patch.airExposure() != null) {
        this.applyAirExposurePatch(patch.airExposure());
      }
      if (patch.realism() != null) {
        this.applyRealismPatch(patch.realism());
      }
      if (patch.special() != null) {
        this.applySpecialPatch(patch.special());
      }
      return this;
    }

    private void applyVerticalPatch(final PluginConfiguration.VerticalDistributionPatch patch) {
      this.verticalDistribution = new VerticalDistributionSettings(
          patch.minY() != null ? patch.minY() : this.verticalDistribution.minY,
          patch.maxY() != null ? patch.maxY() : this.verticalDistribution.maxY,
          patch.peakY() != null ? patch.peakY() : this.verticalDistribution.peakY,
          patch.secondaryPeakY() != null ? patch.secondaryPeakY() : this.verticalDistribution.secondaryPeakY,
          patch.profile() != null ? patch.profile() : this.verticalDistribution.profile
      );
    }

    private void applyHostPatch(final PluginConfiguration.HostRulesPatch patch) {
      this.hostRules = new HostRules(
          patch.families() != null && !patch.families().isEmpty() ? EnumSet.copyOf(patch.families()) : this.hostRules.families,
          patch.allowedMaterials() != null ? Set.copyOf(patch.allowedMaterials()) : this.hostRules.allowedMaterials,
          patch.forbiddenMaterials() != null ? Set.copyOf(patch.forbiddenMaterials()) : this.hostRules.forbiddenMaterials,
          patch.stoneFamilyAware() != null ? patch.stoneFamilyAware() : this.hostRules.stoneFamilyAware
      );
    }

    private void applyAirExposurePatch(final PluginConfiguration.AirExposurePatch patch) {
      this.airExposure = new AirExposureSettings(
          patch.mode() != null ? patch.mode() : this.airExposure.mode,
          patch.penalty() != null ? patch.penalty() : this.airExposure.penalty,
          patch.bonus() != null ? patch.bonus() : this.airExposure.bonus,
          patch.maxOpenFaces() != null ? patch.maxOpenFaces() : this.airExposure.maxOpenFaces,
          patch.minOpenFaces() != null ? patch.minOpenFaces() : this.airExposure.minOpenFaces
      );
    }

    private void applyRealismPatch(final PluginConfiguration.RealismPatch patch) {
      this.realism = new RealismSettings(
          patch.enabled() != null ? patch.enabled() : this.realism.enabled,
          patch.requireValidHost() != null ? patch.requireValidHost() : this.realism.requireValidHost,
          patch.minSolidNeighbors() != null ? patch.minSolidNeighbors() : this.realism.minSolidNeighbors,
          patch.mountainBonus() != null ? patch.mountainBonus() : this.realism.mountainBonus,
          patch.badlandsBonus() != null ? patch.badlandsBonus() : this.realism.badlandsBonus,
          patch.lushCavesBonus() != null ? patch.lushCavesBonus() : this.realism.lushCavesBonus,
          patch.dripstoneCavesBonus() != null ? patch.dripstoneCavesBonus() : this.realism.dripstoneCavesBonus,
          patch.netherBonus() != null ? patch.netherBonus() : this.realism.netherBonus,
          patch.endPenalty() != null ? patch.endPenalty() : this.realism.endPenalty,
          patch.deepDarkPenalty() != null ? patch.deepDarkPenalty() : this.realism.deepDarkPenalty,
          patch.tuffContextBonus() != null ? patch.tuffContextBonus() : this.realism.tuffContextBonus
      );
    }

    private void applySpecialPatch(final PluginConfiguration.SpecialPatch patch) {
      if (patch.rawBlockChance() != null) {
        this.rawBlockChance = patch.rawBlockChance();
      }
      if (patch.rawBlockMaterial() != null) {
        this.rawBlockMaterial = patch.rawBlockMaterial();
      }
    }

    public CompiledOreRule build() {
      return new CompiledOreRule(
          this.oreType,
          this.enabled,
          this.rewriteExisting,
          this.generateNewVeins,
          Math.max(0, this.attempts),
          Math.max(0.0D, this.attemptsMultiplier),
          Math.max(0.0D, this.frequencyMultiplier),
          Math.max(0.05D, this.rarity),
          Math.clamp(this.spawnChance, 0.0D, 1.0D),
          Math.max(0.05D, this.veinSizeMultiplier),
          Math.max(1, this.minVeinSize),
          Math.max(1, this.maxVeinSize),
          Math.clamp(this.irregularity, 0.0D, 2.0D),
          Math.clamp(this.density, 0.05D, 1.0D),
          this.largeButRare,
          this.smallButCommon,
          this.verticalDistribution,
          Objects.requireNonNull(this.hostRules, "hostRules"),
          this.airExposure,
          this.realism,
          Math.clamp(this.rawBlockChance, 0.0D, 1.0D),
          this.rawBlockMaterial
      );
    }
  }
}
