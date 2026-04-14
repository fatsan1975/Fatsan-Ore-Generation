package com.fatsan1975.fug.generation;

import com.fatsan1975.fug.TestWorldInfo;
import com.fatsan1975.fug.config.GlobalSettings;
import com.fatsan1975.fug.config.PluginConfiguration;
import com.fatsan1975.fug.model.CompatibilityMode;
import com.fatsan1975.fug.model.OreType;
import com.fatsan1975.fug.rule.RuleCompiler;
import com.fatsan1975.fug.rule.RuleSnapshot;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OreGenerationEngineTest {
  @Test
  void disabledOreIsRemovedFromNewChunk() {
    final RuleSnapshot snapshot = snapshotWithDiamondPatch(
        new PluginConfiguration.OreRulePatch(false, true, false, 0.0, 1.0, 1.0, 0, 1.0, 1.0, 1, 1, 0.0, 1.0, false, false,
            null, null, null, null, null)
    );
    final TestGenerationRegion region = new TestGenerationRegion(0, -16, 0, 31, 63, 31, Material.STONE,
        NamespacedKey.minecraft("plains"));
    region.setType(4, 10, 4, Material.DIAMOND_ORE);

    final OreGenerationEngine engine = new OreGenerationEngine();
    final GenerationStats stats = new GenerationStats();
    engine.populate(snapshot, new TestWorldInfo("world", java.util.UUID.randomUUID(), World.Environment.NORMAL, 1234L, -16, 64),
        new Random(42L), 0, 0, region, stats);

    Assertions.assertEquals(Material.STONE, region.getType(4, 10, 4));
  }

  @Test
  void generatedDiamondStaysInsideConfiguredYBand() {
    final PluginConfiguration.OreRulePatch patch = new PluginConfiguration.OreRulePatch(true, false, true, 1.0, 1.0, 1.0, 4, 1.0,
        1.0, 3, 5, 0.2, 0.9, false, false,
        new PluginConfiguration.VerticalDistributionPatch(2, 6, 4, 4, com.fatsan1975.fug.model.DistributionProfile.UNIFORM),
        new PluginConfiguration.HostRulesPatch(Set.of(com.fatsan1975.fug.model.HostFamily.STONE), Set.of(), Set.of(), true),
        null,
        new PluginConfiguration.RealismPatch(true, true, 4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
        null
    );
    final RuleSnapshot snapshot = snapshotWithDiamondPatch(patch);
    final TestGenerationRegion region = new TestGenerationRegion(0, 0, 0, 31, 31, 31, Material.STONE, NamespacedKey.minecraft("plains"));

    final OreGenerationEngine engine = new OreGenerationEngine();
    final GenerationStats stats = new GenerationStats();
    engine.populate(snapshot, new TestWorldInfo("world", java.util.UUID.randomUUID(), World.Environment.NORMAL, 9L, 0, 32),
        new Random(1L), 0, 0, region, stats);

    boolean foundDiamond = false;
    for (int x = 0; x <= 31; x++) {
      for (int y = 0; y <= 31; y++) {
        for (int z = 0; z <= 31; z++) {
          if (region.getType(x, y, z) == Material.DIAMOND_ORE || region.getType(x, y, z) == Material.DEEPSLATE_DIAMOND_ORE) {
            foundDiamond = true;
            Assertions.assertTrue(y >= 2 && y <= 6, "Generated diamond escaped configured Y band: " + y);
          }
        }
      }
    }
    Assertions.assertTrue(foundDiamond, "Expected at least one generated diamond ore block.");
  }

  private static RuleSnapshot snapshotWithDiamondPatch(final PluginConfiguration.OreRulePatch diamondPatch) {
    final EnumMap<OreType, PluginConfiguration.OreRulePatch> ores = new EnumMap<>(OreType.class);
    ores.put(OreType.DIAMOND, diamondPatch);
    final PluginConfiguration configuration = new PluginConfiguration(
        new GlobalSettings("en_US", false, false, true, false, CompatibilityMode.PRESERVE, true, true, true, true, 3),
        new PluginConfiguration.WorldSettings(Set.of(), Set.of(), List.of(), List.of(), environmentEntries(), Map.of()),
        new PluginConfiguration.OresConfiguration(
            new PluginConfiguration.ScopeConfig(PluginConfiguration.OreRulePatch.empty(), ores),
            Map.of(),
            new EnumMap<>(World.Environment.class),
            Map.of(),
            List.of()
        ),
        List.of()
    );
    return RuleCompiler.compile(configuration);
  }

  private static EnumMap<World.Environment, PluginConfiguration.EnvironmentEntry> environmentEntries() {
    final EnumMap<World.Environment, PluginConfiguration.EnvironmentEntry> environments = new EnumMap<>(World.Environment.class);
    environments.put(World.Environment.NORMAL, new PluginConfiguration.EnvironmentEntry(true, null));
    environments.put(World.Environment.NETHER, new PluginConfiguration.EnvironmentEntry(true, null));
    environments.put(World.Environment.THE_END, new PluginConfiguration.EnvironmentEntry(true, null));
    return environments;
  }
}
