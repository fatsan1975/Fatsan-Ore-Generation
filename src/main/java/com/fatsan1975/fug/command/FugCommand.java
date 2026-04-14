package com.fatsan1975.fug.command;

import com.fatsan1975.fug.FatsanOreGenerationPlugin;
import com.fatsan1975.fug.model.OreType;
import com.fatsan1975.fug.rule.CompiledOreRule;
import com.fatsan1975.fug.rule.RuleSnapshot;
import com.fatsan1975.fug.rule.WorldRuntimeProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class FugCommand implements TabExecutor {
  private final FatsanOreGenerationPlugin plugin;

  public FugCommand(final FatsanOreGenerationPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(
      final @NotNull CommandSender sender,
      final @NotNull Command command,
      final @NotNull String label,
      final @NotNull String[] args
  ) {
    if (args.length == 0) {
      this.plugin.messages().send(sender, "command.help");
      return true;
    }
    return switch (args[0].toLowerCase(Locale.ROOT)) {
      case "reload" -> this.reload(sender);
      case "info" -> this.info(sender);
      case "debug" -> this.debug(sender);
      case "inspect" -> this.inspect(sender, args);
      case "stats" -> this.stats(sender);
      case "dump-rule" -> this.dumpRule(sender, args);
      default -> {
        this.plugin.messages().send(sender, "command.help");
        yield true;
      }
    };
  }

  @Override
  public List<String> onTabComplete(
      final @NotNull CommandSender sender,
      final @NotNull Command command,
      final @NotNull String alias,
      final @NotNull String[] args
  ) {
    if (args.length == 1) {
      return List.of("reload", "info", "debug", "inspect", "stats", "dump-rule");
    }
    if (args.length == 2 && args[0].equalsIgnoreCase("inspect")) {
      return oreKeys();
    }
    if (args.length == 2 && args[0].equalsIgnoreCase("dump-rule")) {
      return Bukkit.getWorlds().stream().map(World::getName).toList();
    }
    if (args.length == 3 && args[0].equalsIgnoreCase("dump-rule")) {
      return Registry.BIOME.stream().map(biome -> biome.getKey().asString()).limit(30).toList();
    }
    if (args.length == 4 && args[0].equalsIgnoreCase("dump-rule")) {
      return oreKeys();
    }
    return List.of();
  }

  private boolean reload(final CommandSender sender) {
    if (!this.checkPermission(sender, "fug.reload")) {
      return true;
    }
    final boolean success = this.plugin.reloadPluginState();
    this.plugin.messages().send(sender, success ? "command.reload.success" : "command.reload.failure");
    return true;
  }

  private boolean info(final CommandSender sender) {
    if (!this.checkPermission(sender, "fug.info")) {
      return true;
    }
    final RuleSnapshot snapshot = this.plugin.snapshot();
    sender.sendMessage(this.plugin.messages().component("command.info.header"));
    sender.sendMessage(this.plugin.messages().component("command.info.line",
        Map.of("version", this.plugin.getPluginMeta().getVersion(), "language", snapshot.globalSettings().language(),
            "compatibility", snapshot.globalSettings().compatibilityMode().name(), "worlds",
            Integer.toString(this.plugin.worldRegistrationService().registeredWorldCount()))));
    sender.sendMessage(this.plugin.messages().component("command.info.issues",
        Map.of("issues", Integer.toString(snapshot.issues().size()), "loaded_at", snapshot.loadedAt().toString())));
    return true;
  }

  private boolean debug(final CommandSender sender) {
    if (!this.checkPermission(sender, "fug.debug")) {
      return true;
    }
    this.plugin.messages().send(sender, "command.debug.info",
        Map.of("debug", Boolean.toString(this.plugin.snapshot().globalSettings().debug())));
    return true;
  }

  private boolean inspect(final CommandSender sender, final String[] args) {
    if (!this.checkPermission(sender, "fug.inspect")) {
      return true;
    }
    if (!(sender instanceof Player player)) {
      this.plugin.messages().send(sender, "command.inspect.player-only");
      return true;
    }
    final RuleSnapshot snapshot = this.plugin.snapshot();
    final WorldRuntimeProfile profile = snapshot.profile(player.getWorld());
    final int blockX = player.getLocation().getBlockX();
    final int blockY = player.getLocation().getBlockY();
    final int blockZ = player.getLocation().getBlockZ();
    final NamespacedKey biomeKey = player.getWorld().getBiome(blockX, blockY, blockZ).getKey();
    final OreType requested = args.length >= 2 ? OreType.fromKey(args[1]) : null;

    sender.sendMessage(this.plugin.messages().component("command.inspect.header",
        Map.of("world", player.getWorld().getName(), "biome", biomeKey.asString(), "enabled", Boolean.toString(profile.enabled()))));
    if (requested != null) {
      sender.sendMessage(this.ruleLine(requested, profile.resolve(biomeKey, requested)));
      return true;
    }
    for (final OreType oreType : OreType.values()) {
      final CompiledOreRule rule = profile.resolve(biomeKey, oreType);
      if (rule.enabled()) {
        sender.sendMessage(this.ruleLine(oreType, rule));
      }
    }
    return true;
  }

  private boolean stats(final CommandSender sender) {
    if (!this.checkPermission(sender, "fug.stats")) {
      return true;
    }
    sender.sendMessage(this.plugin.messages().component("command.stats.header"));
    sender.sendMessage(this.plugin.messages().component("command.stats.line",
        Map.of("processed", Long.toString(this.plugin.stats().chunksProcessed()), "skipped",
            Long.toString(this.plugin.stats().chunksSkipped()), "errors",
            Long.toString(this.plugin.stats().generationErrors()), "veins_attempted",
            Long.toString(this.plugin.stats().veinsAttempted()), "veins_placed",
            Long.toString(this.plugin.stats().veinsPlaced()))));
    return true;
  }

  private boolean dumpRule(final CommandSender sender, final String[] args) {
    if (!this.checkPermission(sender, "fug.dump-rule")) {
      return true;
    }
    if (args.length < 4) {
      this.plugin.messages().send(sender, "command.dump-rule.usage");
      return true;
    }
    final World world = Bukkit.getWorld(args[1]);
    if (world == null) {
      this.plugin.messages().send(sender, "command.dump-rule.world-not-found", Map.of("world", args[1]));
      return true;
    }
    final NamespacedKey biomeKey = args[2].contains(":") ? NamespacedKey.fromString(args[2]) : NamespacedKey.minecraft(args[2]);
    final OreType oreType = OreType.fromKey(args[3]);
    if (biomeKey == null || oreType == null) {
      this.plugin.messages().send(sender, "command.dump-rule.invalid-input");
      return true;
    }
    final CompiledOreRule rule = this.plugin.snapshot().profile(world).resolve(biomeKey, oreType);
    sender.sendMessage(this.ruleLine(oreType, rule));
    return true;
  }

  private net.kyori.adventure.text.Component ruleLine(final OreType oreType, final CompiledOreRule rule) {
    return this.plugin.messages().component("command.rule.line",
        Map.of("ore", oreType.key(), "enabled", Boolean.toString(rule.enabled()), "attempts", Integer.toString(rule.attempts()),
            "size", rule.minVeinSize() + "-" + rule.maxVeinSize(), "range",
            rule.verticalDistribution().minY() + ".." + rule.verticalDistribution().maxY(), "chance",
            String.format(Locale.ROOT, "%.2f", rule.spawnChance())));
  }

  private static List<String> oreKeys() {
    final List<String> result = new ArrayList<>();
    for (final OreType oreType : OreType.values()) {
      result.add(oreType.key());
    }
    return result;
  }

  private boolean checkPermission(final CommandSender sender, final String permission) {
    if (sender.hasPermission(permission)) {
      return true;
    }
    this.plugin.messages().send(sender, "command.no-permission");
    return false;
  }
}
