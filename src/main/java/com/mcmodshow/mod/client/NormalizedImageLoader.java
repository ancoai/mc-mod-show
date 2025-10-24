package com.mcmodshow.mod.client;

import com.mojang.blaze3d.platform.NativeImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class NormalizedImageLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(NormalizedImageLoader.class);
    private static final int TARGET_WIDTH = 1920;
    private static final int TARGET_HEIGHT = 1080;

    private NormalizedImageLoader() {
    }

    public static NativeImage load(Path imagePath) throws IOException {
        BufferedImage original = ImageIO.read(imagePath.toFile());
        if (original == null) {
            throw new IOException("Unsupported image format: " + imagePath);
        }

        BufferedImage normalized = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = normalized.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, TARGET_WIDTH, TARGET_HEIGHT);

        double scale = Math.max((double) TARGET_WIDTH / original.getWidth(), (double) TARGET_HEIGHT / original.getHeight());
        int scaledWidth = (int) Math.round(original.getWidth() * scale);
        int scaledHeight = (int) Math.round(original.getHeight() * scale);
        int offsetX = (TARGET_WIDTH - scaledWidth) / 2;
        int offsetY = (TARGET_HEIGHT - scaledHeight) / 2;

        graphics.drawImage(original, offsetX, offsetY, offsetX + scaledWidth, offsetY + scaledHeight, 0, 0, original.getWidth(), original.getHeight(), null);
        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(normalized, "png", output);

        try (ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray())) {
            return NativeImage.read(input);
        } finally {
            try {
                output.close();
            } catch (IOException exception) {
                LOGGER.debug("Failed to close output stream", exception);
            }
        }
    }

    public static void writePlaceholder(Path targetPath) throws IOException {
        BufferedImage placeholder = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = placeholder.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.Src);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            LinearGradientPaint gradient = new LinearGradientPaint(
                0f,
                0f,
                0f,
                TARGET_HEIGHT,
                new float[]{0f, 1f},
                new Color[]{new Color(0x1f1f1f), new Color(0x111111)}
            );
            graphics.setPaint(gradient);
            graphics.fillRect(0, 0, TARGET_WIDTH, TARGET_HEIGHT);

            String headline = "MC Mod Show";
            graphics.setColor(new Color(0xffffff));
            graphics.setFont(new Font("SansSerif", Font.BOLD, 96));
            drawCenteredString(graphics, headline, TARGET_WIDTH / 2, TARGET_HEIGHT / 2 - 80);

            graphics.setFont(new Font("SansSerif", Font.PLAIN, 36));
            graphics.setColor(new Color(0xdddddd));
            drawCenteredString(graphics, "Replace this image with your own background.", TARGET_WIDTH / 2, TARGET_HEIGHT / 2 + 10);
            drawCenteredString(graphics, "The mod will resize it to 1920x1080 automatically.", TARGET_WIDTH / 2, TARGET_HEIGHT / 2 + 60);
        } finally {
            graphics.dispose();
        }

        try (OutputStream output = Files.newOutputStream(targetPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            ImageIO.write(placeholder, "png", output);
        }

        LOGGER.info("Generated placeholder background at {}", targetPath);
    }

    private static void drawCenteredString(Graphics2D graphics, String text, int centerX, int baselineY) {
        FontMetrics metrics = graphics.getFontMetrics();
        int width = metrics.stringWidth(text);
        graphics.drawString(text, centerX - width / 2, baselineY);
    }
}
