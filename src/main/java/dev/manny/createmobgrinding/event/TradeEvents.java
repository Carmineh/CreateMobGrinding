package dev.manny.createmobgrinding.event;

import dev.manny.createmobgrinding.CreateMobGrinding;
import dev.manny.createmobgrinding.config.ModConfigs;
import dev.manny.createmobgrinding.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

@EventBusSubscriber(modid = CreateMobGrinding.MOD_ID)
public class TradeEvents {
    private static final int BROKEN_CHUNK_PRICE = 32;
    private static final int BROKEN_CHUNK_USES = 1;
    private static final int TRADER_XP = 1;

    // Rare pool: the trader picks only one rare offer per visit, so the chunk shows up now and then
    @SubscribeEvent
    public static void onWandererTrades(WandererTradesEvent event) {
        if (!ModConfigs.COMMON.wanderingTraderSellsBrokenChunk.get()) return;

        event.getRareTrades().add((trader, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, BROKEN_CHUNK_PRICE),
                new ItemStack(ModItems.BROKEN_SPAWNER_CHUNK.get()),
                BROKEN_CHUNK_USES, TRADER_XP, 0.05F));
    }
}
