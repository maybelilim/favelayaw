package net.favela.yaw.impl.util.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.Optional;

import static net.minecraft.client.renderer.RenderPipelines.DEBUG_FILLED_SNIPPET;
import static net.minecraft.client.renderer.RenderPipelines.LINES_SNIPPET;

public class Pipelines {

    static final RenderPipeline GLOBAL_QUADS_PIPELINE = RenderPipeline.builder(DEBUG_FILLED_SNIPPET)
            .withLocation("pipeline/global_fill_pipeline")
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withDepthStencilState(Optional.empty())
            .build();

    static final RenderPipeline GLOBAL_LINES_PIPELINE = RenderPipeline.builder(LINES_SNIPPET)
            .withLocation("pipeline/global_lines_pipeline")
            .withDepthStencilState(Optional.empty())
            .build();
}