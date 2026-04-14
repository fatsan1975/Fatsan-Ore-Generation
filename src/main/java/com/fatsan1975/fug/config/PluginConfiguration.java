package com.fatsan1975.fug.config;

import com.fatsan1975.fug.model.AirExposureMode;
import com.fatsan1975.fug.model.BiomeGroup;
import com.fatsan1975.fug.model.DistributionProfile;
import com.fatsan1975.fug.model.HostFamily;
import com.fatsan1975.fug.model.OreType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public record PluginConfiguration(
    GlobalSettings globalSettings,
    WorldSettings worldSettings,
    OresConfiguration oresConfiguration,
    List<ConfigIssue> issues
) {

  public record WorldSettings(
      Set<String> explicitlyEnabledWorlds,
      Set<String> explicitlyDisabledWorlds,
      List<Pattern> includePatterns,
      List<Pattern> excludePatterns,
      EnumMap<World.Environment, EnvironmentEntry> environments,
      Map<String, NamedWorldEntry> namedWorlds
  ) {
  }

  public record EnvironmentEntry(boolean enabled, @Nullable String profile) {
  }

  public record NamedWorldEntry(@Nullable Boolean enabled, @Nullable String profile) {
  }

  public record OresConfiguration(
      ScopeConfig global,
      Map<String, ScopeConfig> profiles,
      EnumMap<World.Environment, ScopeConfig> environments,
      Map<String, ScopeConfig> worlds,
      List<BiomeRuleConfig> biomes
  ) {
  }

  public record ScopeConfig(OreRulePatch defaults, EnumMap<OreType, OreRulePatch> ores) {
    public static ScopeConfig empty() {
      return new ScopeConfig(OreRulePatch.empty(), new EnumMap<>(OreType.class));
    }
  }

  public record BiomeRuleConfig(BiomeSelectorConfig selector, ScopeConfig scope) {
  }

  public sealed interface BiomeSelectorConfig permits ExactBiomeSelectorConfig, NamespaceBiomeSelectorConfig,
      GroupBiomeSelectorConfig, WildcardBiomeSelectorConfig {
  }

  public record ExactBiomeSelectorConfig(String key) implements BiomeSelectorConfig {
  }

  public record NamespaceBiomeSelectorConfig(String namespace) implements BiomeSelectorConfig {
  }

  public record GroupBiomeSelectorConfig(BiomeGroup group) implements BiomeSelectorConfig {
  }

  public record WildcardBiomeSelectorConfig() implements BiomeSelectorConfig {
  }

  public record OreRulePatch(
      @Nullable Boolean enabled,
      @Nullable Boolean rewriteExisting,
      @Nullable Boolean generateNewVeins,
      @Nullable Double frequencyMultiplier,
      @Nullable Double rarity,
      @Nullable Double spawnChance,
      @Nullable Integer attempts,
      @Nullable Double attemptsMultiplier,
      @Nullable Double veinSizeMultiplier,
      @Nullable Integer minVeinSize,
      @Nullable Integer maxVeinSize,
      @Nullable Double irregularity,
      @Nullable Double density,
      @Nullable Boolean largeButRare,
      @Nullable Boolean smallButCommon,
      @Nullable VerticalDistributionPatch verticalDistribution,
      @Nullable HostRulesPatch hostRules,
      @Nullable AirExposurePatch airExposure,
      @Nullable RealismPatch realism,
      @Nullable SpecialPatch special
  ) {
    public static OreRulePatch empty() {
      return new OreRulePatch(null, null, null, null, null, null, null, null, null, null, null, null, null, null,
          null, null, null, null, null, null);
    }
  }

  public record VerticalDistributionPatch(
      @Nullable Integer minY,
      @Nullable Integer maxY,
      @Nullable Integer peakY,
      @Nullable Integer secondaryPeakY,
      @Nullable DistributionProfile profile
  ) {
  }

  public record HostRulesPatch(
      @Nullable Set<HostFamily> families,
      @Nullable Set<Material> allowedMaterials,
      @Nullable Set<Material> forbiddenMaterials,
      @Nullable Boolean stoneFamilyAware
  ) {
  }

  public record AirExposurePatch(
      @Nullable AirExposureMode mode,
      @Nullable Double penalty,
      @Nullable Double bonus,
      @Nullable Integer maxOpenFaces,
      @Nullable Integer minOpenFaces
  ) {
  }

  public record RealismPatch(
      @Nullable Boolean enabled,
      @Nullable Boolean requireValidHost,
      @Nullable Integer minSolidNeighbors,
      @Nullable Double mountainBonus,
      @Nullable Double badlandsBonus,
      @Nullable Double lushCavesBonus,
      @Nullable Double dripstoneCavesBonus,
      @Nullable Double netherBonus,
      @Nullable Double endPenalty,
      @Nullable Double deepDarkPenalty,
      @Nullable Double tuffContextBonus
  ) {
  }

  public record SpecialPatch(@Nullable Double rawBlockChance, @Nullable Material rawBlockMaterial) {
  }
}
