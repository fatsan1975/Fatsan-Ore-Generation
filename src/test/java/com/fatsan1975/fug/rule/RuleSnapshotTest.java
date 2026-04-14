package com.fatsan1975.fug.rule;

import com.fatsan1975.fug.TestWorldInfo;
import com.fatsan1975.fug.config.GlobalSettings;
import com.fatsan1975.fug.config.PluginConfiguration;
import com.fatsan1975.fug.model.CompatibilityMode;
import com.fatsan1975.fug.model.OreType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RuleSnapshotTest {
  @Test
  void biomeOverridesWinLast() {
    final PluginConfiguration.ScopeConfig global = scope(Map.of(
        OreType.DIAMOND, patch(null, null, 1.0)
    ));
    final PluginConfiguration.ScopeConfig environment = scope(Map.of(
        OreType.DIAMOND, patch(null, null, 0.8)
    ));
    final PluginConfiguration.ScopeConfig world = scope(Map.of(
        OreType.DIAMOND, patch(null, null, 0.6)
    ));
    final PluginConfiguration.ScopeConfig biome = scope(Map.of(
        OreType.DIAMOND, patch(false, null, 0.4)
    ));

    final PluginConfiguration configuration = new PluginConfiguration(
        new GlobalSettings("en_US", false, false, true, false, CompatibilityMode.PRESERVE, true, true, true, true, 3),
        new PluginConfiguration.WorldSettings(Set.of(), Set.of(), List.of(), List.of(), environmentEntries(),
            Map.of("mining_world", new PluginConfiguration.NamedWorldEntry(true, null))),
        new PluginConfiguration.OresConfiguration(
            global,
            Map.of(),
            new EnumMap<>(Map.of(World.Environment.NORMAL, environment)),
            Map.of("mining_world", world),
            List.of(new PluginConfiguration.BiomeRuleConfig(
                new PluginConfiguration.ExactBiomeSelectorConfig("minecraft:badlands"), biome))
        ),
        List.of()
    );

    final RuleSnapshot snapshot = RuleCompiler.compile(configuration);
    final WorldRuntimeProfile profile = snapshot.profile(new TestWorldInfo("mining_world", java.util.UUID.randomUUID(),
        World.Environment.NORMAL, 42L, -64, 320));

    final CompiledOreRule rule = profile.resolve(NamespacedKey.minecraft("badlands"), OreType.DIAMOND);
    Assertions.assertFalse(rule.enabled());
    Assertions.assertEquals(0.4, rule.frequencyMultiplier(), 0.0001);
  }

  private static PluginConfiguration.ScopeConfig scope(final Map<OreType, PluginConfiguration.OreRulePatch> ores) {
    final EnumMap<OreType, PluginConfiguration.OreRulePatch> map = new EnumMap<>(OreType.class);
    map.putAll(ores);
    return new PluginConfiguration.ScopeConfig(PluginConfiguration.OreRulePatch.empty(), map);
  }

  private static PluginConfiguration.OreRulePatch patch(final Boolean enabled, final Integer attempts, final Double frequency) {
    return new PluginConfiguration.OreRulePatch(enabled, null, null, frequency, null, null, attempts, null, null, null, null,
        null, null, null, null, null, null, null, null, null);
  }

  private static EnumMap<World.Environment, PluginConfiguration.EnvironmentEntry> environmentEntries() {
    final EnumMap<World.Environment, PluginConfiguration.EnvironmentEntry> environments = new EnumMap<>(World.Environment.class);
    environments.put(World.Environment.NORMAL, new PluginConfiguration.EnvironmentEntry(true, null));
    environments.put(World.Environment.NETHER, new PluginConfiguration.EnvironmentEntry(true, null));
    environments.put(World.Environment.THE_END, new PluginConfiguration.EnvironmentEntry(true, null));
    return environments;
  }
}
