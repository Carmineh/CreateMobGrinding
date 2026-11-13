package dev.manny.createmobgrinding.registry;

import dev.manny.createmobgrinding.CreateMobGrinding;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, CreateMobGrinding.MOD_ID);

    public static final Supplier<DataComponentType<ResourceLocation>> SPAWNER_ENTITY = COMPONENTS.register("spawner_entity",
            () -> DataComponentType.<ResourceLocation>builder()
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
                    .build()
    );

    // Blade installed in a Rotational Mob Grinder, kept on the item when the grinder is broken
    public static final Supplier<DataComponentType<net.minecraft.world.item.ItemStack>> INSTALLED_BLADE = COMPONENTS.register("installed_blade",
            () -> DataComponentType.<net.minecraft.world.item.ItemStack>builder()
                    .persistent(net.minecraft.world.item.ItemStack.CODEC)
                    .networkSynchronized(net.minecraft.world.item.ItemStack.STREAM_CODEC)
                    .build()
    );
}

