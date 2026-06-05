package dev.manny.createmobgrinding.block.entity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.ItemStackHandler;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.network.chat.Component;
import java.util.List;

public class RotationalMobSpawnerBlockEntity extends KineticBlockEntity {

    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            updateSpeed = true;
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    private float spawnProgress = 0;

    // Client-side rendering cache
    private Entity renderEntity = null;

    public RotationalMobSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(dev.manny.createmobgrinding.registry.ModBlockEntities.ROTATIONAL_MOB_SPAWNER.get(), pos, state);
    }

    public float getSpawnProgress() {
        return spawnProgress;
    }

    public Entity getRenderEntity() {
        if (level == null || !level.isClientSide) return null;
        ItemStack spawnerChunk = inventory.getStackInSlot(0);
        if (spawnerChunk.isEmpty()) {
            renderEntity = null;
            return null;
        }
        ResourceLocation entityLoc = spawnerChunk.get(dev.manny.createmobgrinding.registry.ModDataComponents.SPAWNER_ENTITY.get());
        if (entityLoc == null) {
            renderEntity = null;
            return null;
        }
        
        if (renderEntity == null || !BuiltInRegistries.ENTITY_TYPE.getKey(renderEntity.getType()).equals(entityLoc)) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityLoc);
            if (type != null) {
                renderEntity = type.create(level);
            } else {
                renderEntity = null;
            }
        }
        return renderEntity;
    }

    public int getTier() {
        ItemStack chunk = inventory.getStackInSlot(0);
        if (chunk.isEmpty()) return 1;
        ResourceLocation entityLoc = chunk.get(dev.manny.createmobgrinding.registry.ModDataComponents.SPAWNER_ENTITY.get());
        if (entityLoc == null) return 1;
        
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityLoc);
        if (type == null) return 1;
        
        if (type.is(dev.manny.createmobgrinding.registry.ModTags.TIER_5)) return 5;
        if (type.is(dev.manny.createmobgrinding.registry.ModTags.TIER_4)) return 4;
        if (type.is(dev.manny.createmobgrinding.registry.ModTags.TIER_3)) return 3;
        if (type.is(dev.manny.createmobgrinding.registry.ModTags.TIER_2)) return 2;
        return 1;
    }

    @Override
    public float calculateStressApplied() {
        float baseImpact = 32.0f; // Base impact for spawner
        float calculated = baseImpact * getTier();
        this.lastStressApplied = calculated;
        return calculated;
    }

    @Override
    public boolean addToGoggleTooltip(java.util.List<net.minecraft.network.chat.Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        
        float stress = calculateStressApplied();
        tooltip.add(net.minecraft.network.chat.Component.empty());
        tooltip.add(net.minecraft.network.chat.Component.literal("    ").append(net.minecraft.network.chat.Component.translatable("gui.create.tooltip.stressImpact")).withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(net.minecraft.network.chat.Component.literal("      " + String.format("%.1f", stress) + "su ")
            .withStyle(net.minecraft.ChatFormatting.AQUA)
            .append(net.minecraft.network.chat.Component.literal("(Tier " + getTier() + ")").withStyle(net.minecraft.ChatFormatting.DARK_GRAY)));
            
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) return;

        float speed = Math.abs(getSpeed());
        if (speed == 0) return;

        ItemStack spawnerChunk = inventory.getStackInSlot(0);
        if (spawnerChunk.isEmpty()) return;

        ResourceLocation entityLoc = spawnerChunk.get(dev.manny.createmobgrinding.registry.ModDataComponents.SPAWNER_ENTITY.get());
        if (entityLoc == null) return;
        
        List<? extends String> blacklist = dev.manny.createmobgrinding.config.ModConfigs.COMMON.spawnerBlacklist.get();
        if (blacklist.contains(entityLoc.toString())) return;

        spawnProgress += speed;

        double threshold = dev.manny.createmobgrinding.config.ModConfigs.COMMON.spawnerBaseProgress.get() * getTier();

        if (spawnProgress >= threshold) {
            spawnProgress -= threshold;
            spawnMob(entityLoc);
        }
    }

    private void spawnMob(ResourceLocation entityLoc) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityLoc);
        if (type != null) {
            ServerLevel serverLevel = (ServerLevel) level;
            
            // Basic area check to prevent too many entities
            long count = serverLevel.getEntities(type, new AABB(worldPosition).inflate(4), Entity::isAlive).size();
            if (count >= 16) {
                return; // Cap reached
            }

            // 3x3 around the spawner, excluding the center column
            int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
            int[] dz = {-1, 0, 1, -1, 1, -1, 0, 1};
            int r = level.random.nextInt(8);
            
            double x = worldPosition.getX() + dx[r] + 0.5D;
            double y = worldPosition.getY() + 1.0D; // Spawna sempre sopra il blocco
            double z = worldPosition.getZ() + dz[r] + 0.5D;

            Entity entity = type.create(serverLevel);
            if (entity instanceof Mob mob) {
                mob.moveTo(x, y, z, level.random.nextFloat() * 360F, 0.0F);
                if (!serverLevel.noCollision(mob)) {
                    mob.discard(); // Annulla lo spawn se è compenetrato
                    return;
                }
                mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(worldPosition), MobSpawnType.SPAWNER, null);
                serverLevel.addFreshEntityWithPassengers(entity);
                serverLevel.levelEvent(2004, worldPosition, 0); // Spawn particles
            }
        }
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("Inventory")) {
            inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
        }
        spawnProgress = compound.getFloat("SpawnProgress");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        
        ItemStack chunk = inventory.getStackInSlot(0);
        if (chunk.isEmpty()) {
            tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.createmobgrinding.spawner.no_chunk")).withStyle(net.minecraft.ChatFormatting.RED));
            return true;
        } else {
            ResourceLocation entityLoc = chunk.get(dev.manny.createmobgrinding.registry.ModDataComponents.SPAWNER_ENTITY.get());
            if (entityLoc != null) {
                java.util.List<? extends String> blacklist = dev.manny.createmobgrinding.config.ModConfigs.COMMON.spawnerBlacklist.get();
                if (blacklist.contains(entityLoc.toString())) {
                    tooltip.add(Component.literal("    ").append(Component.literal("Entity: BLACKLISTED").withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD)));
                    return true;
                }

                tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.createmobgrinding.mob_chunk.entity_type", Component.translatable(net.minecraft.Util.makeDescriptionId("entity", entityLoc)))).withStyle(net.minecraft.ChatFormatting.GRAY));
                double threshold = dev.manny.createmobgrinding.config.ModConfigs.COMMON.spawnerBaseProgress.get() * getTier();
                int percentage = (int) ((spawnProgress / threshold) * 100);
                tooltip.add(Component.literal("    ").append(Component.translatable("jade.createmobgrinding.spawner.progress", percentage)).withStyle(net.minecraft.ChatFormatting.YELLOW));
                return true;
            } else {
                tooltip.add(Component.literal("    ").append(Component.literal("Invalid/Empty Chunk")).withStyle(net.minecraft.ChatFormatting.RED));
                return true;
            }
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider provider, boolean clientPacket) {
        super.write(compound, provider, clientPacket);
        compound.put("Inventory", inventory.serializeNBT(provider));
        compound.putFloat("SpawnProgress", spawnProgress);
        compound.putFloat("MaxSpawnProgress", (float)(dev.manny.createmobgrinding.config.ModConfigs.COMMON.spawnerBaseProgress.get() * getTier()));
    }
}
