package com.fatsan1975.fug.generation;

import com.fatsan1975.fug.FatsanOreGenerationPlugin;
import com.fatsan1975.fug.rule.RuleSnapshot;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

@SuppressWarnings("deprecation")
public final class OreGenerationPopulator extends BlockPopulator {
  private final FatsanOreGenerationPlugin plugin;
  private final Supplier<RuleSnapshot> snapshotSupplier;
  private final GenerationStats stats;
  private final OreGenerationEngine engine;
  private final AtomicInteger loggedErrors = new AtomicInteger();

  public OreGenerationPopulator(
      final FatsanOreGenerationPlugin plugin,
      final Supplier<RuleSnapshot> snapshotSupplier,
      final GenerationStats stats,
      final OreGenerationEngine engine
  ) {
    this.plugin = plugin;
    this.snapshotSupplier = snapshotSupplier;
    this.stats = stats;
    this.engine = engine;
  }

  @Override
  public void populate(
      final WorldInfo worldInfo,
      final Random random,
      final int chunkX,
      final int chunkZ,
      final LimitedRegion limitedRegion
  ) {
    try {
      this.engine.populate(this.snapshotSupplier.get(), worldInfo, random, chunkX, chunkZ, new LimitedRegionAdapter(limitedRegion),
          this.stats);
    } catch (final Throwable throwable) {
      this.stats.generationError();
      final int errorCount = this.loggedErrors.incrementAndGet();
      if (errorCount <= this.snapshotSupplier.get().globalSettings().maxLoggedGenerationErrors()) {
        this.plugin.getLogger().severe("FUG generation error in " + worldInfo.getName() + " chunk " + chunkX + "," + chunkZ
            + ": " + throwable.getMessage());
        if (errorCount == this.snapshotSupplier.get().globalSettings().maxLoggedGenerationErrors()) {
          this.plugin.getLogger().severe("Further generation errors will be suppressed to protect console signal quality.");
        }
      }
    }
  }
}
