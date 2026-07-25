package dev.manny.createmobgrinding.events;

import dev.manny.createmobgrinding.CreateMobGrinding;
import dev.manny.createmobgrinding.block.entity.RotationalMobSpawnerBlockEntity;
import dev.manny.createmobgrinding.registry.ModBlocks;
import dev.manny.createmobgrinding.registry.ModDataComponents;
import dev.manny.createmobgrinding.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
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
public class ModEvents {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        
        BlockPos pos = event.getPos();
        if (event.getState().is(Blocks.SPAWNER)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SpawnerBlockEntity spawnerBE) {
                net.minecraft.nbt.CompoundTag nbt = spawnerBE.saveWithoutMetadata(level.registryAccess());
                if (nbt.contains("SpawnData")) {
                    String id = nbt.getCompound("SpawnData").getCompound("entity").getString("id");
                    ResourceLocation entityLoc = ResourceLocation.parse(id);
                    if (entityLoc != null && !entityLoc.getPath().equals("pig")) { // Don't drop default pig empty spawners unless intended
                        ItemStack chunk = new ItemStack(ModItems.MOB_SPAWNER_CHUNK.get());
                        chunk.set(ModDataComponents.SPAWNER_ENTITY.get(), entityLoc);
                        net.minecraft.world.level.block.Block.popResource(level, pos, chunk);
                    } else if (entityLoc != null) {
                         ItemStack chunk = new ItemStack(ModItems.MOB_SPAWNER_CHUNK.get());
                         chunk.set(ModDataComponents.SPAWNER_ENTITY.get(), entityLoc);
                         net.minecraft.world.level.block.Block.popResource(level, pos, chunk);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (level.getBlockState(pos).is(Blocks.SPAWNER)) {
            // Check if holding Brass Casing
            if (BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(ResourceLocation.fromNamespaceAndPath("create", "brass_casing"))) {
                event.setCanceled(true);
                if (!level.isClientSide) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof SpawnerBlockEntity spawnerBE) {
                        net.minecraft.nbt.CompoundTag nbt = spawnerBE.saveWithoutMetadata(level.registryAccess());
                        if (nbt.contains("SpawnData")) {
                            String id = nbt.getCompound("SpawnData").getCompound("entity").getString("id");
                            ResourceLocation entityLoc = ResourceLocation.parse(id);
                            
                            level.setBlockAndUpdate(pos, ModBlocks.ROTATIONAL_MOB_SPAWNER.get().defaultBlockState());
                            BlockEntity newBE = level.getBlockEntity(pos);
                            if (newBE instanceof RotationalMobSpawnerBlockEntity rotationalBE) {
                                ItemStack chunk = new ItemStack(ModItems.MOB_SPAWNER_CHUNK.get());
                                chunk.set(ModDataComponents.SPAWNER_ENTITY.get(), entityLoc);
                                rotationalBE.inventory.setStackInSlot(0, chunk);
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
                }
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            }
        }
    }
}
