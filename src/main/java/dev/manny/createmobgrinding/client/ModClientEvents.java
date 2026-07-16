package dev.manny.createmobgrinding.client;

import dev.manny.createmobgrinding.CreateMobGrinding;
import dev.manny.createmobgrinding.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = CreateMobGrinding.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "block/rotational_mob_grinder_blade"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "block/rotational_mob_grinder_blade_brass"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "block/rotational_mob_grinder_blade_diamond"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "block/rotational_mob_grinder_blade_netherite"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "block/rotational_mob_grinder_blade_creative"), "standalone"));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ROTATIONAL_MOB_SPAWNER.get(), RotationalMobSpawnerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ROTATIONAL_MOB_GRINDER.get(), RotationalMobGrinderRenderer::new);
    }
}

