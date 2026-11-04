package dev.manny.createmobgrinding.registry;

import dev.manny.createmobgrinding.CreateMobGrinding;
import dev.manny.createmobgrinding.menu.SpawnerChunkBagMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, CreateMobGrinding.MOD_ID);

    public static final Supplier<MenuType<SpawnerChunkBagMenu>> SPAWNER_CHUNK_BAG = MENU_TYPES.register("spawner_chunk_bag",
            () -> IMenuTypeExtension.create(SpawnerChunkBagMenu::fromNetwork));
}
