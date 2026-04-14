package com.fatsan1975.fug.rule;

import com.fatsan1975.fug.config.PluginConfiguration;
import org.bukkit.NamespacedKey;

public interface BiomeSelector {
  boolean matches(NamespacedKey biomeKey);

  String description();

  static BiomeSelector fromConfig(final PluginConfiguration.BiomeSelectorConfig config) {
    return switch (config) {
      case PluginConfiguration.ExactBiomeSelectorConfig exact -> new Exact(exact.key());
      case PluginConfiguration.NamespaceBiomeSelectorConfig namespace -> new Namespace(namespace.namespace());
      case PluginConfiguration.GroupBiomeSelectorConfig group -> new Group(group.group());
      case PluginConfiguration.WildcardBiomeSelectorConfig ignored -> new Wildcard();
    };
  }

  record Exact(String key) implements BiomeSelector {
    @Override
    public boolean matches(final NamespacedKey biomeKey) {
      return biomeKey.asString().equalsIgnoreCase(this.key);
    }

    @Override
    public String description() {
      return this.key;
    }
  }

  record Namespace(String namespace) implements BiomeSelector {
    @Override
    public boolean matches(final NamespacedKey biomeKey) {
      return biomeKey.getNamespace().equalsIgnoreCase(this.namespace);
    }

    @Override
    public String description() {
      return "namespace:" + this.namespace;
    }
  }

  record Group(com.fatsan1975.fug.model.BiomeGroup group) implements BiomeSelector {
    @Override
    public boolean matches(final NamespacedKey biomeKey) {
      return this.group.matches(biomeKey);
    }

    @Override
    public String description() {
      return "group:" + this.group.name().toLowerCase();
    }
  }

  final class Wildcard implements BiomeSelector {
    @Override
    public boolean matches(final NamespacedKey biomeKey) {
      return true;
    }

    @Override
    public String description() {
      return "*";
    }
  }
}
