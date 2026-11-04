package dev.manny.createmobgrinding.event;

import dev.manny.createmobgrinding.CreateMobGrinding;
import dev.manny.createmobgrinding.block.entity.RotationalMobSpawnerBlockEntity;
import dev.manny.createmobgrinding.registry.ModBlocks;
import dev.manny.createmobgrinding.registry.ModDataComponents;
import dev.manny.createmobgrinding.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = CreateMobGrinding.MOD_ID)
public class SpawnerEvents {

    /**
     * Reads the entity stored in a vanilla spawner, or null if it has none.
     */
    private static ResourceLocation getSpawnerEntity(Level level, SpawnerBlockEntity spawnerBE) {
        CompoundTag nbt = spawnerBE.saveWithoutMetadata(level.registryAccess());
        if (!nbt.contains("SpawnData")) return null;
        String id = nbt.getCompound("SpawnData").getCompound("entity").getString("id");
        if (id.isEmpty()) return null;
        return ResourceLocation.tryParse(id);
    }

    private static ItemStack createChunk(ResourceLocation entityLoc) {
        ItemStack chunk = new ItemStack(ModItems.MOB_SPAWNER_CHUNK.get());
        chunk.set(ModDataComponents.SPAWNER_ENTITY.get(), entityLoc);
        return chunk;
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!event.getState().is(Blocks.SPAWNER)) return;

        // Same rules as a normal block drop: survival only, with a pickaxe.
        Player player = event.getPlayer();
        if (player.isCreative() || !player.hasCorrectToolForDrops(event.getState(), level, event.getPos())) return;

        BlockPos pos = event.getPos();
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawnerBE) {
            ResourceLocation entityLoc = getSpawnerEntity(level, spawnerBE);
            if (entityLoc != null) {
                net.minecraft.world.level.block.Block.popResource(level, pos, createChunk(entityLoc));
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (!level.getBlockState(pos).is(Blocks.SPAWNER)) return;
        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(ResourceLocation.fromNamespaceAndPath("create", "brass_casing"))) return;

        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        if (level.isClientSide) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SpawnerBlockEntity spawnerBE)) return;
        ResourceLocation entityLoc = getSpawnerEntity(level, spawnerBE);
        if (entityLoc == null) return;

        level.setBlockAndUpdate(pos, ModBlocks.ROTATIONAL_MOB_SPAWNER.get().defaultBlockState());
        if (level.getBlockEntity(pos) instanceof RotationalMobSpawnerBlockEntity rotationalBE) {
            rotationalBE.inventory.setStackInSlot(0, createChunk(entityLoc));
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }
        level.levelEvent(2001, pos, net.minecraft.world.level.block.Block.getId(Blocks.SPAWNER.defaultBlockState()));

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.minecraft.advancements.AdvancementHolder advancement = serverPlayer.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "craft_spawner_casing"));
            if (advancement != null) {
                net.minecraft.advancements.AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
                if (!progress.isDone()) {
                    for (String criterion : progress.getRemainingCriteria()) {
                        serverPlayer.getAdvancements().award(advancement, criterion);
                    }
                }
            }
        }
    }
}
