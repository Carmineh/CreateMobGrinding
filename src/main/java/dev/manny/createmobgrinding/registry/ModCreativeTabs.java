package dev.manny.createmobgrinding.registry;

import dev.manny.createmobgrinding.CreateMobGrinding;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateMobGrinding.MOD_ID);

    public static final Supplier<CreativeModeTab> KINETIC_SPAWNING_TAB = CREATIVE_MODE_TABS.register("kinetic_spawning_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.createmobgrinding.kinetic_spawning_tab"))
                    .icon(() -> new ItemStack(ModItems.MOB_SPAWNER_CHUNK.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BLANK_SPAWNER_CHUNK.get());
                        output.accept(ModItems.BROKEN_SPAWNER_CHUNK.get());
                        output.accept(ModItems.MOB_SPAWNER_CHUNK.get());
                        output.accept(ModItems.ROTATIONAL_MOB_SPAWNER.get());
                        output.accept(ModItems.ROTATIONAL_MOB_GRINDER.get());
                        
                        output.accept(ModItems.IRON_GRINDER_BLADE.get());
                        output.accept(ModItems.BRASS_GRINDER_BLADE.get());
                        output.accept(ModItems.DIAMOND_GRINDER_BLADE.get());
                        output.accept(ModItems.NETHERITE_GRINDER_BLADE.get());
                        
                        output.accept(ModItems.BASIC_SOUL_EXTRACTOR.get());
                        output.accept(ModItems.ADVANCED_SOUL_EXTRACTOR.get());
                        output.accept(ModItems.ELITE_SOUL_EXTRACTOR.get());
                        output.accept(ModItems.FILLED_SOUL_EXTRACTOR.get());
                    })
                    .build());
}


