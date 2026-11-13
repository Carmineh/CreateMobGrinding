package dev.manny.createmobgrinding.client;

import dev.manny.createmobgrinding.CreateMobGrinding;
import dev.manny.createmobgrinding.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = CreateMobGrinding.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "block/rotational_mob_grinder_blade"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "block/rotational_mob_grinder_blade_brass"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "block/rotational_mob_grinder_blade_diamond"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "block/rotational_mob_grinder_blade_netherite"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "block/rotational_mob_grinder_blade_creative"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "block/rotational_mob_spawner_shaft"), "standalone"));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ROTATIONAL_MOB_SPAWNER.get(), RotationalMobSpawnerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ROTATIONAL_MOB_GRINDER.get(), RotationalMobGrinderRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(dev.manny.createmobgrinding.registry.ModMenuTypes.SPAWNER_CHUNK_BAG.get(), SpawnerChunkBagScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        net.createmod.ponder.foundation.PonderIndex.addPlugin(new ModPonders());
        event.enqueueWork(ModClientEvents::registerItemProperties);
    }

    // Picks the grinder item model matching the blade stored on it (see the overrides in its item model)
    private static void registerItemProperties() {
        net.minecraft.client.renderer.item.ItemProperties.register(
                dev.manny.createmobgrinding.registry.ModItems.ROTATIONAL_MOB_GRINDER.get(),
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "blade_tier"),
                (stack, level, entity, seed) -> {
                    net.minecraft.world.item.ItemStack blade = dev.manny.createmobgrinding.registry.ModDataComponents.getInstalledBlade(stack);
                    if (blade.isEmpty()) return 0.0F;
                    if (blade.is(dev.manny.createmobgrinding.registry.ModItems.BRASS_GRINDER_BLADE.get())) return 0.25F;
                    if (blade.is(dev.manny.createmobgrinding.registry.ModItems.DIAMOND_GRINDER_BLADE.get())) return 0.5F;
                    if (blade.is(dev.manny.createmobgrinding.registry.ModItems.NETHERITE_GRINDER_BLADE.get())) return 0.75F;
                    if (blade.is(dev.manny.createmobgrinding.registry.ModItems.CREATIVE_GRINDER_BLADE.get())) return 1.0F;
                    return 0.0F;
                });
    }
}
