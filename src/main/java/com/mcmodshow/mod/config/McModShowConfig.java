package com.mcmodshow.mod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class McModShowConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<String> BACKGROUND_PATH;

    private static final String DEFAULT_PATH = "mcmodshow/background.png";

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Client configuration for MC Mod Show").push("client");
        BACKGROUND_PATH = builder
            .comment("Relative path (inside the config directory) to the background image that replaces the main menu backdrop.",
                    "The image will be automatically normalized to 1920x1080 while keeping its aspect ratio.")
            .define("backgroundPath", DEFAULT_PATH);
        builder.pop();

        SPEC = builder.build();
    }

    private McModShowConfig() {
    }

    public static String getDefaultPath() {
        return DEFAULT_PATH;
    }
}
