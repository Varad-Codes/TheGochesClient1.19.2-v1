package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.optifine.Config;
import net.optifine.render.MultiTextureData;
import net.optifine.render.MultiTextureRenderer;
import net.optifine.render.VboRange;
import net.optifine.render.VboRegion;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;

public class VertexBuffer implements AutoCloseable
{
    private int vertexBufferId;
    private int indexBufferId;
    private int arrayObjectId;
    @Nullable
    private VertexFormat format;
    @Nullable
    private RenderSystem.AutoStorageIndexBuffer sequentialIndices;
    private VertexFormat.IndexType indexType;
    private int indexCount;
    private VertexFormat.Mode mode;
    private VboRegion vboRegion;
    private VboRange vboRange;
    private MultiTextureData multiTextureData;

    public VertexBuffer()
    {
        RenderSystem.assertOnRenderThread();
        this.vertexBufferId = GlStateManager._glGenBuffers();
        this.indexBufferId = GlStateManager._glGenBuffers();
        this.arrayObjectId = GlStateManager._glGenVertexArrays();
    }

    public void upload(BufferBuilder.RenderedBuffer pBuilder)
    {
        if (!this.isInvalid())
        {
            RenderSystem.assertOnRenderThread();

            try
            {
                BufferBuilder.DrawState bufferbuilder$drawstate = pBuilder.drawState();
                this.format = this.uploadVertexBuffer(bufferbuilder$drawstate, pBuilder.vertexBuffer());
                this.sequentialIndices = this.uploadIndexBuffer(bufferbuilder$drawstate, pBuilder.indexBuffer());
                this.indexCount = bufferbuilder$drawstate.indexCount();
                this.indexType = bufferbuilder$drawstate.indexType();
                this.mode = bufferbuilder$drawstate.mode();

                if (this.vboRegion == null)
                {
                    this.multiTextureData = bufferbuilder$drawstate.getMultiTextureData();
                    return;
                }

                ByteBuffer bytebuffer = pBuilder.vertexBuffer();
                bytebuffer.position(0);
                bytebuffer.limit(bufferbuilder$drawstate.vertexBufferSize());
                this.vboRegion.bufferData(bytebuffer, this.vboRange);
                bytebuffer.position(0);
                bytebuffer.limit(bufferbuilder$drawstate.bufferSize());
            }
            finally
            {
                pBuilder.release();
            }
        }
    }

    private VertexFormat uploadVertexBuffer(BufferBuilder.DrawState p_231219_, ByteBuffer p_231220_)
    {
        if (this.vboRegion != null)
        {
            return p_231219_.format();
        }
        else
        {
            boolean flag = false;

            if (!p_231219_.format().equals(this.format))
            {
                if (this.format != null)
                {
                    this.format.clearBufferState();
                }

                GlStateManager._glBindBuffer(34962, this.vertexBufferId);
                p_231219_.format().setupBufferState();

                if (Config.isShaders())
                {
                    ShadersRender.setupArrayPointersVbo();
                }

                flag = true;
            }

            if (!p_231219_.indexOnly())
            {
                if (!flag)
                {
                    GlStateManager._glBindBuffer(34962, this.vertexBufferId);
                }

                RenderSystem.glBufferData(34962, p_231220_, 35044);
            }

            return p_231219_.format();
        }
    }

    @Nullable
    private RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(BufferBuilder.DrawState p_231224_, ByteBuffer p_231225_)
    {
        if (!p_231224_.sequentialIndex())
        {
            if (this.vboRegion != null)
            {
                return null;
            }
            else
            {
                GlStateManager._glBindBuffer(34963, this.indexBufferId);
                RenderSystem.glBufferData(34963, p_231225_, 35044);
                return null;
            }
        }
        else
        {
            RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(p_231224_.mode());
            int i = p_231224_.indexCount();

            if (this.vboRegion != null && p_231224_.mode() == VertexFormat.Mode.QUADS)
            {
                i = 65536;
            }

            if (rendersystem$autostorageindexbuffer != this.sequentialIndices || !rendersystem$autostorageindexbuffer.hasStorage(i))
            {
                rendersystem$autostorageindexbuffer.bind(i);
            }

            return rendersystem$autostorageindexbuffer;
        }
    }

    public void bind()
    {
        BufferUploader.invalidate();

        if (this.arrayObjectId >= 0)
        {
            GlStateManager._glBindVertexArray(this.arrayObjectId);
        }
    }

    public static void unbind()
    {
        BufferUploader.invalidate();
        GlStateManager._glBindVertexArray(0);
    }

