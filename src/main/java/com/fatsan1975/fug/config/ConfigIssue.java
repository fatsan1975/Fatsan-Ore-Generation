package com.fatsan1975.fug.config;

public record ConfigIssue(Severity severity, String file, String path, String value, String message) {

  public enum Severity {
    WARNING,
    ERROR
  }

  public String toLogLine() {
    return "[%s] %s @ %s (%s): %s".formatted(severity, file, path, value, message);
  }
}
