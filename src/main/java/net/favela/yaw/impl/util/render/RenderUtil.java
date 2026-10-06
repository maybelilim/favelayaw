package net.favela.yaw.impl.util.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;

import static net.favela.yaw.impl.util.wrapper.Wrapper.MC;

public final class RenderUtil {

    private static final int[] EDGES = {
            0, 1, 1, 2, 2, 3, 3, 0,
            4, 5, 5, 6, 6, 7, 7, 4,
            0, 4, 1, 5, 2, 6, 3, 7
    };

    private RenderUtil() {

    }

    public static void drawBoxFilled(PoseStack stack, AABB box, Color color) {
        Vec3 cam = MC.gameRenderer.mainCamera().position();
        PoseStack.Pose pose = stack.last();

        float minX = (float) (box.minX - cam.x);
        float minY = (float) (box.minY - cam.y);
        float minZ = (float) (box.minZ - cam.z);
        float maxX = (float) (box.maxX - cam.x);
        float maxY = (float) (box.maxY - cam.y);
        float maxZ = (float) (box.maxZ - cam.z);
        int rgba = color.getRGB();

        RenderPipeline pipeline = Pipelines.GLOBAL_QUADS_PIPELINE;
        BufferBuilder buffer = new BufferBuilder(Drawer.allocator(), pipeline.getPrimitiveTopology(), pipeline.getVertexFormatBinding(0));
        VertexConsumer v = buffer;

        v.addVertex(pose, minX, minY, minZ).setColor(rgba);
        v.addVertex(pose, maxX, minY, minZ).setColor(rgba);
        v.addVertex(pose, maxX, minY, maxZ).setColor(rgba);
        v.addVertex(pose, minX, minY, maxZ).setColor(rgba);

        v.addVertex(pose, minX, maxY, minZ).setColor(rgba);
        v.addVertex(pose, minX, maxY, maxZ).setColor(rgba);
        v.addVertex(pose, maxX, maxY, maxZ).setColor(rgba);
        v.addVertex(pose, maxX, maxY, minZ).setColor(rgba);

        v.addVertex(pose, minX, minY, minZ).setColor(rgba);
        v.addVertex(pose, minX, maxY, minZ).setColor(rgba);
        v.addVertex(pose, maxX, maxY, minZ).setColor(rgba);
        v.addVertex(pose, maxX, minY, minZ).setColor(rgba);

        v.addVertex(pose, maxX, minY, minZ).setColor(rgba);
        v.addVertex(pose, maxX, maxY, minZ).setColor(rgba);
        v.addVertex(pose, maxX, maxY, maxZ).setColor(rgba);
        v.addVertex(pose, maxX, minY, maxZ).setColor(rgba);

        v.addVertex(pose, minX, minY, maxZ).setColor(rgba);
        v.addVertex(pose, maxX, minY, maxZ).setColor(rgba);
        v.addVertex(pose, maxX, maxY, maxZ).setColor(rgba);
        v.addVertex(pose, minX, maxY, maxZ).setColor(rgba);

        v.addVertex(pose, minX, minY, minZ).setColor(rgba);
        v.addVertex(pose, minX, minY, maxZ).setColor(rgba);
        v.addVertex(pose, minX, maxY, maxZ).setColor(rgba);
        v.addVertex(pose, minX, maxY, minZ).setColor(rgba);

        Drawer.draw(pipeline, buffer.buildOrThrow());
    }

    public static void drawBoxOutline(PoseStack stack, AABB box, Color color, float lineWidth) {
        Vec3 cam = MC.gameRenderer.mainCamera().position();
        PoseStack.Pose pose = stack.last();

        float minX = (float) (box.minX - cam.x);
        float minY = (float) (box.minY - cam.y);
        float minZ = (float) (box.minZ - cam.z);
        float maxX = (float) (box.maxX - cam.x);
        float maxY = (float) (box.maxY - cam.y);
        float maxZ = (float) (box.maxZ - cam.z);
        int rgba = color.getRGB();

        float[] xs = {minX, maxX, maxX, minX, minX, maxX, maxX, minX};
        float[] ys = {minY, minY, minY, minY, maxY, maxY, maxY, maxY};
        float[] zs = {minZ, minZ, maxZ, maxZ, minZ, minZ, maxZ, maxZ};

        RenderPipeline pipeline = Pipelines.GLOBAL_LINES_PIPELINE;
        BufferBuilder buffer = new BufferBuilder(Drawer.allocator(), pipeline.getPrimitiveTopology(), pipeline.getVertexFormatBinding(0));

        for (int i = 0; i < EDGES.length; i += 2) {
            int a = EDGES[i];
            int b = EDGES[i + 1];
            float dx = xs[b] - xs[a];
            float dy = ys[b] - ys[a];
            float dz = zs[b] - zs[a];
            buffer.addVertex(pose, xs[a], ys[a], zs[a]).setNormal(pose, dx, dy, dz).setColor(rgba).setLineWidth(lineWidth);
            buffer.addVertex(pose, xs[b], ys[b], zs[b]).setNormal(pose, dx, dy, dz).setColor(rgba).setLineWidth(lineWidth);
        }

        Drawer.draw(pipeline, buffer.buildOrThrow());
    }

    public static void rect(GuiGraphicsExtractor ctx, double x, double y, double x2, double y2, int color) {
        ctx.fill((int) x, (int) y, (int) x2, (int) y2, color);
    }

    public static void drawRectOutline(GuiGraphicsExtractor ctx, int x, int y, int x2, int y2, int color) {
        ctx.outline(x, y, x2 - x, y2 - y, color);
    }

    public static void verticalGradient(GuiGraphicsExtractor ctx, double x, double y, double x2, double y2, Color top, Color bottom) {
        ctx.fillGradient((int) x, (int) y, (int) x2, (int) y2, top.getRGB(), bottom.getRGB());
    }

    public static void horizontalGradient(GuiGraphicsExtractor ctx, double x, double y, double x2, double y2, Color left, Color right) {
        int x0 = (int) x;
        int x1 = (int) x2;
        int y0 = (int) y;
        int y1 = (int) y2;
        int width = Math.max(1, x1 - x0);
        for (int i = 0; i < width; i++) {
            float f = width <= 1 ? 0f : i / (float) (width - 1);
            int r = (int) (left.getRed() + (right.getRed() - left.getRed()) * f);
            int g = (int) (left.getGreen() + (right.getGreen() - left.getGreen()) * f);
            int b = (int) (left.getBlue() + (right.getBlue() - left.getBlue()) * f);
            int a = (int) (left.getAlpha() + (right.getAlpha() - left.getAlpha()) * f);
            ctx.fill(x0 + i, y0, x0 + i + 1, y1, new Color(r, g, b, a).getRGB());
        }
    }
}
