package com.fatsan1975.fug.generation;

import com.fatsan1975.fug.model.OreType;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

public final class GenerationStats {
  private final LongAdder chunksProcessed = new LongAdder();
  private final LongAdder chunksSkipped = new LongAdder();
  private final LongAdder generationErrors = new LongAdder();
  private final LongAdder veinsAttempted = new LongAdder();
  private final LongAdder veinsPlaced = new LongAdder();
  private final EnumMap<OreType, LongAdder> blocksPlaced = new EnumMap<>(OreType.class);
  private final EnumMap<OreType, LongAdder> blocksRemoved = new EnumMap<>(OreType.class);

  public GenerationStats() {
    for (final OreType oreType : OreType.values()) {
      this.blocksPlaced.put(oreType, new LongAdder());
      this.blocksRemoved.put(oreType, new LongAdder());
    }
  }

  public void skippedChunk() {
    this.chunksSkipped.increment();
  }

  public void generationError() {
    this.generationErrors.increment();
  }

  public void record(final ChunkReport report) {
    this.chunksProcessed.increment();
    this.veinsAttempted.add(report.veinsAttempted);
    this.veinsPlaced.add(report.veinsPlaced);
    for (final OreType oreType : OreType.values()) {
      this.blocksPlaced.get(oreType).add(report.placed[oreType.ordinal()]);
      this.blocksRemoved.get(oreType).add(report.removed[oreType.ordinal()]);
    }
  }

  public long chunksProcessed() {
    return this.chunksProcessed.sum();
  }

  public long chunksSkipped() {
    return this.chunksSkipped.sum();
  }

  public long generationErrors() {
    return this.generationErrors.sum();
  }

  public long veinsAttempted() {
    return this.veinsAttempted.sum();
  }

  public long veinsPlaced() {
    return this.veinsPlaced.sum();
  }

  public Map<OreType, Long> placedSnapshot() {
    return snapshot(this.blocksPlaced);
  }

  public Map<OreType, Long> removedSnapshot() {
    return snapshot(this.blocksRemoved);
  }

  private static Map<OreType, Long> snapshot(final EnumMap<OreType, LongAdder> source) {
    final EnumMap<OreType, Long> out = new EnumMap<>(OreType.class);
    for (final Map.Entry<OreType, LongAdder> entry : source.entrySet()) {
      out.put(entry.getKey(), entry.getValue().sum());
    }
    return out;
  }

  public static final class ChunkReport {
    private final int[] placed = new int[OreType.values().length];
    private final int[] removed = new int[OreType.values().length];
    private int veinsAttempted;
    private int veinsPlaced;

    public void placed(final OreType oreType) {
      this.placed[oreType.ordinal()]++;
    }

    public void removed(final OreType oreType) {
      this.removed[oreType.ordinal()]++;
    }

    public void attemptedVein() {
      this.veinsAttempted++;
    }

    public void successfulVein() {
      this.veinsPlaced++;
    }
  }
}
