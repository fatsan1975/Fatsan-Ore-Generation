package com.fatsan1975.fug;

import com.fatsan1975.fug.command.FugCommand;
import com.fatsan1975.fug.config.ConfigIssue;
import com.fatsan1975.fug.config.ConfigurationService;
import com.fatsan1975.fug.config.PluginConfiguration;
import com.fatsan1975.fug.generation.GenerationStats;
import com.fatsan1975.fug.generation.OreGenerationEngine;
import com.fatsan1975.fug.generation.OreGenerationPopulator;
import com.fatsan1975.fug.locale.MessageService;
import com.fatsan1975.fug.rule.RuleCompiler;
import com.fatsan1975.fug.rule.RuleSnapshot;
import com.fatsan1975.fug.service.WorldRegistrationService;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class FatsanOreGenerationPlugin extends JavaPlugin {
  private ConfigurationService configurationService;
  private final AtomicReference<RuleSnapshot> snapshot = new AtomicReference<>();
  private MessageService messages;
  private GenerationStats stats;
  private WorldRegistrationService worldRegistrationService;

  @Override
  public void onEnable() {
    this.configurationService = new ConfigurationService(this);
    this.configurationService.ensureDefaults();
    this.stats = new GenerationStats();

    if (!this.reloadPluginState()) {
      this.getServer().getPluginManager().disablePlugin(this);
      return;
    }

    final OreGenerationPopulator populator = new OreGenerationPopulator(this, this.snapshot::get, this.stats, new OreGenerationEngine());
    this.worldRegistrationService = new WorldRegistrationService(this, populator);
    if (this.snapshot.get().globalSettings().autoRegisterNewWorlds()) {
      this.getServer().getPluginManager().registerEvents(this.worldRegistrationService, this);
    }
    if (this.snapshot.get().globalSettings().registerLoadedWorldsOnStartup()) {
      this.worldRegistrationService.registerLoadedWorlds();
    }

    final PluginCommand fug = this.getCommand("fug");
    if (fug != null) {
      final FugCommand command = new FugCommand(this);
      fug.setExecutor(command);
      fug.setTabCompleter(command);
    }
    this.getLogger().info("Fatsan Ore Generation enabled with " + this.snapshot.get().issues().size() + " config issue(s).");
  }

  public boolean reloadPluginState() {
    final PluginConfiguration configuration = this.configurationService.load();
    final MessageService newMessages = new MessageService(this, configuration.globalSettings().language());
    final RuleSnapshot newSnapshot = RuleCompiler.compile(configuration);

    for (final ConfigIssue issue : configuration.issues()) {
      if (issue.severity() == ConfigIssue.Severity.ERROR) {
        this.getLogger().warning(issue.toLogLine());
      } else {
        this.getLogger().info(issue.toLogLine());
      }
    }

    if (configuration.globalSettings().strictMode() && configuration.issues().stream()
        .anyMatch(issue -> issue.severity() == ConfigIssue.Severity.ERROR)) {
      this.messages = newMessages;
      return false;
    }

    this.messages = newMessages;
    this.snapshot.set(newSnapshot);
    return true;
  }

  public RuleSnapshot snapshot() {
    return Objects.requireNonNull(this.snapshot.get(), "snapshot");
  }

  public MessageService messages() {
    return Objects.requireNonNull(this.messages, "messages");
  }

  public GenerationStats stats() {
    return Objects.requireNonNull(this.stats, "stats");
  }

  public WorldRegistrationService worldRegistrationService() {
    return Objects.requireNonNull(this.worldRegistrationService, "worldRegistrationService");
  }
}
