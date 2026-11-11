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
            updateComparator();
        }

        // Chunks of the same mob stack up to the configured limit (funnels and arms respect it too)
        @Override
        public int getSlotLimit(int slot) {
            return dev.manny.createmobgrinding.config.ModConfigs.COMMON.maxStackedChunks.get();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(dev.manny.createmobgrinding.registry.ModItems.MOB_SPAWNER_CHUNK.get());
        }
    };

    public final ItemStackHandler upgrades = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    private float spawnProgress = 0;
    private int lastComparatorSignal = 0;
    public boolean disabledByRedstone = false;
    public float visualSpin = 0f;
    public float oVisualSpin = 0f;

    // Client-side rendering cache
    private Entity renderEntity = null;

    public RotationalMobSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(dev.manny.createmobgrinding.registry.ModBlockEntities.ROTATIONAL_MOB_SPAWNER.get(), pos, state);
    }

    public float getSpawnProgress() {
        return spawnProgress;
    }

    /**
     * Comparator output: 0 without a chunk, otherwise 1-15 following the progress towards the next spawn.
     */
    public int getComparatorSignal() {
        if (inventory.getStackInSlot(0).isEmpty()) return 0;
        double fraction = Math.min(1.0, spawnProgress / getSpawnThreshold());
        return 1 + (int) (fraction * 14);
    }

    private void updateComparator() {
        if (level == null || level.isClientSide) return;
        int signal = getComparatorSignal();
        if (signal != lastComparatorSignal) {
            lastComparatorSignal = signal;
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
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
        return dev.manny.createmobgrinding.util.MobTierHelper.getMobTier(type);
    }

    /**
     * Chunks that count towards the stacking bonuses. A stack bigger than the limit (the config was lowered)
     * still works, but only up to the limit.
     */
    public int getChunkCount() {
        return Math.min(inventory.getStackInSlot(0).getCount(), dev.manny.createmobgrinding.config.ModConfigs.COMMON.maxStackedChunks.get());
    }

    public double getSpeedMultiplier() {
        int extra = Math.max(0, getChunkCount() - 1);
        return 1.0 + extra * dev.manny.createmobgrinding.config.ModConfigs.COMMON.speedBonusPerChunk.get();
    }

    public int getMobsPerSpawn() {
        int chunks = getChunkCount();
        int mobs = 1;
        for (int threshold : dev.manny.createmobgrinding.config.ModConfigs.COMMON.extraMobThresholds.get()) {
            if (chunks >= threshold) mobs++;
        }
        return mobs;
    }

    @Override
    public float calculateStressApplied() {
        var config = dev.manny.createmobgrinding.config.ModConfigs.COMMON;
        int extraChunks = Math.max(0, getChunkCount() - 1);
        float calculated = (float) (config.spawnerBaseStress.get() * config.getStressMultiplier(getTier())
                * (1.0 + extraChunks * config.stressPerExtraChunk.get()));
        this.lastStressApplied = calculated;
        return calculated;
    }

    public double getSpawnThreshold() {
        return dev.manny.createmobgrinding.config.ModConfigs.COMMON.getSpawnTime(getTier());
    }

    @Override
    public void tick() {
        super.tick();

        if (level != null && level.isClientSide) {
            oVisualSpin = visualSpin;
            if (!disabledByRedstone) {
                float speed = Math.abs(getSpeed());
                if (speed > 0) {
                    visualSpin += (speed / 10f);
                }
            }
            return;
        }

        if (level == null) return;
        
        // Redstone Control
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (!powered) {
            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(worldPosition.relative(dir));
                // A lit redstone torch attached to the spawner doesn't power it in vanilla, but it still turns the spawner off.
                if (state.getBlock() instanceof net.minecraft.world.level.block.RedstoneTorchBlock
                        && state.getValue(net.minecraft.world.level.block.RedstoneTorchBlock.LIT)) {
                    powered = true;
                    break;
                }
            }
        }
        
        if (powered != this.disabledByRedstone) {
            this.disabledByRedstone = powered;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        if (this.disabledByRedstone) return;

        float speed = Math.abs(getSpeed());
        if (speed == 0) return;

        ItemStack spawnerChunk = inventory.getStackInSlot(0);
        if (spawnerChunk.isEmpty()) return;

        ResourceLocation entityLoc = spawnerChunk.get(dev.manny.createmobgrinding.registry.ModDataComponents.SPAWNER_ENTITY.get());
        if (entityLoc == null) return;
        
        List<? extends String> blacklist = dev.manny.createmobgrinding.config.ModConfigs.COMMON.spawnerBlacklist.get();
        if (blacklist.contains(entityLoc.toString())) return;

        spawnProgress += (float) (speed * getSpeedMultiplier());

        double threshold = getSpawnThreshold();

        if (spawnProgress >= threshold) {
            spawnProgress -= threshold;
            for (int i = getMobsPerSpawn(); i > 0; i--) {
                spawnMob(entityLoc);
            }
        }
        updateComparator();
    }

    private void spawnMob(ResourceLocation entityLoc) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityLoc);
        if (type != null) {
            ServerLevel serverLevel = (ServerLevel) level;
            
            // Basic area check to prevent too many entities
            var config = dev.manny.createmobgrinding.config.ModConfigs.COMMON;
            long count = serverLevel.getEntities(type, new AABB(worldPosition).inflate(config.mobCapRadius.get()), Entity::isAlive).size();
            if (count >= config.maxNearbyMobs.get()) {
                return; // Cap reached
            }

            // 3x3 around the spawner, excluding the center column
            int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
            int[] dz = {-1, 0, 1, -1, 1, -1, 0, 1};
            int r = level.random.nextInt(8);
            
            double x = worldPosition.getX() + dx[r] + 0.5D;
            double y = worldPosition.getY() + 1.0D; // Always spawn above the block
            double z = worldPosition.getZ() + dz[r] + 0.5D;

            Entity entity = type.create(serverLevel);
            if (entity instanceof Mob mob) {
                boolean noAi = false;
                boolean noConditions = false;

                for (int i = 0; i < upgrades.getSlots(); i++) {
                    net.minecraft.world.item.ItemStack upgrade = upgrades.getStackInSlot(i);
                    if (upgrade.is(dev.manny.createmobgrinding.registry.ModItems.SPAWNER_UPGRADE_NO_AI.get())) noAi = true;
                    if (upgrade.is(dev.manny.createmobgrinding.registry.ModItems.SPAWNER_UPGRADE_NO_CONDITIONS.get())) noConditions = true;
                }

                if (!noConditions && getLightError(entity, serverLevel, net.minecraft.core.BlockPos.containing(x, y, z)) != null) {
                    entity.discard();
                    return;
                }

                mob.moveTo(x, y, z, level.random.nextFloat() * 360F, 0.0F);
                if (!noConditions && !serverLevel.noCollision(mob)) {
                    mob.discard(); // Cancel spawn if colliding
                    return;
                }
                
                if (noAi) {
                    mob.goalSelector.removeAllGoals(g -> true);
                    mob.targetSelector.removeAllGoals(g -> true);
                }
                
                mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(worldPosition), MobSpawnType.SPAWNER, null);
                serverLevel.addFreshEntityWithPassengers(entity);
                serverLevel.levelEvent(2004, worldPosition, 0); // Spawn particles
            }
        }
    }

    /**
     * Returns the translation key of the light problem preventing this entity from spawning at pos, or null if light is fine.
     * Shared by the spawn logic and the goggle tooltip so they always agree.
     */
    public static String getLightError(Entity entity, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        int light = level.getMaxLocalRawBrightness(pos);
        if (entity instanceof net.minecraft.world.entity.monster.Monster && light > dev.manny.createmobgrinding.config.ModConfigs.COMMON.monsterMaxLight.get()) {
            return "tooltip.createmobgrinding.spawner.error.too_much_light";
        }
        if (entity instanceof net.minecraft.world.entity.animal.Animal && light < dev.manny.createmobgrinding.config.ModConfigs.COMMON.animalMinLight.get()) {
            return "tooltip.createmobgrinding.spawner.error.too_dark";
        }
        return null;
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("Inventory")) {
            inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
        }
        if (compound.contains("Upgrades")) {
            upgrades.deserializeNBT(registries, compound.getCompound("Upgrades"));
        }
        spawnProgress = compound.getFloat("SpawnProgress");
        disabledByRedstone = compound.getBoolean("DisabledByRedstone");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        
        float stress = calculateStressApplied();
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.createmobgrinding.stress_impact")).withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.literal("      " + String.format("%.1f", stress) + "x ")
            .withStyle(net.minecraft.ChatFormatting.AQUA)
            .append(Component.translatable("tooltip.createmobgrinding.tier", getTier()).withStyle(net.minecraft.ChatFormatting.DARK_GRAY)));
        
        ItemStack chunk = inventory.getStackInSlot(0);
        if (chunk.isEmpty()) {
            tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.createmobgrinding.spawner.no_chunk")).withStyle(net.minecraft.ChatFormatting.RED));
        } else {
            ResourceLocation entityLoc = chunk.get(dev.manny.createmobgrinding.registry.ModDataComponents.SPAWNER_ENTITY.get());
            if (entityLoc != null) {
                java.util.List<? extends String> blacklist = dev.manny.createmobgrinding.config.ModConfigs.COMMON.spawnerBlacklist.get();
                if (blacklist.contains(entityLoc.toString())) {
                    tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.createmobgrinding.entity_blacklisted").withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD)));
                } else {
                    tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.createmobgrinding.mob_chunk.entity_type", Component.translatable(net.minecraft.Util.makeDescriptionId("entity", entityLoc)))).withStyle(net.minecraft.ChatFormatting.GRAY));
                    
                    boolean noConditions = false;
                    for (int i = 0; i < upgrades.getSlots(); i++) {
                        net.minecraft.world.item.ItemStack upgrade = upgrades.getStackInSlot(i);
                        if (!upgrade.isEmpty() && upgrade.is(dev.manny.createmobgrinding.registry.ModItems.SPAWNER_UPGRADE_NO_CONDITIONS.get())) {
                            noConditions = true;
                            break;
                        }
                    }

                    Entity previewEntity = getRenderEntity();
                    if (!noConditions && previewEntity != null) {
                        String lightError = getLightError(previewEntity, level, worldPosition.above());
                        if (lightError != null) {
                            tooltip.add(Component.literal("    ").append(Component.translatable(lightError)).withStyle(net.minecraft.ChatFormatting.RED));
                        }
                    }

                    double threshold = getSpawnThreshold();
                    int percentage = (int) ((spawnProgress / threshold) * 100);
                    tooltip.add(Component.literal("    ").append(Component.translatable("jade.createmobgrinding.spawner.progress", percentage)).withStyle(net.minecraft.ChatFormatting.YELLOW));
                    tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.createmobgrinding.spawner.chunks",
                            chunk.getCount(), dev.manny.createmobgrinding.config.ModConfigs.COMMON.maxStackedChunks.get(),
                            String.format("%.0f", (getSpeedMultiplier() - 1) * 100), getMobsPerSpawn())).withStyle(net.minecraft.ChatFormatting.GRAY));
                }
            } else {
                tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.createmobgrinding.spawner.invalid_chunk")).withStyle(net.minecraft.ChatFormatting.RED));
            }
        }

        boolean hasUpgrades = false;
        for (int i = 0; i < upgrades.getSlots(); i++) {
            if (!upgrades.getStackInSlot(i).isEmpty()) {
                hasUpgrades = true;
                break;
            }
        }
        if (hasUpgrades) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.createmobgrinding.spawner.upgrades")).withStyle(net.minecraft.ChatFormatting.GRAY));
            for (int i = 0; i < upgrades.getSlots(); i++) {
                net.minecraft.world.item.ItemStack upgrade = upgrades.getStackInSlot(i);
                if (!upgrade.isEmpty()) {
                    tooltip.add(Component.literal("      - ").withStyle(net.minecraft.ChatFormatting.DARK_GRAY).append(upgrade.getHoverName().copy().withStyle(net.minecraft.ChatFormatting.GREEN)));
                }
            }
        }
        return true;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider provider, boolean clientPacket) {
        super.write(compound, provider, clientPacket);
        compound.put("Inventory", inventory.serializeNBT(provider));
        compound.put("Upgrades", upgrades.serializeNBT(provider));
        compound.putFloat("SpawnProgress", spawnProgress);
        compound.putBoolean("DisabledByRedstone", disabledByRedstone);
        compound.putFloat("MaxSpawnProgress", (float)getSpawnThreshold());
    }
}
