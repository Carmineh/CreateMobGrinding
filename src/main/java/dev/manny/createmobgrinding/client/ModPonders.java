package dev.manny.createmobgrinding.client;

import dev.manny.createmobgrinding.CreateMobGrinding;
import dev.manny.createmobgrinding.registry.ModBlocks;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class ModPonders implements PonderPlugin {

    @Override
    public String getModId() {
        return CreateMobGrinding.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "rotational_mob_spawner"))
                .addStoryBoard(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "spawner-ponder_1_1"), PonderScenes::spawnerConversion)
                .addStoryBoard(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "spawner-ponder_2"), PonderScenes::spawnerPowering)
                .addStoryBoard(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "spawner-ponder_3"), PonderScenes::spawnerStacking)
                .addStoryBoard(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "spawner-ponder_2"), PonderScenes::spawnerChunkStacking)
                .addStoryBoard(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "spawner-ponder_2"), PonderScenes::spawnerAutomation);

        helper.forComponents(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "spawner_chunk_bag"))
                .addStoryBoard(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "spawner-ponder_2"), PonderScenes::chunkBag);

        // Shown both on the upgrade and on Create's Encased Fan, using Create's own fan schematic
        helper.forComponents(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "fan_filter_upgrade"),
                        ResourceLocation.fromNamespaceAndPath("create", "encased_fan"))
                .addStoryBoard(ResourceLocation.fromNamespaceAndPath("create", "fan/direction"), PonderScenes::fanFilter);

        helper.forComponents(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "rotational_mob_grinder"))
                .addStoryBoard(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "grinder-ponder_1_1"), PonderScenes::grinderBlades)
                .addStoryBoard(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "grinder-ponder_1_1"), PonderScenes::grinderMonitoring);
    }
}
