package dev.manny.createmobgrinding.compat.jade;

import dev.manny.createmobgrinding.block.entity.RotationalMobSpawnerBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import net.minecraft.nbt.CompoundTag;
import snownee.jade.api.IServerDataProvider;
import net.minecraft.world.item.ItemStack;
import dev.manny.createmobgrinding.registry.ModDataComponents;

public enum SpawnerComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof RotationalMobSpawnerBlockEntity spawner) {
            CompoundTag serverData = accessor.getServerData();
            if (serverData.contains("SpawnProgress") && serverData.contains("MaxSpawnProgress")) {
                float spawnProgress = serverData.getFloat("SpawnProgress");
                float max = serverData.getFloat("MaxSpawnProgress");
                tooltip.add(Component.translatable("jade.createmobgrinding.spawner.progress", (int) ((spawnProgress / Math.max(1, max)) * 100)));
            }
            if (serverData.contains("SpawnerEntity")) {
                if (serverData.getBoolean("Blacklisted")) {
                    tooltip.add(Component.translatable("tooltip.createmobgrinding.entity_blacklisted").withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD));
                } else {
                    ResourceLocation entityId = ResourceLocation.parse(serverData.getString("SpawnerEntity"));
                    tooltip.add(Component.translatable("tooltip.createmobgrinding.mob_chunk.entity_type", Component.translatable(net.minecraft.Util.makeDescriptionId("entity", entityId))).withStyle(net.minecraft.ChatFormatting.GRAY));
                }
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof RotationalMobSpawnerBlockEntity spawnerBE) {
            tag.putFloat("SpawnProgress", spawnerBE.getSpawnProgress());
            
            ItemStack chunk = spawnerBE.inventory.getStackInSlot(0);
            if (!chunk.isEmpty()) {
                ResourceLocation entityId = chunk.get(ModDataComponents.SPAWNER_ENTITY.get());
                if (entityId != null) {
                    tag.putString("SpawnerEntity", entityId.toString());
                    java.util.List<? extends String> blacklist = dev.manny.createmobgrinding.config.ModConfigs.COMMON.spawnerBlacklist.get();
                    if (blacklist.contains(entityId.toString())) {
                        tag.putBoolean("Blacklisted", true);
                    }
                }
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath("createmobgrinding", "spawner_provider");
    }
}


