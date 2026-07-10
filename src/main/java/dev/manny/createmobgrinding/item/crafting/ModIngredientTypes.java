package dev.manny.createmobgrinding.item.crafting;

import com.mojang.serialization.MapCodec;
import dev.manny.createmobgrinding.CreateMobGrinding;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

public class ModIngredientTypes {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, CreateMobGrinding.MOD_ID);

    public static final Supplier<IngredientType<StrictSyringeIngredient>> STRICT_SYRINGE = INGREDIENT_TYPES.register("strict_syringe", () -> new IngredientType<>(StrictSyringeIngredient.CODEC));

    public static void register(IEventBus eventBus) {
        INGREDIENT_TYPES.register(eventBus);
    }
}
