package com.fatsan1975.fug.config;

import com.fatsan1975.fug.model.CompatibilityMode;

public record GlobalSettings(
    String language,
    boolean debug,
    boolean metricsEnabled,
    boolean defaultWorldEnabled,
    boolean strictMode,
    CompatibilityMode compatibilityMode,
    boolean registerLoadedWorldsOnStartup,
    boolean autoRegisterNewWorlds,
    boolean scanExistingOres,
    boolean useBiomeCache,
    int maxLoggedGenerationErrors
) {
}
