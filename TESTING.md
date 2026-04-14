# Testing Matrix

## Executed In This Workspace

### Build / Static Validation

- `./gradlew test`
- Result: passed

### Unit Coverage

- Rule precedence test:
  global -> environment -> world -> biome override order
- Generation behavior tests:
  disabled ore removes existing generated ore blocks
  generated ore stays inside configured Y range

### Resource / Packaging Validation

- `plugin.yml` present and expanded through Gradle resources
- bilingual config and lang files included in jar resources
- preset files included in jar resources

## Intended Runtime Matrix For Release Sign-Off

These scenarios are the recommended real-server acceptance matrix for packaging the jar as a release artifact:

### Platform

- Paper `1.21.x`
- Folia `1.21.x`

### World Coverage

- Overworld
- Nether
- End
- at least one custom-named extra world

### Compatibility Coverage

- vanilla generation
- Terralith-style altered biomes/terrain
- Tectonic-style altered terrain/caves
- Incendium or altered Nether generation
- combined datapack stack

### Rule Coverage

- disable an ore entirely
- heavily boost an ore
- large but rare veins
- small but common veins
- biome-specific buff
- biome-specific nerf
- invalid host rejection
- strict Y-range enforcement
- ancient debris defaults

### Stability Coverage

- cold startup
- shutdown
- `/fug reload`
- world created after startup
- pre-generated chunks stay unchanged
- newly generated chunks receive new rules
- no unsafe async warnings

### Performance Coverage

- chunk generation stress or pregeneration tool run
- compare generation throughput with plugin disabled
- inspect console for repeated exception suppression

## Current Assessment

- The code compiles and the core logic tests pass.
- The architecture is release-oriented and avoids generator replacement.
- Before public release, the runtime matrix above should still be executed on actual Paper and Folia servers with representative datapack stacks.
