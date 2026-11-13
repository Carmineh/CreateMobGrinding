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

    // Blade installed in a Rotational Mob Grinder, kept on the item when the grinder is broken.
    // Components must be immutable, so the blade is wrapped in ItemContainerContents (like shulker boxes).
    public static final Supplier<DataComponentType<net.minecraft.world.item.component.ItemContainerContents>> INSTALLED_BLADE = COMPONENTS.register("installed_blade",
            () -> DataComponentType.<net.minecraft.world.item.component.ItemContainerContents>builder()
                    .persistent(net.minecraft.world.item.component.ItemContainerContents.CODEC)
                    .networkSynchronized(net.minecraft.world.item.component.ItemContainerContents.STREAM_CODEC)
                    .build()
    );

    public static net.minecraft.world.item.ItemStack getInstalledBlade(net.minecraft.world.item.ItemStack grinder) {
        net.minecraft.world.item.component.ItemContainerContents contents = grinder.get(INSTALLED_BLADE.get());
        return contents == null ? net.minecraft.world.item.ItemStack.EMPTY : contents.copyOne();
    }

    public static net.minecraft.world.item.component.ItemContainerContents bladeContents(net.minecraft.world.item.ItemStack blade) {
        return net.minecraft.world.item.component.ItemContainerContents.fromItems(java.util.List.of(blade.copy()));
    }
}

