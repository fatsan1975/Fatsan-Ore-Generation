package com.fatsan1975.fug.rule;

import com.fatsan1975.fug.config.ConfigIssue;
import com.fatsan1975.fug.config.GlobalSettings;
import com.fatsan1975.fug.config.PluginConfiguration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.World;
import org.bukkit.generator.WorldInfo;

public final class RuleSnapshot {
  private final GlobalSettings globalSettings;
  private final PluginConfiguration.WorldSettings worldSettings;
  private final PluginConfiguration.OresConfiguration oresConfiguration;
  private final List<ConfigIssue> issues;
  private final Instant loadedAt;
  private final ConcurrentHashMap<UUID, WorldRuntimeProfile> worldProfiles = new ConcurrentHashMap<>();

  RuleSnapshot(
      final GlobalSettings globalSettings,
      final PluginConfiguration.WorldSettings worldSettings,
      final PluginConfiguration.OresConfiguration oresConfiguration,
      final List<ConfigIssue> issues,
      final Instant loadedAt
  ) {
    this.globalSettings = globalSettings;
    this.worldSettings = worldSettings;
    this.oresConfiguration = oresConfiguration;
    this.issues = issues;
    this.loadedAt = loadedAt;
  }

  public GlobalSettings globalSettings() {
    return this.globalSettings;
  }

  public List<ConfigIssue> issues() {
    return this.issues;
  }

  public Instant loadedAt() {
    return this.loadedAt;
  }

  public WorldRuntimeProfile profile(final WorldInfo worldInfo) {
    return this.worldProfiles.computeIfAbsent(worldInfo.getUID(), ignored -> this.compileWorldProfile(worldInfo));
  }

  public WorldRuntimeProfile profile(final World world) {
    return this.worldProfiles.computeIfAbsent(world.getUID(), ignored -> this.compileWorldProfile(world));
  }

  private WorldRuntimeProfile compileWorldProfile(final WorldInfo worldInfo) {
    final String normalizedName = worldInfo.getName().toLowerCase(Locale.ROOT);
    final PluginConfiguration.EnvironmentEntry environmentEntry = this.worldSettings.environments()
        .getOrDefault(worldInfo.getEnvironment(), new PluginConfiguration.EnvironmentEntry(this.globalSettings.defaultWorldEnabled(), null));
    final PluginConfiguration.NamedWorldEntry namedWorldEntry = this.worldSettings.namedWorlds().get(normalizedName);
    final boolean enabled = this.computeWorldEnabled(normalizedName, worldInfo.getEnvironment(), environmentEntry, namedWorldEntry);

    final EnumMap<com.fatsan1975.fug.model.OreType, CompiledOreRule> baseRules = new EnumMap<>(com.fatsan1975.fug.model.OreType.class);
    for (final com.fatsan1975.fug.model.OreType oreType : com.fatsan1975.fug.model.OreType.values()) {
      CompiledOreRule.Builder builder = oreType.defaults().toBuilder(oreType);
      builder = builder.applyPatch(this.oresConfiguration.global().defaults());
      final PluginConfiguration.OreRulePatch globalPatch = this.oresConfiguration.global().ores().get(oreType);
      if (globalPatch != null) {
        builder = builder.applyPatch(globalPatch);
      }
      builder = this.applyProfile(builder, environmentEntry.profile());
      final PluginConfiguration.ScopeConfig environmentScope = this.oresConfiguration.environments().get(worldInfo.getEnvironment());
      if (environmentScope != null) {
        builder = builder.applyPatch(environmentScope.defaults());
        final PluginConfiguration.OreRulePatch environmentPatch = environmentScope.ores().get(oreType);
        if (environmentPatch != null) {
          builder = builder.applyPatch(environmentPatch);
        }
      }
      if (namedWorldEntry != null) {
        builder = this.applyProfile(builder, namedWorldEntry.profile());
      }
      final PluginConfiguration.ScopeConfig worldScope = this.oresConfiguration.worlds().get(normalizedName);
      if (worldScope != null) {
        builder = builder.applyPatch(worldScope.defaults());
        final PluginConfiguration.OreRulePatch worldPatch = worldScope.ores().get(oreType);
        if (worldPatch != null) {
          builder = builder.applyPatch(worldPatch);
        }
      }
      final CompiledOreRule built = builder.build();
      baseRules.put(oreType, oreType.supports(worldInfo.getEnvironment()) ? built : built.toBuilder(oreType).applyPatch(
          new PluginConfiguration.OreRulePatch(false, null, null, null, null, null, null, null, null, null, null, null, null,
              null, null, null, null, null, null, null)).build());
    }

    final List<WorldRuntimeProfile.BiomeEntry> biomeEntries = this.oresConfiguration.biomes().stream()
        .map(entry -> new WorldRuntimeProfile.BiomeEntry(BiomeSelector.fromConfig(entry.selector()), entry.scope()))
        .toList();
    return new WorldRuntimeProfile(worldInfo.getName(), enabled, baseRules, biomeEntries);
  }

  private boolean computeWorldEnabled(
      final String normalizedName,
      final World.Environment environment,
      final PluginConfiguration.EnvironmentEntry environmentEntry,
      final PluginConfiguration.NamedWorldEntry namedWorldEntry
  ) {
    if (this.worldSettings.explicitlyDisabledWorlds().contains(normalizedName)) {
      return false;
    }
    if (!this.worldSettings.includePatterns().isEmpty()
        && this.worldSettings.includePatterns().stream().noneMatch(pattern -> pattern.matcher(normalizedName).matches())) {
      return false;
    }
    if (this.worldSettings.excludePatterns().stream().anyMatch(pattern -> pattern.matcher(normalizedName).matches())) {
      return false;
    }
    if (this.worldSettings.explicitlyEnabledWorlds().contains(normalizedName)) {
      return true;
    }
    if (namedWorldEntry != null && namedWorldEntry.enabled() != null) {
      return namedWorldEntry.enabled();
    }
    return environmentEntry.enabled();
  }

  private CompiledOreRule.Builder applyProfile(final CompiledOreRule.Builder builder, final String profileName) {
    if (profileName == null) {
      return builder;
    }
    final PluginConfiguration.ScopeConfig profile = this.oresConfiguration.profiles().get(profileName.toLowerCase(Locale.ROOT));
    if (profile == null) {
      return builder;
    }
    CompiledOreRule.Builder next = builder.applyPatch(profile.defaults());
    final CompiledOreRule preview = next.build();
    final PluginConfiguration.OreRulePatch orePatch = profile.ores().get(preview.oreType());
    if (orePatch != null) {
      next = next.applyPatch(orePatch);
    }
    return next;
  }
}
