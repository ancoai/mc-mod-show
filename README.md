# MC Mod Show Background

This Forge mod for Minecraft 1.20.1 replaces the main menu background with a user-specified image. The mod automatically normalizes the chosen picture so it fills the screen without distortion.

## Features
- Client-side replacement of the Minecraft title screen background.
- Automatic scaling and letterboxing to 1920×1080 while preserving aspect ratio.
- Default placeholder background generated in the config directory for easy customization.

## Getting Started
1. Build or run the mod with Forge 1.20.1 (47.2.0).
2. Launch the game once to generate the configuration and a placeholder background at `config/mcmodshow/background.png`.
3. Replace the generated image with your own. Any standard image format supported by Java (`png`, `jpg`, etc.) works.
4. (Optional) Edit `config/mcmodshow-client.toml` to point to a different file name or location within the `config` directory.

The mod will reload the background automatically after the config is saved or when the game restarts.

The repository avoids shipping binary assets; the placeholder background is generated procedurally when the config file is created.

## Building
Use Gradle with ForgeGradle 6 and Java 17 to build the project. Example:

```bash
gradle build
```

The compiled mod jar will be located in `build/libs/`.
