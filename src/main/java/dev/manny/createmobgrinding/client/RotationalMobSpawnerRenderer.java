package dev.manny.createmobgrinding.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.manny.createmobgrinding.block.entity.RotationalMobSpawnerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;

public class RotationalMobSpawnerRenderer extends KineticBlockEntityRenderer<RotationalMobSpawnerBlockEntity> {

    public RotationalMobSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(RotationalMobSpawnerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {

        Entity entity = be.getRenderEntity();
        if (entity != null) {
            ms.pushPose();
            ms.translate(0.5D, 0.5D, 0.5D); // Center of the block
            
            float angle = net.minecraft.util.Mth.lerp(partialTicks, be.oVisualSpin, be.visualSpin);
            
            ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
            
            // Scale
            float scale = 0.4375F; // Vanilla spawner scale
            float maxDimension = Math.max(entity.getBbWidth(), entity.getBbHeight());
            if (maxDimension > 1.0F) {
                scale /= maxDimension;
            }
            
            ms.translate(0.0D, -0.4D, 0.0D); // Lower it a bit so it sits in the cage
            ms.scale(scale, scale, scale);

            EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, ms, buffer, light);
            
            ms.popPose();
        }
    }
}

