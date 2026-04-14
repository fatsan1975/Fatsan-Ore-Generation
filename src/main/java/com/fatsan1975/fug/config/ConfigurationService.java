package com.fatsan1975.fug.config;

import com.fatsan1975.fug.FatsanOreGenerationPlugin;
import com.fatsan1975.fug.model.AirExposureMode;
import com.fatsan1975.fug.model.BiomeGroup;
import com.fatsan1975.fug.model.CompatibilityMode;
import com.fatsan1975.fug.model.DistributionProfile;
import com.fatsan1975.fug.model.HostFamily;
import com.fatsan1975.fug.model.OreType;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

public final class ConfigurationService {
  private static final List<String> BUNDLED_RESOURCES = List.of(
      "config.yml",
      "ores.yml",
      "worlds.yml",
      "lang/en_US.yml",
      "lang/tr_TR.yml",
      "presets/simple-mode.yml",
      "presets/advanced-mode.yml",
      "presets/hard-survival.yml",
      "presets/realistic-geology.yml",
      "presets/large-but-rare-veins.yml",
      "presets/performance-first.yml"
  );

  private final FatsanOreGenerationPlugin plugin;

  public ConfigurationService(final FatsanOreGenerationPlugin plugin) {
    this.plugin = plugin;
  }

  public void ensureDefaults() {
    for (final String resource : BUNDLED_RESOURCES) {
      final File destination = new File(this.plugin.getDataFolder(), resource);
      if (!destination.exists()) {
        this.plugin.saveResource(resource, false);
      }
    }
  }

