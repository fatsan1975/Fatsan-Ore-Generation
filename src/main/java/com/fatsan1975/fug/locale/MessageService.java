package com.fatsan1975.fug.locale;

import com.fatsan1975.fug.FatsanOreGenerationPlugin;
import java.io.File;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MessageService {
  private final MiniMessage miniMessage = MiniMessage.miniMessage();
  private final YamlConfiguration primary;
  private final YamlConfiguration fallback;

  public MessageService(final FatsanOreGenerationPlugin plugin, final String language) {
    this.primary = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "lang/" + language + ".yml"));
    this.fallback = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "lang/en_US.yml"));
  }

  public Component component(final String key, final Map<String, String> placeholders) {
    final TagResolver[] resolvers = placeholders.entrySet().stream()
        .map(entry -> Placeholder.parsed(entry.getKey(), entry.getValue()))
        .toArray(TagResolver[]::new);
    return this.miniMessage.deserialize(this.raw(key), resolvers);
  }

  public Component component(final String key) {
    return this.miniMessage.deserialize(this.raw(key));
  }

  public void send(final CommandSender sender, final String key, final Map<String, String> placeholders) {
    sender.sendMessage(this.component(key, placeholders));
  }

  public void send(final CommandSender sender, final String key) {
    sender.sendMessage(this.component(key));
  }

  private String raw(final String key) {
    return this.primary.getString(key, this.fallback.getString(key, "<red>Missing lang key:</red> <gray>" + key + "</gray>"));
  }
}