    public void draw()
    {
        if (this.vboRegion != null)
        {
            this.vboRegion.drawArrays(VertexFormat.Mode.QUADS, this.vboRange);
        }
        else if (this.multiTextureData != null)
        {
            MultiTextureRenderer.draw(this.mode, this.getIndexType().asGLType, this.multiTextureData);
        }
        else
        {
            RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.getIndexType().asGLType);
        }
    }

    private VertexFormat.IndexType getIndexType()
    {
        RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = this.sequentialIndices;
        return rendersystem$autostorageindexbuffer != null ? rendersystem$autostorageindexbuffer.type() : this.indexType;
    }

    public void drawWithShader(Matrix4f pModelViewMatrix, Matrix4f pProjectionMatrix, ShaderInstance pShaderInstance)
    {
        if (!RenderSystem.isOnRenderThread())
        {
            RenderSystem.recordRenderCall(() ->
            {
                this._drawWithShader(pModelViewMatrix.copy(), pProjectionMatrix.copy(), pShaderInstance);
            });
        }
        else
        {
            this._drawWithShader(pModelViewMatrix, pProjectionMatrix, pShaderInstance);
        }
    }

    private void _drawWithShader(Matrix4f pModelViewMatrix, Matrix4f pProjectionMatrix, ShaderInstance pShaderInstance)
    {
        for (int i = 0; i < 12; ++i)
        {
            int j = RenderSystem.getShaderTexture(i);
            pShaderInstance.setSampler(i, j);
        }

        if (pShaderInstance.MODEL_VIEW_MATRIX != null)
        {
            pShaderInstance.MODEL_VIEW_MATRIX.set(pModelViewMatrix);
        }

        if (pShaderInstance.PROJECTION_MATRIX != null)
        {
            pShaderInstance.PROJECTION_MATRIX.set(pProjectionMatrix);
        }

        if (pShaderInstance.INVERSE_VIEW_ROTATION_MATRIX != null)
        {
            pShaderInstance.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
        }

        if (pShaderInstance.COLOR_MODULATOR != null)
        {
            pShaderInstance.COLOR_MODULATOR.a(RenderSystem.getShaderColor());
        }

        if (pShaderInstance.FOG_START != null)
        {
            pShaderInstance.FOG_START.set(RenderSystem.getShaderFogStart());
        }

        if (pShaderInstance.FOG_END != null)
        {
            pShaderInstance.FOG_END.set(RenderSystem.getShaderFogEnd());
        }

        if (pShaderInstance.FOG_COLOR != null)
        {
            pShaderInstance.FOG_COLOR.a(RenderSystem.getShaderFogColor());
        }

        if (pShaderInstance.FOG_SHAPE != null)
        {
            pShaderInstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }

        if (pShaderInstance.TEXTURE_MATRIX != null)
        {
            pShaderInstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }

        if (pShaderInstance.GAME_TIME != null)
        {
            pShaderInstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }

        if (pShaderInstance.SCREEN_SIZE != null)
        {
            Window window = Minecraft.getInstance().getWindow();
            pShaderInstance.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
        }

        if (pShaderInstance.LINE_WIDTH != null && (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP))
        {
            pShaderInstance.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
        }

        RenderSystem.setupShaderLights(pShaderInstance);
        pShaderInstance.apply();
        boolean flag = Config.isShaders() && Shaders.isRenderingWorld;
        boolean flag1 = flag && SVertexBuilder.preDrawArrays(this.format);

        if (flag)
        {
            Shaders.setModelViewMatrix(pModelViewMatrix);
            Shaders.setProjectionMatrix(pProjectionMatrix);
            Shaders.setTextureMatrix(RenderSystem.getTextureMatrix());
            Shaders.setColorModulator(RenderSystem.getShaderColor());
        }

        this.draw();

        if (flag1)
        {
            SVertexBuilder.postDrawArrays();
        }

        pShaderInstance.clear();
    }

    public void close()
    {
        if (this.vertexBufferId >= 0)
        {
            RenderSystem.glDeleteBuffers(this.vertexBufferId);
            this.vertexBufferId = -1;
        }

        if (this.indexBufferId >= 0)
        {
            RenderSystem.glDeleteBuffers(this.indexBufferId);
            this.indexBufferId = -1;
        }

        if (this.arrayObjectId >= 0)
        {
            RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
            this.arrayObjectId = -1;
        }
    }

    public VertexFormat getFormat()
    {
        return this.format;
    }

    public boolean isInvalid()
    {
        if (this.vboRegion != null)
        {
            return false;
        }
        else
        {
            return this.arrayObjectId == -1;
        }
    }

    public void setVboRegion(VboRegion vboRegion)
    {
        if (vboRegion != null)
        {
            this.close();
            this.vboRegion = vboRegion;
            this.vboRange = new VboRange();
        }
    }

    public VboRegion getVboRegion()
    {
        return this.vboRegion;
    }
}
