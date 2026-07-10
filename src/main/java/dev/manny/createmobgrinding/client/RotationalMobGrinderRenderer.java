package dev.manny.createmobgrinding.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.manny.createmobgrinding.CreateMobGrinding;
import dev.manny.createmobgrinding.block.entity.RotationalMobGrinderBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class RotationalMobGrinderRenderer extends KineticBlockEntityRenderer<RotationalMobGrinderBlockEntity> {

    public RotationalMobGrinderRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(RotationalMobGrinderBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {


        String modelName = "block/rotational_mob_grinder_blade";
        net.minecraft.world.item.ItemStack blade = be.getInstalledBlade();
        if (blade.is(dev.manny.createmobgrinding.registry.ModItems.BRASS_GRINDER_BLADE.get())) {
            modelName = "block/rotational_mob_grinder_blade_brass";
        } else if (blade.is(dev.manny.createmobgrinding.registry.ModItems.DIAMOND_GRINDER_BLADE.get())) {
            modelName = "block/rotational_mob_grinder_blade_diamond";
        } else if (blade.is(dev.manny.createmobgrinding.registry.ModItems.NETHERITE_GRINDER_BLADE.get())) {
            modelName = "block/rotational_mob_grinder_blade_netherite";
        }

        BakedModel bladeModel = Minecraft.getInstance().getModelManager().getModel(
            new net.minecraft.client.resources.model.ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, modelName), "standalone")
        );

        ms.pushPose();

        // Determine block orientation
        Direction facing = be.getBlockState().hasProperty(BlockStateProperties.FACING) ? 
                           be.getBlockState().getValue(BlockStateProperties.FACING) : 
                           Direction.NORTH;

        float angle = 0f;
        try {
            Direction.Axis axis = be.getBlockState().hasProperty(BlockStateProperties.FACING) ? 
                                  be.getBlockState().getValue(BlockStateProperties.FACING).getAxis() : 
                                  Direction.Axis.Z;
            float time = be.getLevel().getGameTime() + partialTicks;
            float offset = com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer.getRotationOffsetForPosition(be, be.getBlockPos(), axis);
            angle = ((time * be.getSpeed() * 3f / 10f) + offset) % 360;
        } catch (Exception e) {}

        ms.translate(0.5D, 0.5D, 0.5D);

        // Rotate the entire blade assembly to face the block's front direction
        // Align blade to face the correct direction
        float yRot = 0;
        switch (facing) {
            case NORTH: yRot = 0f; break;
            case EAST:  yRot = 270f; break;
            case SOUTH: yRot = 180f; break;
            case WEST:  yRot = 90f; break;
            default: break;
        }
        if (facing.getAxis().isVertical()) {
            // Handle UP/DOWN
            float xRot = facing == Direction.UP ? 90f : (facing == Direction.DOWN ? -90f : 0f);
            ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(xRot));
        } else {
            ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRot));
        }

        // Spin the blade around the Z axis (which is now the forward axis after Y rotation)
        ms.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));

        ms.translate(-0.5D, -0.5D, -0.5D);

        // Render the BakedModel
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
            ms.last(), buffer.getBuffer(RenderType.cutoutMipped()), be.getBlockState(), bladeModel, 1.0F, 1.0F, 1.0F, light, overlay
        );

        ms.popPose();
    }
}

