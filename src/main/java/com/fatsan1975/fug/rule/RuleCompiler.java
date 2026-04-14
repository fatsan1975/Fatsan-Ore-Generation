package com.fatsan1975.fug.rule;

import com.fatsan1975.fug.config.PluginConfiguration;
import java.time.Instant;

public final class RuleCompiler {
  private RuleCompiler() {
  }

  public static RuleSnapshot compile(final PluginConfiguration configuration) {
    return new RuleSnapshot(
        configuration.globalSettings(),
        configuration.worldSettings(),
        configuration.oresConfiguration(),
        configuration.issues(),
        Instant.now()
    );
  }
}
