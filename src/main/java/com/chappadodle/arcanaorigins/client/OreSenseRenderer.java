package com.chappadodle.arcanaorigins.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;
import java.util.OptionalDouble;

/**
 * Prismborn's Ore Sense on the client: outlines the ores the server just felt (OreSensePayload),
 * seen through the rock, each in its ore's colour, fading out over two seconds.
 */
public final class OreSenseRenderer {
    private static final int SHOWN_TICKS = 40;
    private static final RenderType LINES = Types.SEE_THROUGH_LINES;

    private static List<BlockPos> ores = List.of();
    private static long shownAt;

    private OreSenseRenderer() {
    }

    public static void show(List<BlockPos> sensed) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            ores = List.copyOf(sensed);
            shownAt = level.getGameTime();
        }
    }

    public static void render(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (ores.isEmpty() || level == null) {
            return;
        }
        float age = (level.getGameTime() - shownAt + event.getPartialTick().getGameTimeDeltaPartialTick(false)) / SHOWN_TICKS;
        if (age >= 1f || age < 0f) {
            ores = List.of();
            return;
        }
        float alpha = age < 0.5f ? 0.9f : 0.9f * (1f - (age - 0.5f) / 0.5f);
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(LINES);
        for (BlockPos pos : ores) {
            int color = colorOf(level.getBlockState(pos));
            LevelRenderer.renderLineBox(poseStack, lines, new AABB(pos).deflate(0.02),
                    (color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, alpha);
        }
        // The render type's "always" depth test only skips enabling the test; it's on at this
        // stage, so turn it off here for the outlines to show through the rock.
        RenderSystem.disableDepthTest();
        buffers.endBatch(LINES);
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }

    /** Each ore in its own colour; anything else ore-like in Prismborn violet. */
    private static int colorOf(BlockState state) {
        if (state.is(BlockTags.DIAMOND_ORES)) {
            return 0x5FF5E8;
        } else if (state.is(BlockTags.EMERALD_ORES)) {
            return 0x3DE070;
        } else if (state.is(BlockTags.GOLD_ORES) || state.is(Blocks.NETHER_GOLD_ORE)) {
            return 0xFFD640;
        } else if (state.is(BlockTags.IRON_ORES)) {
            return 0xE8B898;
        } else if (state.is(BlockTags.COPPER_ORES)) {
            return 0xE87C48;
        } else if (state.is(BlockTags.REDSTONE_ORES)) {
            return 0xFF3030;
        } else if (state.is(BlockTags.LAPIS_ORES)) {
            return 0x3A6AFF;
        } else if (state.is(BlockTags.COAL_ORES)) {
            return 0x707070;
        } else if (state.is(Blocks.NETHER_QUARTZ_ORE)) {
            return 0xF4EEE6;
        } else if (state.is(Blocks.ANCIENT_DEBRIS)) {
            return 0x9A6A50;
        }
        return 0xD08CFF;
    }

    /** RenderType's state pieces are protected; a subclass reaches them. */
    private abstract static class Types extends RenderType {
        static final RenderType SEE_THROUGH_LINES = create("arcanaorigins_ore_sense", DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.LINES, 1536, false, false,
                CompositeState.builder()
                        .setShaderState(RENDERTYPE_LINES_SHADER)
                        .setLineState(new LineStateShard(OptionalDouble.of(2.5)))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false));

        private Types(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling,
                      boolean sortOnUpload, Runnable setup, Runnable clear) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
        }
    }
}
