# BetterSpawnProtect

BetterSpawnProtect is a lightweight and efficient Minecraft plugin designed to protect your server's spawn area from modifications by players. It offers a simple yet powerful solution for maintaining the integrity of your spawn, ensuring a safe and welcoming environment for all players.

## Features

- **Comprehensive Spawn Protection:** Safeguards a configurable circular area from block breaking, placing, and various other forms of modification.
- **PvP Support:** Allows players to engage in PvP combat within the protected spawn area, ensuring that gameplay remains exciting.
- **Entity Protection:** Protects non-player entities from being harmed by players and vice-versa, preserving the spawn's ecosystem.
- **Highly Configurable:** Provides an intuitive configuration file to easily set the protection center, radius, and world.
- **Bypass Permission:** Includes a bypass permission node for administrators, allowing them to make changes to the protected area without restrictions.
- **Optimized for Performance:** Engineered to be extremely lightweight, ensuring minimal impact on server performance.

## Configuration

The configuration for BetterSpawnProtect is located in `plugins/BetterSpawnProtect/config.yml`.

```yaml
protection:
  # The world where the spawn protection is active.
  # Default: "world"
  world: world
  center:
    # The coordinates of the center of the protected area.
    # Default: x=0.5, y=64.0, z=0.5
    x: 0.5
    y: 64.0
    z: 0.5
  # The radius of the protected area.
  # Default: 64.0
  radius: 64.0
```

## Commands

BetterSpawnProtect provides a set of simple and easy-to-use commands for managing the protected area.

| Command | Description |
| --- | --- |
| `/bsp reload` | Reloads the configuration from `config.yml`. |
| `/bsp setcenter` | Sets the center of the protected area to your current location. |
| `/bsp setradius <radius>` | Sets the radius of the protected area. |

## Permissions

The following permissions are available for BetterSpawnProtect:

| Permission | Description |
| --- | --- |
| `betterspawnprotect.bypass` | Allows a player to bypass the spawn protection. |
| `betterspawnprotect.admin` | Grants access to all admin commands. |

## Contributing

We welcome contributions to BetterSpawnProtect! If you have any ideas, suggestions, or bug reports, please feel free to open an issue or submit a pull request on our GitHub repository.

## License

This project is licensed under the MIT License. For more information, please see the [LICENSE](LICENSE) file.
