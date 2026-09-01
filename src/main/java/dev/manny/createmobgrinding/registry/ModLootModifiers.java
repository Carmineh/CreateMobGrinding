package dev.manny.createmobgrinding.registry;

import com.mojang.serialization.MapCodec;
import dev.manny.createmobgrinding.CreateMobGrinding;
import dev.manny.createmobgrinding.loot.AddItemModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = 
        DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CreateMobGrinding.MOD_ID);

    public static final Supplier<MapCodec<AddItemModifier>> ADD_ITEM = 
        LOOT_MODIFIER_SERIALIZERS.register("add_item", () -> AddItemModifier.CODEC);
}
