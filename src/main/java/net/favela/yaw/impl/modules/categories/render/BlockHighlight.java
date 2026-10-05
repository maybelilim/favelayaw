package net.favela.yaw.impl.modules.categories.render;

import com.google.auto.service.AutoService;
import com.mojang.blaze3d.vertex.PoseStack;
import net.favela.yaw.impl.event.events.Render3DEvent;
import net.favela.yaw.impl.event.events.RenderBlockOutlineEvent;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.setting.settings.ColorSetting;
import net.favela.yaw.impl.setting.settings.EnumSetting;
import net.favela.yaw.impl.setting.settings.NumberSetting;
import net.favela.yaw.impl.util.render.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.awt.Color;

import static net.favela.yaw.impl.util.wrapper.Wrapper.MC;

@AutoService(Module.class)
public class BlockHighlight extends Module {

    private static final float FADE_EPSILON = 0.01f;

    public final EnumSetting<Mode> mode = enm("Mode", "Render mode", Mode.Fill);
    public final ColorSetting color = color("Color", "Highlight color", new Color(163, 135, 255, 255), true);
    public final NumberSetting lineWidth = num("LineWidth", "Outline width", 0.1f, 5.0f, 1.0f);
    public final NumberSetting fillAlpha = num("FillAlpha", () -> mode.get() != Mode.Outline, 0, 255, 40);
    public final EnumSetting<AnimationMode> animation = enm("Animation", "Animation mode", AnimationMode.EaseOut);
    public final NumberSetting animationTime = num("AnimTime", () -> animation.get() != AnimationMode.None, 0, 1000, 150);

    private BlockPos targetPos;
    private AABB targetBox;
    private AABB shownBox;
    private AABB fromBox;
    private AABB fadeOutBox;
    private long animationStart;

    public BlockHighlight() {
        super("BlockHighlight", "Draws box at the block that you are looking at", Category.RENDER);
    }

    @Override
    public void onDisable() {
        reset();
    }

    @Override
    public void onRenderBlockOutline(RenderBlockOutlineEvent event) {
        event.cancel();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (MC.level == null || MC.player == null) return;

        AABB box = findTargetBox();
        if (box == null) {
            reset();
            return;
        }

        BlockPos pos = ((BlockHitResult) MC.hitResult).getBlockPos();
        if (!pos.equals(targetPos)) {
            beginTransition(pos, box);
        }

        if (!isAnimated()) {
            shownBox = box;
            draw(event.getMatrix(), box, 1.0f);
            return;
        }

        if (animation.get() == AnimationMode.Fade) {
            renderFade(event.getMatrix());
        } else {
            renderMotion(event.getMatrix());
        }
    }

    private AABB findTargetBox() {
        if (!(MC.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return null;
        BlockPos pos = hit.getBlockPos();
        VoxelShape shape = MC.level.getBlockState(pos).getShape(MC.level, pos);
        return shape.isEmpty() ? null : shape.bounds().move(pos);
    }

    private void beginTransition(BlockPos pos, AABB box) {
        if (isAnimated()) {
            if (animation.get() == AnimationMode.Fade) {
                fadeOutBox = shownBox;
            } else {
                fromBox = shownBox != null ? shownBox : box;
            }
            animationStart = System.currentTimeMillis();
        }
        targetPos = pos;
        targetBox = box;
    }

    private void renderMotion(PoseStack stack) {
        float eased = easedProgress();
        shownBox = lerp(fromBox, targetBox, eased);
        draw(stack, shownBox, 1.0f);
        if (eased >= 1.0f) {
            fromBox = targetBox;
        }
    }

    private void renderFade(PoseStack stack) {
        float eased = easedProgress();
        if (fadeOutBox != null && 1.0f - eased > FADE_EPSILON) {
            draw(stack, fadeOutBox, 1.0f - eased);
        }
        if (eased > FADE_EPSILON) {
            draw(stack, targetBox, eased);
        }
        shownBox = targetBox;
        if (eased >= 1.0f) {
            fadeOutBox = null;
        }
    }

    private float easedProgress() {
        double progress = (double) (System.currentTimeMillis() - animationStart) / animationTime.getInt();
        return (float) ease(clamp(progress, 0.0, 1.0));
    }

    private double ease(double progress) {
        return switch (animation.get()) {
            case None, Linear -> progress;
            case Fade, EaseOut -> {
                double inverse = 1.0 - progress;
                yield 1.0 - inverse * inverse * inverse;
            }
            case EaseIn -> progress * progress * progress;
            case EaseInOut -> {
                if (progress < 0.5) {
                    yield 4.0 * progress * progress * progress;
                }
                double value = -2.0 * progress + 2.0;
                yield 1.0 - (value * value * value) / 2.0;
            }
            case Elastic -> {
                if (progress == 0.0 || progress == 1.0) {
                    yield progress;
                }
                double c4 = (2.0 * Math.PI) / 3.0;
                yield Math.pow(2.0, -10.0 * progress) * Math.sin((progress * 10.0 - 0.75) * c4) + 1.0;
            }
        };
    }

    private void draw(PoseStack stack, AABB box, float factor) {
        Color base = color.get();
        Mode m = mode.get();
        if (m != Mode.Outline) {
            RenderUtil.drawBoxFilled(stack, box, withAlpha(base, (int) (fillAlpha.getInt() * factor)));
        }
        if (m != Mode.Fill) {
            RenderUtil.drawBoxOutline(stack, box, withAlpha(base, (int) (base.getAlpha() * factor)), lineWidth.getFloat());
        }
    }

    private Color withAlpha(Color base, int alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), clamp(alpha, 0, 255));
    }

    private AABB lerp(AABB from, AABB to, double progress) {
        return new AABB(
                lerp(from.minX, to.minX, progress),
                lerp(from.minY, to.minY, progress),
                lerp(from.minZ, to.minZ, progress),
                lerp(from.maxX, to.maxX, progress),
                lerp(from.maxY, to.maxY, progress),
                lerp(from.maxZ, to.maxZ, progress)
        );
    }

    private double lerp(double from, double to, double progress) {
        return from + (to - from) * progress;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean isAnimated() {
        return animation.get() != AnimationMode.None && animationTime.getInt() > 0;
    }

    private void reset() {
        targetPos = null;
        targetBox = null;
        shownBox = null;
        fromBox = null;
        fadeOutBox = null;
        animationStart = 0L;
    }

    public enum Mode {
        Outline,
        Fill,
        Both
    }

    public enum AnimationMode {
        None,
        Linear,
        EaseIn,
        EaseOut,
        EaseInOut,
        Elastic,
        Fade
    }
}