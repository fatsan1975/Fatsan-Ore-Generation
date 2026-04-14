package com.fatsan1975.fug.generation;

import com.fatsan1975.fug.model.HostFamily;
import com.fatsan1975.fug.model.OreType;
import com.fatsan1975.fug.rule.CompiledOreRule;
import com.fatsan1975.fug.rule.RuleSnapshot;
import com.fatsan1975.fug.rule.WorldRuntimeProfile;
import java.util.Map;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.generator.WorldInfo;

public final class OreGenerationEngine {
  private static final int[][] NEIGHBOR_OFFSETS = new int[][] {
      {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
  };

  public boolean populate(
      final RuleSnapshot snapshot,
      final WorldInfo worldInfo,
      final Random random,
      final int chunkX,
      final int chunkZ,
      final GenerationRegion region,
      final GenerationStats stats
  ) {
    final WorldRuntimeProfile worldProfile = snapshot.profile(worldInfo);
    if (!worldProfile.enabled() || worldProfile.scanMaxY() < worldProfile.scanMinY()) {
      stats.skippedChunk();
      return false;
    }

    final int worldMin = worldInfo.getMinHeight();
    final int worldMax = worldInfo.getMaxHeight();
    final int startX = chunkX << 4;
    final int startZ = chunkZ << 4;
    final int minScanY = Math.max(worldMin, worldProfile.scanMinY());
    final int maxScanY = Math.min(worldMax - 1, worldProfile.scanMaxY());

    final GenerationStats.ChunkReport report = new GenerationStats.ChunkReport();
    if (snapshot.globalSettings().scanExistingOres()) {
      this.rewriteExisting(chunkX, chunkZ, region, worldProfile, random, report, startX, startZ, minScanY, maxScanY);
    }
    this.generateAdditionalVeins(worldInfo, region, worldProfile, random, report, startX, startZ, worldMin, worldMax);
    stats.record(report);
    return true;
  }

  private void rewriteExisting(
      final int chunkX,
      final int chunkZ,
      final GenerationRegion region,
      final WorldRuntimeProfile worldProfile,
      final Random random,
      final GenerationStats.ChunkReport report,
      final int startX,
      final int startZ,
      final int minScanY,
      final int maxScanY
  ) {
    for (int x = startX; x < startX + 16; x++) {
      for (int z = startZ; z < startZ + 16; z++) {
        for (int y = minScanY; y <= maxScanY; y++) {
          final Material current = region.getType(x, y, z);
          final OreType oreType = OreType.fromExistingMaterial(current);
          if (oreType == null) {
            continue;
          }
          final NamespacedKey biomeKey = region.getBiomeKey(x, y, z);
          final CompiledOreRule rule = worldProfile.resolve(biomeKey, oreType);
          if (!rule.rewriteExisting()) {
            continue;
          }
          final int openFaces = this.countOpenFaces(region, x, y, z);
          final double keepChance = rule.existingRetentionChance()
              * rule.biomeRealismMultiplier(biomeKey)
              * rule.airExposureMultiplier(openFaces);
          if (!rule.enabled() || random.nextDouble() > keepChance) {
            region.setType(x, y, z, oreType.replacementMaterialForExisting(current));
            report.removed(oreType);
          }
        }
      }
    }
  }

  private void generateAdditionalVeins(
      final WorldInfo worldInfo,
      final GenerationRegion region,
      final WorldRuntimeProfile worldProfile,
      final Random random,
      final GenerationStats.ChunkReport report,
      final int startX,
      final int startZ,
      final int worldMin,
      final int worldMax
  ) {
    for (final Map.Entry<OreType, CompiledOreRule> entry : worldProfile.baseRules().entrySet()) {
      final OreType oreType = entry.getKey();
      final CompiledOreRule baseRule = entry.getValue();
      if (!baseRule.enabled() || !baseRule.generateNewVeins()) {
        continue;
      }
      final int attempts = baseRule.resolveAttempts(random);
      for (int attempt = 0; attempt < attempts; attempt++) {
        report.attemptedVein();
        final int centerX = startX + random.nextInt(16);
        final int centerZ = startZ + random.nextInt(16);
        final int centerY = baseRule.verticalDistribution().pickY(random, worldMin, worldMax);
        final NamespacedKey biomeKey = region.getBiomeKey(centerX, centerY, centerZ);
        final CompiledOreRule rule = worldProfile.resolve(biomeKey, oreType);
        if (!rule.enabled() || random.nextDouble() > rule.spawnChance()) {
          continue;
        }
        final int veinSize = rule.resolveVeinSize(random);
        if (this.tryGenerateVein(worldInfo.getEnvironment(), region, worldProfile, random, centerX, centerY, centerZ, oreType, rule,
            veinSize, worldMin, worldMax, report)) {
          report.successfulVein();
        }
      }
    }
  }

  private boolean tryGenerateVein(
      final World.Environment environment,
      final GenerationRegion region,
      final WorldRuntimeProfile worldProfile,
      final Random random,
      final int centerX,
      final int centerY,
      final int centerZ,
      final OreType oreType,
      final CompiledOreRule rule,
      final int size,
      final int worldMin,
      final int worldMax,
      final GenerationStats.ChunkReport report
  ) {
    final int segments = Math.max(2, Math.min(6, 2 + (size / 4)));
    final double angle = random.nextDouble() * Math.PI;
    final double xSpread = Math.sin(angle) * size / 5.5D;
    final double zSpread = Math.cos(angle) * size / 5.5D;
    final double[] cx = new double[segments];
    final double[] cy = new double[segments];
    final double[] cz = new double[segments];
    final double[] radius = new double[segments];

    int minX = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxY = Integer.MIN_VALUE;
    int minZ = Integer.MAX_VALUE;
    int maxZ = Integer.MIN_VALUE;

    for (int index = 0; index < segments; index++) {
      final double t = segments == 1 ? 0.0D : index / (double) (segments - 1);
      cx[index] = centerX + 0.5D + lerp(-xSpread, xSpread, t) + random.nextGaussian() * 0.6D * rule.irregularity();
      cy[index] = centerY + random.nextGaussian() * 1.4D * Math.max(0.25D, rule.irregularity());
      cz[index] = centerZ + 0.5D + lerp(-zSpread, zSpread, t) + random.nextGaussian() * 0.6D * rule.irregularity();
      radius[index] = Math.max(1.15D, (Math.sin(Math.PI * t) + 1.0D) * 0.5D * size / 4.25D + random.nextDouble());

      minX = Math.min(minX, (int) Math.floor(cx[index] - radius[index]));
      maxX = Math.max(maxX, (int) Math.ceil(cx[index] + radius[index]));
      minY = Math.min(minY, (int) Math.floor(cy[index] - radius[index]));
      maxY = Math.max(maxY, (int) Math.ceil(cy[index] + radius[index]));
      minZ = Math.min(minZ, (int) Math.floor(cz[index] - radius[index]));
      maxZ = Math.max(maxZ, (int) Math.ceil(cz[index] + radius[index]));
    }

    boolean placedAny = false;
    final long noiseSeed = (((long) centerX) << 32) ^ (((long) centerZ) << 16) ^ centerY ^ oreType.ordinal();
    for (int x = minX; x <= maxX; x++) {
      for (int y = minY; y <= maxY; y++) {
        for (int z = minZ; z <= maxZ; z++) {
          if (!region.isInRegion(x, y, z)) {
            continue;
          }
          final NamespacedKey biomeKey = region.getBiomeKey(x, y, z);
          final CompiledOreRule blockRule = worldProfile.resolve(biomeKey, oreType);
          if (!blockRule.enabled()) {
            continue;
          }
          final int allowedMinY = blockRule.verticalDistribution().clampMin(worldMin);
          final int allowedMaxY = blockRule.verticalDistribution().clampMax(worldMax);
          if (y < allowedMinY || y > allowedMaxY) {
            continue;
          }
          final Material host = region.getType(x, y, z);
          if (!blockRule.hostAllowed(host)) {
            continue;
          }
          final HostFamily family = HostFamily.fromMaterial(host);
          if (blockRule.realism().requireValidHost() && family == null) {
            continue;
          }
          final int openFaces = this.countOpenFaces(region, x, y, z);
          final int solidNeighbors = 6 - openFaces;
          if (solidNeighbors < blockRule.realism().minSolidNeighbors()) {
            continue;
          }
          double contribution = 0.0D;
          for (int index = 0; index < segments; index++) {
            final double dx = (x + 0.5D - cx[index]) / radius[index];
            final double dy = (y + 0.5D - cy[index]) / radius[index];
            final double dz = (z + 0.5D - cz[index]) / radius[index];
            final double value = 1.0D - (dx * dx + dy * dy + dz * dz);
            if (value > contribution) {
              contribution = value;
            }
          }
          if (contribution <= 0.0D) {
            continue;
          }
          final double tuffBonus = this.countAdjacentMaterial(region, x, y, z, Material.TUFF) > 0
              ? blockRule.realism().tuffContextBonus()
              : 0.0D;
          double score = contribution * blockRule.density()
              * blockRule.biomeRealismMultiplier(biomeKey)
              * blockRule.airExposureMultiplier(openFaces)
              * (1.0D + tuffBonus);
          final double noise = hashedUnit(noiseSeed, x, y, z);
          score += (noise - 0.5D) * blockRule.irregularity() * 0.65D;
          if (score < 0.34D) {
            continue;
          }
          final Material target = blockRule.resolvePlacedMaterial(host, hashedUnit(noiseSeed ^ 0x9E3779B97F4A7C15L, x, y, z));
          if (target == host) {
            continue;
          }
          if (environment == World.Environment.THE_END && oreType != OreType.ANCIENT_DEBRIS && family == HostFamily.END_STONE
              && !BiomeKeyAllowsEndPlacement(biomeKey)) {
            continue;
          }
          region.setType(x, y, z, target);
          report.placed(oreType);
          placedAny = true;
        }
      }
    }
    return placedAny;
  }

  private int countOpenFaces(final GenerationRegion region, final int x, final int y, final int z) {
    int open = 0;
    for (final int[] offset : NEIGHBOR_OFFSETS) {
      final int nx = x + offset[0];
      final int ny = y + offset[1];
      final int nz = z + offset[2];
      if (!region.isInRegion(nx, ny, nz)) {
        continue;
      }
      final Material neighbor = region.getType(nx, ny, nz);
      if (isOpenMaterial(neighbor)) {
        open++;
      }
    }
    return open;
  }

  private int countAdjacentMaterial(final GenerationRegion region, final int x, final int y, final int z, final Material target) {
    int matches = 0;
    for (final int[] offset : NEIGHBOR_OFFSETS) {
      final int nx = x + offset[0];
      final int ny = y + offset[1];
      final int nz = z + offset[2];
      if (region.isInRegion(nx, ny, nz) && region.getType(nx, ny, nz) == target) {
        matches++;
      }
    }
    return matches;
  }

  private static double lerp(final double start, final double end, final double delta) {
    return start + (end - start) * delta;
  }

  private static double hashedUnit(final long seed, final int x, final int y, final int z) {
    long value = seed;
    value ^= x * 341873128712L;
    value ^= y * 132897987541L;
    value ^= z * 42317861L;
    value ^= (value >>> 33);
    value *= 0xff51afd7ed558ccdL;
    value ^= (value >>> 33);
    value *= 0xc4ceb9fe1a85ec53L;
    value ^= (value >>> 33);
    return ((value >>> 11) & ((1L << 53) - 1)) / (double) (1L << 53);
  }

  private static boolean BiomeKeyAllowsEndPlacement(final NamespacedKey biomeKey) {
    final String key = biomeKey.getKey();
    return key.contains("end_highlands") || key.contains("end_midlands");
  }

  private static boolean isOpenMaterial(final Material material) {
    return material == Material.AIR
        || material == Material.CAVE_AIR
        || material == Material.VOID_AIR
        || material == Material.WATER
        || material == Material.LAVA;
  }
}
