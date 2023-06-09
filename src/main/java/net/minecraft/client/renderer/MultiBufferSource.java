package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.optifine.render.VertexBuilderDummy;
import net.optifine.util.TextureUtils;

public interface MultiBufferSource
{
    static MultiBufferSource.BufferSource immediate(BufferBuilder pBuilder)
    {
        return immediateWithBuffers(ImmutableMap.of(), pBuilder);
    }

    static MultiBufferSource.BufferSource immediateWithBuffers(Map<RenderType, BufferBuilder> pMapBuilders, BufferBuilder pBuilder)
    {
        return new MultiBufferSource.BufferSource(pBuilder, pMapBuilders);
    }

    VertexConsumer getBuffer(RenderType pRenderType);

default void flushRenderBuffers()
    {
    }

    public static class BufferSource implements MultiBufferSource
    {
        protected final BufferBuilder builder;
        protected final Map<RenderType, BufferBuilder> fixedBuffers;
        protected RenderType lastState = null;
        protected final Set<BufferBuilder> startedBuffers = Sets.newIdentityHashSet();
        private final VertexConsumer DUMMY_BUFFER = new VertexBuilderDummy(this);

        protected BufferSource(BufferBuilder pBuilder, Map<RenderType, BufferBuilder> pFixedBuffers)
        {
            this.builder = pBuilder;
            this.fixedBuffers = pFixedBuffers;
            this.builder.setRenderTypeBuffer(this);

            for (BufferBuilder bufferbuilder : pFixedBuffers.values())
            {
                bufferbuilder.setRenderTypeBuffer(this);
            }
        }

        public VertexConsumer getBuffer(RenderType pRenderType)
        {
            BufferBuilder bufferbuilder = this.getBuilderRaw(pRenderType);

            if (!Objects.equals(this.lastState, pRenderType) || !pRenderType.canConsolidateConsecutiveGeometry())
            {
                if (this.lastState != null)
                {
                    RenderType rendertype = this.lastState;

                    if (!this.fixedBuffers.containsKey(rendertype))
                    {
                        this.endBatch(rendertype);
                    }
                }

                if (this.startedBuffers.add(bufferbuilder))
                {
                    bufferbuilder.setRenderType(pRenderType);
                    bufferbuilder.begin(pRenderType.mode(), pRenderType.format());
                }

                this.lastState = pRenderType;
            }

            return (VertexConsumer)(pRenderType.getTextureLocation() == TextureUtils.LOCATION_TEXTURE_EMPTY ? this.DUMMY_BUFFER : bufferbuilder);
        }

        private BufferBuilder getBuilderRaw(RenderType pRenderType)
        {
            return this.fixedBuffers.getOrDefault(pRenderType, this.builder);
        }

        public void endLastBatch()
        {
            if (this.lastState != null)
            {
                RenderType rendertype = this.lastState;

                if (!this.fixedBuffers.containsKey(rendertype))
                {
                    this.endBatch(rendertype);
                }

                this.lastState = null;
            }
        }

        public void endBatch()
        {
            if (!this.startedBuffers.isEmpty())
            {
                if (this.lastState != null)
                {
                    VertexConsumer vertexconsumer = this.getBuffer(this.lastState);

                    if (vertexconsumer == this.builder)
                    {
                        this.endBatch(this.lastState);
                    }
                }

                if (!this.startedBuffers.isEmpty())
                {
                    for (RenderType rendertype : this.fixedBuffers.keySet())
                    {
                        this.endBatch(rendertype);

                        if (this.startedBuffers.isEmpty())
                        {
                            break;
                        }
                    }
                }
            }
        }

        public void endBatch(RenderType pRenderType)
        {
            BufferBuilder bufferbuilder = this.getBuilderRaw(pRenderType);
            boolean flag = Objects.equals(this.lastState, pRenderType);

            if ((flag || bufferbuilder != this.builder) && this.startedBuffers.remove(bufferbuilder))
            {
                pRenderType.end(bufferbuilder, 0, 0, 0);

                if (flag)
                {
                    this.lastState = null;
                }
            }
        }

        public VertexConsumer getBuffer(ResourceLocation textureLocation, VertexConsumer def)
        {
            if (!(this.lastState instanceof RenderType.CompositeRenderType))
            {
                return def;
            }
            else
            {
                textureLocation = RenderType.getCustomTexture(textureLocation);
                RenderType.CompositeRenderType rendertype$compositerendertype = (RenderType.CompositeRenderType)this.lastState;
                RenderType.CompositeRenderType rendertype$compositerendertype1 = rendertype$compositerendertype.getTextured(textureLocation);
                return this.getBuffer(rendertype$compositerendertype1);
            }
        }

        public RenderType getLastRenderType()
        {
            return this.lastState;
        }

        public void flushRenderBuffers()
        {
            RenderType rendertype = this.lastState;
            this.endBatch();

            if (rendertype != null)
            {
                this.getBuffer(rendertype);
            }
        }

        public VertexConsumer getDummyBuffer()
        {
            return this.DUMMY_BUFFER;
        }
    }
}
