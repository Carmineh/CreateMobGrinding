package dev.manny.createmobgrinding.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.manny.createmobgrinding.registry.ModDataComponents;
import dev.manny.createmobgrinding.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

public class StrictSyringeIngredient implements ICustomIngredient {

    private final ResourceLocation entityId;

    public StrictSyringeIngredient(ResourceLocation entityId) {
        this.entityId = entityId;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.is(ModItems.FILLED_SOUL_EXTRACTOR.get())) return false;
        ResourceLocation stored = stack.get(ModDataComponents.SPAWNER_ENTITY.get());
        return entityId.equals(stored);
    }

    @Override
    public Stream<ItemStack> getItems() {
        ItemStack stack = new ItemStack(ModItems.FILLED_SOUL_EXTRACTOR.get());
        stack.set(ModDataComponents.SPAWNER_ENTITY.get(), entityId);
        return Stream.of(stack);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return ModIngredientTypes.STRICT_SYRINGE.get();
    }

    public static final MapCodec<StrictSyringeIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("entity").forGetter(i -> i.entityId)
            ).apply(instance, StrictSyringeIngredient::new)
    );
}
