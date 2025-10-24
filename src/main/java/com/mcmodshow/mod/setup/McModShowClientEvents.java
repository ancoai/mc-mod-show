package com.mcmodshow.mod.setup;

import com.mcmodshow.mod.McModShowMod;
import com.mcmodshow.mod.client.NormalizedImageLoader;
import com.mcmodshow.mod.config.McModShowConfig;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class McModShowClientEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private static ResourceLocation activeBackground;
    @Nullable
    private static DynamicTexture dynamicTexture;

    private McModShowClientEvents() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(McModShowClientEvents::onClientSetup);
        modEventBus.addListener(McModShowClientEvents::onConfigReload);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(McModShowClientEvents::reloadBackgroundTexture);
    }

    private static void onConfigReload(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == McModShowConfig.SPEC) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.execute(McModShowClientEvents::reloadBackgroundTexture);
        }
    }

    private static void reloadBackgroundTexture() {
        Minecraft minecraft = Minecraft.getInstance();
        TextureManager textureManager = minecraft.getTextureManager();

        if (dynamicTexture != null && activeBackground != null) {
            textureManager.release(activeBackground);
            try {
                dynamicTexture.close();
            } catch (IOException exception) {
                LOGGER.warn("Failed to close dynamic background texture", exception);
            }
            dynamicTexture = null;
            activeBackground = null;
        }

        Path configDir = FMLPaths.CONFIGDIR.get();
        Path backgroundPath = configDir.resolve(McModShowConfig.BACKGROUND_PATH.get());

        if (!Files.exists(backgroundPath)) {
            createDefaultBackgroundFile(backgroundPath);
        }

        if (Files.exists(backgroundPath)) {
            try {
                NativeImage image = NormalizedImageLoader.load(backgroundPath);
                dynamicTexture = new DynamicTexture(image);
                activeBackground = textureManager.register(McModShowMod.MOD_ID + "_menu_background", dynamicTexture);
                LOGGER.info("Loaded menu background from {}", backgroundPath);
            } catch (IOException exception) {
                LOGGER.warn("Failed to load configured background '{}'. Using vanilla title screen.", backgroundPath, exception);
                activeBackground = null;
            }
        } else {
            LOGGER.warn("No menu background available at {}. Using vanilla title screen.", backgroundPath);
            activeBackground = null;
        }
    }

    private static void createDefaultBackgroundFile(Path targetPath) {
        try {
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }
            NormalizedImageLoader.writePlaceholder(targetPath);
        } catch (IOException exception) {
            LOGGER.warn("Unable to create default background file at {}", targetPath, exception);
        }
    }

    public static ResourceLocation getActiveBackground() {
        return activeBackground;
    }

    @Mod.EventBusSubscriber(modid = McModShowMod.MOD_ID, value = Dist.CLIENT)
    public static final class ScreenRendering {
        private ScreenRendering() {
        }

        @SubscribeEvent
        public static void onBackgroundRender(ScreenEvent.Render.Background event) {
            if (!(event.getScreen() instanceof TitleScreen)) {
                return;
            }

            if (activeBackground == null) {
                return;
            }

            GuiGraphics guiGraphics = event.getGuiGraphics();
            int width = event.getScreen().width;
            int height = event.getScreen().height;

            renderBackground(guiGraphics, width, height);
            event.setCanceled(true);
        }

        private static void renderBackground(GuiGraphics guiGraphics, int width, int height) {
            RenderSystem.setShaderTexture(0, getActiveBackground());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            guiGraphics.blit(getActiveBackground(), 0, 0, 0, 0, width, height, 1920, 1080);
            RenderSystem.disableBlend();
        }
    }
}
