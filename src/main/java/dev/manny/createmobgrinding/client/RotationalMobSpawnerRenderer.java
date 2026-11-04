package dev.manny.createmobgrinding.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.manny.createmobgrinding.block.entity.RotationalMobSpawnerBlockEntity;
import net.minecraft.client.Minecraft;
import dev.manny.createmobgrinding.CreateMobGrinding;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;

public class RotationalMobSpawnerRenderer extends KineticBlockEntityRenderer<RotationalMobSpawnerBlockEntity> {

    private static final ModelResourceLocation SHAFT_MODEL = new ModelResourceLocation(
        ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "block/rotational_mob_spawner_shaft"), "standalone");

    public RotationalMobSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(RotationalMobSpawnerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderShaft(be, partialTicks, ms, buffer, light, overlay);

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

    // Shaft stubs on the top and bottom faces, spinning with the kinetic network like the grinder blade
    private void renderShaft(RotationalMobSpawnerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BakedModel shaftModel = Minecraft.getInstance().getModelManager().getModel(SHAFT_MODEL);

        float time = be.getLevel().getGameTime() + partialTicks;
        float offset = getRotationOffsetForPosition(be, be.getBlockPos(), Direction.Axis.Y);
        float angle = ((time * be.getSpeed() * 3f / 10f) + offset) % 360;

        ms.pushPose();
        ms.translate(0.5D, 0.5D, 0.5D);
        ms.mulPose(Axis.YP.rotationDegrees(angle));
        ms.translate(-0.5D, -0.5D, -0.5D);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
            ms.last(), buffer.getBuffer(RenderType.solid()), be.getBlockState(), shaftModel, 1.0F, 1.0F, 1.0F, light, overlay
        );
        ms.popPose();
    }
}

