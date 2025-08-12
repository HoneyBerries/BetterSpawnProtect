# BetterSpawnProtect

BetterSpawnProtect is a simple and efficient Minecraft plugin that protects a defined spawn area from being modified by players. It's designed to be lightweight and easy to use, providing essential protection for your server's spawn.

## Features

- **Spawn Protection:** Protects a circular area from block breaking, placing, and other modifications.
- **PvP Enabled:** Players can still engage in PvP combat within the protected area.
- **Entity Protection:** Prevents players from harming non-player entities and vice-versa within the protected zone.
- **Configurable:** Easily configure the protection center, radius, and world.
- **Bypass Permission:** Allows administrators to bypass the protection with a permission node.
- **Lightweight:** Designed to have minimal impact on server performance.

## Configuration

The configuration is located in `plugins/BetterSpawnProtect/config.yml`.

```yaml
protection:
  # The world where the spawn protection is active.
  world: world
  center:
    # The coordinates of the center of the protected area.
    x: 0.5
    y: 64.0
    z: 0.5
  # The radius of the protected area.
  radius: 64.0
```

## Commands

- `/bsp reload` - Reloads the configuration.
- `/bsp setcenter` - Sets the center of the protected area to your current location.
- `/bsp setradius <radius>` - Sets the radius of the protected area.

## Permissions

- `betterspawnprotect.bypass` - Allows a player to bypass the spawn protection.
- `betterspawnprotect.admin` - Allows a player to use the admin commands.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
