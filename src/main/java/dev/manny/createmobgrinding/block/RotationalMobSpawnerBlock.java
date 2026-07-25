package dev.manny.createmobgrinding.block;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.manny.createmobgrinding.block.entity.RotationalMobSpawnerBlockEntity;
import dev.manny.createmobgrinding.registry.ModBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelReader;

public class RotationalMobSpawnerBlock extends KineticBlock implements IBE<RotationalMobSpawnerBlockEntity> {

    public RotationalMobSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, net.minecraft.core.BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN || face == Direction.UP;
    }

    @Override
    public boolean hideStressImpact() {
        return true;
    }

    @Override
    public Class<RotationalMobSpawnerBlockEntity> getBlockEntityClass() {
        return RotationalMobSpawnerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RotationalMobSpawnerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ROTATIONAL_MOB_SPAWNER.get();
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (stack.is(dev.manny.createmobgrinding.registry.ModItems.MOB_SPAWNER_CHUNK.get())) {
            if (!level.isClientSide) {
                net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof RotationalMobSpawnerBlockEntity spawnerBE) {
                    net.minecraft.world.item.ItemStack current = spawnerBE.inventory.getStackInSlot(0);

                    net.minecraft.resources.ResourceLocation entityLoc = stack.get(dev.manny.createmobgrinding.registry.ModDataComponents.SPAWNER_ENTITY.get());
                    if (entityLoc != null) {
                        java.util.List<? extends String> blacklist = dev.manny.createmobgrinding.config.ModConfigs.COMMON.spawnerBlacklist.get();
                        if (blacklist.contains(entityLoc.toString())) {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Mob Blacklisted").withStyle(net.minecraft.ChatFormatting.RED), true);
                            return net.minecraft.world.ItemInteractionResult.SUCCESS;
                        }
                    }

                    if (!current.isEmpty()) {
                        net.neoforged.neoforge.items.ItemHandlerHelper.giveItemToPlayer(player, current.copy());
                    }

                    spawnerBE.inventory.setStackInSlot(0, stack.copyWithCount(1));
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    return net.minecraft.world.ItemInteractionResult.SUCCESS;
                }
            }
            return net.minecraft.world.ItemInteractionResult.SUCCESS;
        } else if (stack.is(dev.manny.createmobgrinding.registry.ModItems.SPAWNER_UPGRADE_NO_AI.get()) ||
                   stack.is(dev.manny.createmobgrinding.registry.ModItems.SPAWNER_UPGRADE_NO_CONDITIONS.get())) {
            if (!level.isClientSide) {
                net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof RotationalMobSpawnerBlockEntity spawnerBE) {
                    boolean alreadyPresent = false;
                    int firstEmpty = -1;
                    for (int i = 0; i < spawnerBE.upgrades.getSlots(); i++) {
                        net.minecraft.world.item.ItemStack current = spawnerBE.upgrades.getStackInSlot(i);
                        if (current.is(stack.getItem())) {
                            alreadyPresent = true;
                            break;
                        }
                        if (current.isEmpty() && firstEmpty == -1) {
                            firstEmpty = i;
                        }
                    }
                    if (!alreadyPresent && firstEmpty != -1) {
                        spawnerBE.upgrades.setStackInSlot(firstEmpty, stack.copyWithCount(1));
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                    }
                }
            }
            return net.minecraft.world.ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public net.minecraft.world.InteractionResult onWrenched(BlockState state, net.minecraft.world.item.context.UseOnContext context) {
        if (context.getPlayer() != null && !context.getPlayer().isShiftKeyDown()) {
            net.minecraft.world.level.block.entity.BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
            if (be instanceof RotationalMobSpawnerBlockEntity spawnerBE) {
                net.minecraft.world.item.ItemStack chunk = spawnerBE.inventory.getStackInSlot(0);
                if (!chunk.isEmpty()) {
                    if (!context.getLevel().isClientSide) {
                        net.neoforged.neoforge.items.ItemHandlerHelper.giveItemToPlayer(context.getPlayer(), chunk.copy());
                        spawnerBE.inventory.setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                } else {
                    for (int i = spawnerBE.upgrades.getSlots() - 1; i >= 0; i--) {
                        net.minecraft.world.item.ItemStack upgrade = spawnerBE.upgrades.getStackInSlot(i);
                        if (!upgrade.isEmpty()) {
                            if (!context.getLevel().isClientSide) {
                                net.neoforged.neoforge.items.ItemHandlerHelper.giveItemToPlayer(context.getPlayer(), upgrade.copy());
                                spawnerBE.upgrades.setStackInSlot(i, net.minecraft.world.item.ItemStack.EMPTY);
                            }
                            return net.minecraft.world.InteractionResult.SUCCESS;
                        }
                    }
                }
            }
        }
        return super.onWrenched(state, context);
    }

    @Override
    public net.minecraft.world.InteractionResult onSneakWrenched(BlockState state, net.minecraft.world.item.context.UseOnContext context) {
        net.minecraft.world.level.Level level = context.getLevel();
        net.minecraft.core.BlockPos pos = context.getClickedPos();
        if (!level.isClientSide) {
            level.destroyBlock(pos, false);
            net.minecraft.world.level.block.Block.popResource(level, pos, new net.minecraft.world.item.ItemStack(dev.manny.createmobgrinding.registry.ModBlocks.ROTATIONAL_MOB_SPAWNER.get().asItem()));
        }
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RotationalMobSpawnerBlockEntity spawnerBE) {
                net.minecraft.world.item.ItemStack chunk = spawnerBE.inventory.getStackInSlot(0);
                if (!chunk.isEmpty()) {
                    net.minecraft.world.level.block.Block.popResource(level, pos, chunk);
                }
                for (int i = 0; i < spawnerBE.upgrades.getSlots(); i++) {
                    net.minecraft.world.item.ItemStack upgrade = spawnerBE.upgrades.getStackInSlot(i);
                    if (!upgrade.isEmpty()) {
                        net.minecraft.world.level.block.Block.popResource(level, pos, upgrade);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}

