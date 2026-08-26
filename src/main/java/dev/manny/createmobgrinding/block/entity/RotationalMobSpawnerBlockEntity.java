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

    public final ItemStackHandler upgrades = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    private float spawnProgress = 0;
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
        float calculated = switch (getTier()) {
            case 1 -> baseImpact * 1.0f;  // 32 SU
            case 2 -> baseImpact * 1.5f;  // 48 SU
            case 3 -> baseImpact * 2.0f;  // 64 SU
            case 4 -> baseImpact * 6.0f;  // 192 SU (Quite large)
            case 5 -> baseImpact * 12.0f; // 384 SU (Quite large)
            default -> baseImpact;
        };
        this.lastStressApplied = calculated;
        return calculated;
    }

    public double getSpawnThreshold() {
        double base = dev.manny.createmobgrinding.config.ModConfigs.COMMON.spawnerBaseProgress.get();
        return switch (getTier()) {
            case 1 -> base * 0.5;   // Many mobs (1000)
            case 2 -> base * 0.75;  // (1500)
            case 3 -> base * 1.0;   // (2000)
            case 4 -> base * 2.5;   // PiÃ¹ lento ma non troppo (5000)
            case 5 -> base * 3.5;   // (7000)
            default -> base;
        };
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
                if (state.getBlock() instanceof net.minecraft.world.level.block.RedstoneTorchBlock) {
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

        spawnProgress += speed;

        double threshold = getSpawnThreshold();

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

                if (!noConditions) {
                    net.minecraft.core.BlockPos spawnPos = net.minecraft.core.BlockPos.containing(x, y, z);
                    if (entity instanceof net.minecraft.world.entity.monster.Monster) {
                        if (serverLevel.getMaxLocalRawBrightness(spawnPos) > 7) {
                            entity.discard();
                            return; // Too bright for monsters
                        }
                    } else if (entity instanceof net.minecraft.world.entity.animal.Animal) {
                        if (serverLevel.getMaxLocalRawBrightness(spawnPos) < 9) {
                            entity.discard();
                            return; // Too dark for animals
                        }
                    }
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
            .append(Component.literal("(Tier " + getTier() + ")").withStyle(net.minecraft.ChatFormatting.DARK_GRAY)));
        
        ItemStack chunk = inventory.getStackInSlot(0);
        if (chunk.isEmpty()) {
            tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.createmobgrinding.spawner.no_chunk")).withStyle(net.minecraft.ChatFormatting.RED));
        } else {
            ResourceLocation entityLoc = chunk.get(dev.manny.createmobgrinding.registry.ModDataComponents.SPAWNER_ENTITY.get());
            if (entityLoc != null) {
                java.util.List<? extends String> blacklist = dev.manny.createmobgrinding.config.ModConfigs.COMMON.spawnerBlacklist.get();
                if (blacklist.contains(entityLoc.toString())) {
                    tooltip.add(Component.literal("    ").append(Component.literal("Entity: BLACKLISTED").withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD)));
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

                    if (!noConditions) {
                        net.minecraft.world.entity.EntityType<?> type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(entityLoc);
                        if (type != null) {
                            net.minecraft.core.BlockPos spawnPos = worldPosition.above();
                            if (type.getCategory() == net.minecraft.world.entity.MobCategory.MONSTER) {
                                if (level.getMaxLocalRawBrightness(spawnPos) > 7) {
                                    tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.createmobgrinding.spawner.error.too_much_light")).withStyle(net.minecraft.ChatFormatting.RED));
                                }
                            } else if (type.getCategory() == net.minecraft.world.entity.MobCategory.CREATURE) {
                                if (level.getMaxLocalRawBrightness(spawnPos) < 9) {
                                    tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.createmobgrinding.spawner.error.too_dark")).withStyle(net.minecraft.ChatFormatting.RED));
                                }
                            }
                        }
                    }

                    double threshold = getSpawnThreshold();
                    int percentage = (int) ((spawnProgress / threshold) * 100);
                    tooltip.add(Component.literal("    ").append(Component.translatable("jade.createmobgrinding.spawner.progress", percentage)).withStyle(net.minecraft.ChatFormatting.YELLOW));
                }
            } else {
                tooltip.add(Component.literal("    ").append(Component.literal("Invalid/Empty Chunk")).withStyle(net.minecraft.ChatFormatting.RED));
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
