package com.chappadodle.arcanaorigins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/**
 * A six-sided shaft of crystal (Elemental Arcana's Crystal Spire drew clusters of them), for the
 * Geode Sentinel.
 */
public final class CrystalShaft {
    private static final int SIDES = 6;

    private CrystalShaft() {
    }

    /** A six-sided shaft {@code height} tall and {@code radius} wide, its top fifth a point (the Geode Sentinel's too). */
    public static void shaft(VertexConsumer buffer, PoseStack.Pose pose, float height, float radius, float red, float green, float blue) {
        float body = height * 0.8f;
        for (int i = 0; i < SIDES; i++) {
            float a0 = Mth.TWO_PI * i / SIDES;
            float a1 = Mth.TWO_PI * (i + 1) / SIDES;
            float x0 = Mth.cos(a0) * radius;
            float z0 = Mth.sin(a0) * radius;
            float x1 = Mth.cos(a1) * radius;
            float z1 = Mth.sin(a1) * radius;
            float u0 = (i % 3) / 3f;
            float u1 = (i % 3 + 1) / 3f;
            // Faces turned away from the light are a little darker, like blocks.
            float shade = 0.8f + 0.2f * Mth.cos((a0 + a1) / 2f);
            vertex(buffer, pose, x0, 0, z0, u0, 1f, red * shade, green * shade, blue * shade);
            vertex(buffer, pose, x0, body, z0, u0, 0.2f, red * shade, green * shade, blue * shade);
            vertex(buffer, pose, x1, body, z1, u1, 0.2f, red * shade, green * shade, blue * shade);
            vertex(buffer, pose, x1, 0, z1, u1, 1f, red * shade, green * shade, blue * shade);
            // The point, as a quad with its tip doubled.
            vertex(buffer, pose, x0, body, z0, u0, 0.2f, red, green, blue);
            vertex(buffer, pose, 0, height, 0, (u0 + u1) / 2f, 0f, red, green, blue);
            vertex(buffer, pose, 0, height, 0, (u0 + u1) / 2f, 0f, red, green, blue);
            vertex(buffer, pose, x1, body, z1, u1, 0.2f, red, green, blue);
        }
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v,
                               float red, float green, float blue) {
        buffer.addVertex(pose, x, y, z)
                .setColor(red, green, blue, 0.9f)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0, 1, 0);
    }
}
