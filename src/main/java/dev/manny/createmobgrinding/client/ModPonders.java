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
                .addStoryBoard(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "spawner-ponder_3"), PonderScenes::spawnerStacking);

        helper.forComponents(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "rotational_mob_grinder"))
                .addStoryBoard(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "grinder-ponder_1_1"), PonderScenes::grinderBlades);
    }
}
