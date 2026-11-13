package dev.manny.createmobgrinding.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.manny.createmobgrinding.CreateMobGrinding;
import dev.manny.createmobgrinding.block.entity.RotationalMobGrinderBlockEntity;
import dev.manny.createmobgrinding.block.entity.RotationalMobSpawnerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

@EventBusSubscriber(modid = CreateMobGrinding.MOD_ID, value = Dist.CLIENT)
public class GhostBoxRenderer {

    @SubscribeEvent
    public static void onRenderHighlight(RenderHighlightEvent.Block event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        boolean mainHandWrench = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).getPath().contains("wrench");
        boolean offHandWrench = BuiltInRegistries.ITEM.getKey(player.getOffhandItem().getItem()).getPath().contains("wrench");

        if (!mainHandWrench && !offHandWrench) return;

        BlockHitResult hitResult = event.getTarget();
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hitResult.getBlockPos();
            BlockEntity be = player.level().getBlockEntity(pos);

            AABB boxToDraw = null;
            if (be instanceof RotationalMobSpawnerBlockEntity) {
                boxToDraw = RotationalMobSpawnerBlockEntity.getSpawnArea(pos);
            } else if (be instanceof RotationalMobGrinderBlockEntity) {
                net.minecraft.core.Direction facing = be.getBlockState().hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING) ? 
                        be.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING) : 
                        net.minecraft.core.Direction.NORTH;
                boxToDraw = new AABB(pos.relative(facing));
            }

            if (boxToDraw != null) {
                PoseStack poseStack = event.getPoseStack();
                double camX = event.getCamera().getPosition().x;
                double camY = event.getCamera().getPosition().y;
                double camZ = event.getCamera().getPosition().z;

                AABB offsetBox = boxToDraw.move(-camX, -camY, -camZ);
                
                VertexConsumer vertexConsumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
                // ARGB for NeoForge 1.21.1: alpha, red, green, blue (or the 4 float variant)
                // renderLineBox in 1.21 takes floats (AABB, red, green, blue, alpha)
                LevelRenderer.renderLineBox(poseStack, vertexConsumer, offsetBox, 1.0F, 0.2F, 0.2F, 1.0F);
            }
        }
    }
}

