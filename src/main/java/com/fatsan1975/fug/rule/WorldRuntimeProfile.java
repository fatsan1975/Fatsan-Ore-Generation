package com.fatsan1975.fug.rule;

import com.fatsan1975.fug.config.PluginConfiguration;
import com.fatsan1975.fug.model.OreType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.NamespacedKey;

public final class WorldRuntimeProfile {
  private final String worldName;
  private final boolean enabled;
  private final EnumMap<OreType, CompiledOreRule> baseRules;
  private final List<BiomeEntry> biomeEntries;
  private final ConcurrentHashMap<String, EnumMap<OreType, CompiledOreRule>> biomeCache = new ConcurrentHashMap<>();
  private final int scanMinY;
  private final int scanMaxY;

  public WorldRuntimeProfile(
      final String worldName,
      final boolean enabled,
      final EnumMap<OreType, CompiledOreRule> baseRules,
      final List<BiomeEntry> biomeEntries
  ) {
    this.worldName = worldName;
    this.enabled = enabled;
    this.baseRules = baseRules;
    this.biomeEntries = biomeEntries;
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for (final CompiledOreRule rule : baseRules.values()) {
      if (!rule.enabled()) {
        continue;
      }
      min = Math.min(min, rule.verticalDistribution().minY());
      max = Math.max(max, rule.verticalDistribution().maxY());
    }
    for (final BiomeEntry biomeEntry : biomeEntries) {
      for (final Map.Entry<OreType, CompiledOreRule> baseEntry : baseRules.entrySet()) {
        CompiledOreRule.Builder builder = baseEntry.getValue().toBuilder(baseEntry.getKey()).applyPatch(biomeEntry.scope().defaults());
        final PluginConfiguration.OreRulePatch orePatch = biomeEntry.scope().ores().get(baseEntry.getKey());
        if (orePatch != null) {
          builder = builder.applyPatch(orePatch);
        }
        final CompiledOreRule resolved = builder.build();
        if (!resolved.enabled()) {
          continue;
        }
        min = Math.min(min, resolved.verticalDistribution().minY());
        max = Math.max(max, resolved.verticalDistribution().maxY());
      }
    }
    this.scanMinY = min == Integer.MAX_VALUE ? 0 : min;
    this.scanMaxY = max == Integer.MIN_VALUE ? -1 : max;
  }

  public boolean enabled() {
    return this.enabled;
  }

  public String worldName() {
    return this.worldName;
  }

  public int scanMinY() {
    return this.scanMinY;
  }

  public int scanMaxY() {
    return this.scanMaxY;
  }

  public EnumMap<OreType, CompiledOreRule> baseRules() {
    return this.baseRules;
  }

  public CompiledOreRule resolve(final NamespacedKey biomeKey, final OreType oreType) {
    return this.biomeCache
        .computeIfAbsent(biomeKey.asString(), ignored -> this.compileForBiome(biomeKey))
        .get(oreType);
  }

  private EnumMap<OreType, CompiledOreRule> compileForBiome(final NamespacedKey biomeKey) {
    final EnumMap<OreType, CompiledOreRule> compiled = new EnumMap<>(OreType.class);
    for (final Map.Entry<OreType, CompiledOreRule> entry : this.baseRules.entrySet()) {
      CompiledOreRule.Builder builder = entry.getValue().toBuilder(entry.getKey());
      for (final BiomeEntry biomeEntry : this.biomeEntries) {
        if (!biomeEntry.selector().matches(biomeKey)) {
          continue;
        }
        builder = builder.applyPatch(biomeEntry.scope().defaults());
        final PluginConfiguration.OreRulePatch orePatch = biomeEntry.scope().ores().get(entry.getKey());
        if (orePatch != null) {
          builder = builder.applyPatch(orePatch);
        }
      }
      compiled.put(entry.getKey(), builder.build());
    }
    return compiled;
  }

  public List<BiomeEntry> biomeEntries() {
    return new ArrayList<>(this.biomeEntries);
  }

  public record BiomeEntry(BiomeSelector selector, PluginConfiguration.ScopeConfig scope) {
  }
}