  public PluginConfiguration load() {
    final ConfigIssueCollector issues = new ConfigIssueCollector();

    final YamlConfiguration configYml = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "config.yml"));
    final YamlConfiguration oresYml = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "ores.yml"));
    final YamlConfiguration worldsYml = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "worlds.yml"));

    final GlobalSettings globalSettings = this.loadGlobalSettings(configYml, issues);
    final PluginConfiguration.WorldSettings worldSettings = this.loadWorldSettings(worldsYml, globalSettings, issues);
    final PluginConfiguration.OresConfiguration oresConfiguration = this.loadOresConfiguration(oresYml, issues);
    return new PluginConfiguration(globalSettings, worldSettings, oresConfiguration, issues.snapshot());
  }

  private GlobalSettings loadGlobalSettings(final YamlConfiguration config, final ConfigIssueCollector issues) {
    final String language = config.getString("language", "en_US");
    final boolean debug = config.getBoolean("debug", false);
    final boolean metrics = config.getBoolean("metrics.enabled", false);
    final boolean defaultWorldEnabled = config.getBoolean("worlds.default-enabled", true);
    final boolean strictMode = config.getBoolean("strict-mode", false);
    final CompatibilityMode compatibilityMode = this.parseEnum(
        config.getString("compatibility-mode", CompatibilityMode.PRESERVE.name()),
        CompatibilityMode.class,
        "config.yml",
        "compatibility-mode",
        issues,
        CompatibilityMode.PRESERVE
    );
    final boolean registerLoadedWorlds = config.getBoolean("startup.register-loaded-worlds", true);
    final boolean autoRegisterNewWorlds = config.getBoolean("startup.auto-register-new-worlds", true);
    final boolean scanExistingOres = config.getBoolean("performance.scan-existing-ores", true);
    final boolean useBiomeCache = config.getBoolean("performance.use-biome-cache", true);
    final int maxLoggedGenerationErrors = Math.max(1, config.getInt("performance.max-logged-generation-errors", 3));
    return new GlobalSettings(language, debug, metrics, defaultWorldEnabled, strictMode, compatibilityMode, registerLoadedWorlds,
        autoRegisterNewWorlds, scanExistingOres, useBiomeCache, maxLoggedGenerationErrors);
  }

  private PluginConfiguration.WorldSettings loadWorldSettings(
      final YamlConfiguration config,
      final GlobalSettings globalSettings,
      final ConfigIssueCollector issues
  ) {
    final Set<String> enabledWorlds = normalizeStrings(config.getStringList("enabled-worlds"));
    final Set<String> disabledWorlds = normalizeStrings(config.getStringList("disabled-worlds"));
    final List<Pattern> includePatterns = this.parseGlobPatterns(config.getStringList("include-patterns"), "worlds.yml",
        "include-patterns", issues);
    final List<Pattern> excludePatterns = this.parseGlobPatterns(config.getStringList("exclude-patterns"), "worlds.yml",
        "exclude-patterns", issues);

    final EnumMap<World.Environment, PluginConfiguration.EnvironmentEntry> environments = new EnumMap<>(World.Environment.class);
    for (final World.Environment environment : World.Environment.values()) {
      final String basePath = "environments." + environment.name();
      environments.put(environment, new PluginConfiguration.EnvironmentEntry(
          config.getBoolean(basePath + ".enabled", globalSettings.defaultWorldEnabled()),
          normalizeNullable(config.getString(basePath + ".profile"))
      ));
    }

    final Map<String, PluginConfiguration.NamedWorldEntry> namedWorlds = new HashMap<>();
    final ConfigurationSection namedWorldSection = config.getConfigurationSection("named-worlds");
    if (namedWorldSection != null) {
      for (final String worldName : namedWorldSection.getKeys(false)) {
        final String path = "named-worlds." + worldName;
        final boolean hasEnabled = namedWorldSection.isSet(worldName + ".enabled");
        namedWorlds.put(
            worldName.toLowerCase(Locale.ROOT),
            new PluginConfiguration.NamedWorldEntry(
                hasEnabled ? namedWorldSection.getBoolean(worldName + ".enabled") : null,
                normalizeNullable(namedWorldSection.getString(worldName + ".profile"))
            )
        );
      }
    }

    return new PluginConfiguration.WorldSettings(enabledWorlds, disabledWorlds, includePatterns, excludePatterns, environments,
        namedWorlds);
  }

  private PluginConfiguration.OresConfiguration loadOresConfiguration(
      final YamlConfiguration config,
      final ConfigIssueCollector issues
  ) {
    final PluginConfiguration.ScopeConfig global = this.loadScope(config.getConfigurationSection("global"), "ores.yml", "global",
        issues);

    final Map<String, PluginConfiguration.ScopeConfig> profiles = new HashMap<>();
    final ConfigurationSection profilesSection = config.getConfigurationSection("profiles");
    if (profilesSection != null) {
      for (final String key : profilesSection.getKeys(false)) {
        profiles.put(key.toLowerCase(Locale.ROOT), this.loadScope(profilesSection.getConfigurationSection(key), "ores.yml",
            "profiles." + key, issues));
      }
    }

    final EnumMap<World.Environment, PluginConfiguration.ScopeConfig> environments = new EnumMap<>(World.Environment.class);
    final ConfigurationSection environmentSection = config.getConfigurationSection("environments");
    if (environmentSection != null) {
      for (final String key : environmentSection.getKeys(false)) {
        final World.Environment environment = this.parseEnum(key, World.Environment.class, "ores.yml", "environments." + key,
            issues, null);
        if (environment != null) {
          environments.put(environment, this.loadScope(environmentSection.getConfigurationSection(key), "ores.yml",
              "environments." + key, issues));
        }
      }
    }

    final Map<String, PluginConfiguration.ScopeConfig> worlds = new HashMap<>();
    final ConfigurationSection worldsSection = config.getConfigurationSection("worlds");
    if (worldsSection != null) {
      for (final String key : worldsSection.getKeys(false)) {
        worlds.put(key.toLowerCase(Locale.ROOT), this.loadScope(worldsSection.getConfigurationSection(key), "ores.yml",
            "worlds." + key, issues));
      }
    }

    final List<PluginConfiguration.BiomeRuleConfig> biomes = new ArrayList<>();
    final ConfigurationSection biomesSection = config.getConfigurationSection("biomes");
    if (biomesSection != null) {
      for (final String key : biomesSection.getKeys(false)) {
        final PluginConfiguration.BiomeSelectorConfig selector = this.parseBiomeSelector(key, "ores.yml", "biomes." + key, issues);
        if (selector != null) {
          biomes.add(new PluginConfiguration.BiomeRuleConfig(selector, this.loadScope(biomesSection.getConfigurationSection(key),
              "ores.yml", "biomes." + key, issues)));
        }
      }
    }

    return new PluginConfiguration.OresConfiguration(global, profiles, environments, worlds, biomes);
  }

  private PluginConfiguration.ScopeConfig loadScope(
      final @Nullable ConfigurationSection section,
      final String file,
      final String path,
      final ConfigIssueCollector issues
  ) {
    if (section == null) {
      return PluginConfiguration.ScopeConfig.empty();
    }
    final PluginConfiguration.OreRulePatch defaults = this.loadOrePatch(section.getConfigurationSection("default"), file,
        path + ".default", issues);
    final EnumMap<OreType, PluginConfiguration.OreRulePatch> ores = new EnumMap<>(OreType.class);
    final ConfigurationSection oresSection = section.getConfigurationSection("ores");
    if (oresSection != null) {
      for (final String oreKey : oresSection.getKeys(false)) {
        final OreType oreType = OreType.fromKey(oreKey);
        if (oreType == null) {
          issues.error(file, path + ".ores." + oreKey, oreKey, "Unknown ore key.");
          continue;
        }
        ores.put(oreType, this.loadOrePatch(oresSection.getConfigurationSection(oreKey), file, path + ".ores." + oreKey, issues));
      }
    }
    return new PluginConfiguration.ScopeConfig(defaults, ores);
  }

  private PluginConfiguration.OreRulePatch loadOrePatch(
      final @Nullable ConfigurationSection section,
      final String file,
      final String path,
      final ConfigIssueCollector issues
  ) {
    if (section == null) {
      return PluginConfiguration.OreRulePatch.empty();
    }
    return new PluginConfiguration.OreRulePatch(
        section.isSet("enabled") ? section.getBoolean("enabled") : null,
        section.isSet("rewrite-existing") ? section.getBoolean("rewrite-existing") : null,
        section.isSet("generate-new-veins") ? section.getBoolean("generate-new-veins") : null,
        this.doubleValue(section, "frequency-multiplier", file, path, issues),
        this.doubleValue(section, "rarity", file, path, issues),
        this.doubleValue(section, "spawn-chance", file, path, issues),
        this.intValue(section, "attempts", file, path, issues),
        this.doubleValue(section, "attempts-multiplier", file, path, issues),
        this.doubleValue(section, "vein-size.multiplier", file, path, issues),
        this.intValue(section, "vein-size.min", file, path, issues),
        this.intValue(section, "vein-size.max", file, path, issues),
        this.doubleValue(section, "vein-size.irregularity", file, path, issues),
        this.doubleValue(section, "vein-size.density", file, path, issues),
        section.isSet("vein-size.large-but-rare") ? section.getBoolean("vein-size.large-but-rare") : null,
        section.isSet("vein-size.small-but-common") ? section.getBoolean("vein-size.small-but-common") : null,
        this.loadVerticalPatch(section.getConfigurationSection("vertical-distribution"), file, path + ".vertical-distribution", issues),
        this.loadHostRulesPatch(section.getConfigurationSection("host-blocks"), file, path + ".host-blocks", issues),
        this.loadAirExposurePatch(section.getConfigurationSection("air-exposure"), file, path + ".air-exposure", issues),
        this.loadRealismPatch(section.getConfigurationSection("realism"), file, path + ".realism", issues),
        this.loadSpecialPatch(section.getConfigurationSection("special"), file, path + ".special", issues)
    );
  }

  private @Nullable PluginConfiguration.VerticalDistributionPatch loadVerticalPatch(
      final @Nullable ConfigurationSection section,
      final String file,
      final String path,
      final ConfigIssueCollector issues
  ) {
    if (section == null) {
      return null;
    }
    return new PluginConfiguration.VerticalDistributionPatch(
        this.intValue(section, "min-y", file, path, issues),
        this.intValue(section, "max-y", file, path, issues),
        this.intValue(section, "peak-y", file, path, issues),
        this.intValue(section, "secondary-peak-y", file, path, issues),
        section.isSet("profile")
            ? this.parseEnum(section.getString("profile"), DistributionProfile.class, file, path + ".profile", issues, null)
            : null
    );
  }

  private @Nullable PluginConfiguration.HostRulesPatch loadHostRulesPatch(
      final @Nullable ConfigurationSection section,
      final String file,
      final String path,
      final ConfigIssueCollector issues
  ) {
    if (section == null) {
      return null;
    }
    return new PluginConfiguration.HostRulesPatch(
        this.parseHostFamilies(section.getStringList("families"), file, path + ".families", issues),
        this.parseMaterials(section.getStringList("allowed-materials"), file, path + ".allowed-materials", issues),
        this.parseMaterials(section.getStringList("forbidden-materials"), file, path + ".forbidden-materials", issues),
        section.isSet("stone-family-aware") ? section.getBoolean("stone-family-aware") : null
    );
  }

  private @Nullable PluginConfiguration.AirExposurePatch loadAirExposurePatch(
      final @Nullable ConfigurationSection section,
      final String file,
      final String path,
      final ConfigIssueCollector issues
  ) {
    if (section == null) {
      return null;
    }
    return new PluginConfiguration.AirExposurePatch(
        section.isSet("mode") ? this.parseEnum(section.getString("mode"), AirExposureMode.class, file, path + ".mode", issues, null)
            : null,
        this.doubleValue(section, "penalty", file, path, issues),
        this.doubleValue(section, "bonus", file, path, issues),
        this.intValue(section, "max-open-faces", file, path, issues),
        this.intValue(section, "min-open-faces", file, path, issues)
    );
  }

  private @Nullable PluginConfiguration.RealismPatch loadRealismPatch(
      final @Nullable ConfigurationSection section,
      final String file,
      final String path,
      final ConfigIssueCollector issues
  ) {
    if (section == null) {
      return null;
    }
    return new PluginConfiguration.RealismPatch(
        section.isSet("enabled") ? section.getBoolean("enabled") : null,
        section.isSet("require-valid-host") ? section.getBoolean("require-valid-host") : null,
        this.intValue(section, "min-solid-neighbors", file, path, issues),
        this.doubleValue(section, "mountain-bonus", file, path, issues),
        this.doubleValue(section, "badlands-bonus", file, path, issues),
        this.doubleValue(section, "lush-caves-bonus", file, path, issues),
        this.doubleValue(section, "dripstone-caves-bonus", file, path, issues),
        this.doubleValue(section, "nether-bonus", file, path, issues),
        this.doubleValue(section, "end-penalty", file, path, issues),
        this.doubleValue(section, "deep-dark-penalty", file, path, issues),
        this.doubleValue(section, "tuff-context-bonus", file, path, issues)
    );
  }

  private @Nullable PluginConfiguration.SpecialPatch loadSpecialPatch(
      final @Nullable ConfigurationSection section,
      final String file,
      final String path,
      final ConfigIssueCollector issues
  ) {
    if (section == null) {
      return null;
    }
    return new PluginConfiguration.SpecialPatch(
        this.doubleValue(section, "raw-block-chance", file, path, issues),
        section.isSet("raw-block-material")
            ? this.parseMaterial(section.getString("raw-block-material"), file, path + ".raw-block-material", issues)
            : null
    );
  }

  private @Nullable PluginConfiguration.BiomeSelectorConfig parseBiomeSelector(
      final String rawKey,
      final String file,
      final String path,
      final ConfigIssueCollector issues
  ) {
    if (rawKey.equals("*")) {
      return new PluginConfiguration.WildcardBiomeSelectorConfig();
    }
    if (rawKey.toLowerCase(Locale.ROOT).startsWith("namespace:")) {
      return new PluginConfiguration.NamespaceBiomeSelectorConfig(rawKey.substring("namespace:".length()).toLowerCase(Locale.ROOT));
    }
    if (rawKey.toLowerCase(Locale.ROOT).startsWith("group:")) {
      final String name = rawKey.substring("group:".length()).trim().toUpperCase(Locale.ROOT);
      final BiomeGroup group = this.parseEnum(name, BiomeGroup.class, file, path, issues, null);
      return group == null ? null : new PluginConfiguration.GroupBiomeSelectorConfig(group);
    }
    final NamespacedKey key = parseNamespacedKey(rawKey);
    if (key == null) {
      issues.error(file, path, rawKey, "Invalid biome selector.");
      return null;
    }
    if (Registry.BIOME.get(key) == null) {
      issues.warning(file, path, rawKey, "Biome key is not present in the current registry. It will still be kept as a future/custom match.");
    }
    return new PluginConfiguration.ExactBiomeSelectorConfig(key.asString());
  }

  private <T extends Enum<T>> @Nullable T parseEnum(
      final @Nullable String input,
      final Class<T> type,
      final String file,
      final String path,
      final ConfigIssueCollector issues,
      final @Nullable T fallback
  ) {
    if (input == null || input.isBlank()) {
      return fallback;
    }
    try {
      return Enum.valueOf(type, input.trim().toUpperCase(Locale.ROOT));
    } catch (final IllegalArgumentException ignored) {
      issues.error(file, path, input, "Expected one of " + List.of(type.getEnumConstants()));
      return fallback;
    }
  }

  private Set<HostFamily> parseHostFamilies(
      final List<String> values,
      final String file,
      final String path,
      final ConfigIssueCollector issues
  ) {
    final Set<HostFamily> out = new HashSet<>();
    for (final String value : values) {
      final HostFamily family = this.parseEnum(value, HostFamily.class, file, path, issues, null);
      if (family != null) {
        out.add(family);
      }
    }
    return out;
  }

  private Set<Material> parseMaterials(
      final List<String> values,
      final String file,
      final String path,
      final ConfigIssueCollector issues
  ) {
    final Set<Material> out = new HashSet<>();
    for (final String value : values) {
      final Material material = this.parseMaterial(value, file, path, issues);
      if (material != null) {
        out.add(material);
      }
    }
    return out;
  }

  private @Nullable Material parseMaterial(
      final @Nullable String value,
      final String file,
      final String path,
      final ConfigIssueCollector issues
  ) {
    if (value == null || value.isBlank()) {
      return null;
    }
    final Material material = Material.matchMaterial(value.trim().toUpperCase(Locale.ROOT));
    if (material == null) {
      issues.error(file, path, value, "Unknown material.");
    }
    return material;
  }

  private List<Pattern> parseGlobPatterns(
      final List<String> globs,
      final String file,
      final String path,
      final ConfigIssueCollector issues
  ) {
    final List<Pattern> patterns = new ArrayList<>();
    for (final String glob : globs) {
      try {
        patterns.add(Pattern.compile(globToRegex(glob)));
      } catch (final PatternSyntaxException exception) {
        issues.error(file, path, glob, "Invalid pattern: " + exception.getMessage());
      }
    }
    return patterns;
  }

  private @Nullable Double doubleValue(
      final ConfigurationSection section,
      final String key,
      final String file,
      final String basePath,
      final ConfigIssueCollector issues
  ) {
    if (!section.isSet(key)) {
      return null;
    }
    final Object raw = section.get(key);
    if (raw instanceof Number number) {
      return number.doubleValue();
    }
    issues.error(file, basePath + "." + key, String.valueOf(raw), "Expected a number.");
    return null;
  }

  private @Nullable Integer intValue(
      final ConfigurationSection section,
      final String key,
      final String file,
      final String basePath,
      final ConfigIssueCollector issues
  ) {
    if (!section.isSet(key)) {
      return null;
    }
    final Object raw = section.get(key);
    if (raw instanceof Number number) {
      return number.intValue();
    }
    issues.error(file, basePath + "." + key, String.valueOf(raw), "Expected an integer.");
    return null;
  }

  private static Set<String> normalizeStrings(final List<String> source) {
    final Set<String> result = new HashSet<>();
    for (final String value : source) {
      if (value != null && !value.isBlank()) {
        result.add(value.toLowerCase(Locale.ROOT));
      }
    }
    return result;
  }

  private static @Nullable String normalizeNullable(final @Nullable String input) {
    return input == null || input.isBlank() ? null : input.toLowerCase(Locale.ROOT);
  }

  private static @Nullable NamespacedKey parseNamespacedKey(final String input) {
    final String normalized = input.trim().toLowerCase(Locale.ROOT);
    return normalized.contains(":") ? NamespacedKey.fromString(normalized) : NamespacedKey.minecraft(normalized);
  }

  private static String globToRegex(final String glob) {
    final StringBuilder builder = new StringBuilder("^");
    for (final char character : glob.toCharArray()) {
      switch (character) {
        case '*' -> builder.append(".*");
        case '?' -> builder.append('.');
        case '.', '(', ')', '+', '|', '^', '$', '@', '%' -> builder.append('\\').append(character);
        case '\\' -> builder.append("\\\\");
        default -> builder.append(character);
      }
    }
    return builder.append('$').toString();
  }
}
