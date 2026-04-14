package com.fatsan1975.fug.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConfigIssueCollector {
  private final List<ConfigIssue> issues = new ArrayList<>();

  public void warning(final String file, final String path, final String value, final String message) {
    this.issues.add(new ConfigIssue(ConfigIssue.Severity.WARNING, file, path, value, message));
  }

  public void error(final String file, final String path, final String value, final String message) {
    this.issues.add(new ConfigIssue(ConfigIssue.Severity.ERROR, file, path, value, message));
  }

  public boolean hasErrors() {
    return this.issues.stream().anyMatch(issue -> issue.severity() == ConfigIssue.Severity.ERROR);
  }

  public List<ConfigIssue> snapshot() {
    return Collections.unmodifiableList(new ArrayList<>(this.issues));
  }
}
