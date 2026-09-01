package dev.manny.createmobgrinding;

import com.mojang.logging.LogUtils;
import dev.manny.createmobgrinding.item.crafting.ModIngredientTypes;
import dev.manny.createmobgrinding.registry.ModBlockEntities;
import dev.manny.createmobgrinding.registry.ModBlocks;
import dev.manny.createmobgrinding.registry.ModCreativeTabs;
import dev.manny.createmobgrinding.registry.ModDataComponents;
import dev.manny.createmobgrinding.registry.ModItemAttributes;
import dev.manny.createmobgrinding.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CreateMobGrinding.MOD_ID)
public class CreateMobGrinding {
    public static final String MOD_ID = "createmobgrinding";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateMobGrinding(IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {
        dev.manny.createmobgrinding.config.ModConfigs.register(modContainer);
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModIngredientTypes.register(modEventBus);
        ModItemAttributes.ITEM_ATTRIBUTES.register(modEventBus);
        
        modEventBus.addListener(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent.class, event -> {
            event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ROTATIONAL_MOB_GRINDER.get(),
                (be, side) -> be.vacuumInventory
            );
        });
    }
}
