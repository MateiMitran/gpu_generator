# WorldGenerator

A Minecraft plugin that uses GPU-accelerated noise generation for procedural world generation.

## Overview

WorldGenerator is a custom Minecraft world generator plugin that leverages GPU acceleration through the Aparapi library to create diverse and performant procedural terrain. The plugin uses OpenSimplex2 noise algorithms to generate realistic landscapes with various biomes, including:

- Plains, forests, and hills
- Oceans (warm, cold, frozen) with depth variations
- Rivers with custom riverbed generation
- Deserts and beaches
- Mountains and other elevated terrain

## Features

- **GPU-Accelerated Noise Generation**: Utilizes GPU processing power via Aparapi for faster world generation
- **Biome-Specific Terrain**: Different terrain generation rules based on Minecraft biomes
- **Parallelization**: Built to be parallel-capable for improved performance
- **Vanilla Integration**: Compatible with vanilla Minecraft caves, structures, decorations, and mob spawning

## Requirements

- Minecraft server version 1.15 or higher
- Java 8 or higher
- A GPU compatible with Aparapi (most modern GPUs)

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. The plugin will automatically handle world generation for new worlds

## Configuration

Currently, the plugin has minimal configuration options. Future releases will include more customization.

## Usage

The plugin will automatically register itself as a world generator. To use it for a specific world:

1. Edit your `bukkit.yml` file
2. Find the `worlds` section
3. Add or modify a world entry:

```yaml
worlds:
  world_name:
    generator: WorldGenerator
```

4. Create or reset the world to apply the generator

## Development

### Building from Source

1. Clone the repository
2. Build using Maven: `mvn clean package`
3. The compiled JAR will be in the `target` directory

### Project Structure

- `Core.java`: Main plugin class
- `GPUChunkGenerator.java`: Custom chunk generator implementation
- `OpenSimplex2.java`: Interface to GPU-accelerated noise generation
- `OpenSimplex2NoiseGenerator.java`: Aparapi kernel for noise generation

## Future Development

- Tree and ore populators
- Enhanced biome-specific features
- Additional configuration options
- Improved performance optimizations
- Custom structures

## Credits

- Developed by Shockbyte
- Utilizes Aparapi for GPU computing
- Based on OpenSimplex2 noise algorithms

## License

[Insert your license information here]
