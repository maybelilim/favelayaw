package net.favela.yaw.impl.util.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.Optional;
import java.util.OptionalDouble;

public class Drawer {

    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(1536);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static MappableRingBuffer vertexBuffer;
    private static int vertexBufferCapacity;

    public static ByteBufferBuilder allocator() {
        return ALLOCATOR;
    }

    public static void draw(RenderPipeline pipeline, MeshData mesh) {
        Minecraft client = Minecraft.getInstance();
        var drawState = mesh.drawState();
        int vertexBufferSize = drawState.vertexCount() * drawState.format().getVertexSize();

        if (vertexBuffer == null || vertexBufferCapacity < vertexBufferSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }
            vertexBuffer = new MappableRingBuffer(() -> "yaw vertices", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
            vertexBufferCapacity = vertexBufferSize;
        }

        GpuBuffer vertices = vertexBuffer.currentBuffer();
        try (var view = vertices.map(0L, mesh.vertexBuffer().remaining(), false, true)) {
            MemoryUtil.memCopy(mesh.vertexBuffer(), view.data());
        }

        var sequentialBuffer = RenderSystem.getSequentialBuffer(pipeline.getPrimitiveTopology());
        GpuBuffer indices = sequentialBuffer.getBuffer(drawState.indexCount());
        var indexType = sequentialBuffer.type();

        var dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        RenderTarget target = client.gameRenderer.mainRenderTarget();

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "yaw render", target.getColorTextureView(), Optional.empty(), null, OptionalDouble.empty())) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            pass.setVertexBuffer(0, vertices.slice());
            pass.setIndexBuffer(indices, indexType);
            pass.drawIndexed(drawState.indexCount(), 1, 0, 0, 0);
        }

        vertexBuffer.rotate();
        mesh.close();
    }
}