package dev.manny.createmobgrinding.registry;

import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.content.logistics.item.filter.attribute.SingletonItemAttribute;
import dev.manny.createmobgrinding.CreateMobGrinding;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ModItemAttributes {
    public static final DeferredRegister<ItemAttributeType> ITEM_ATTRIBUTES = 
        DeferredRegister.create(CreateRegistries.ITEM_ATTRIBUTE_TYPE, CreateMobGrinding.MOD_ID);

    public static final Supplier<ItemAttributeType> HAS_SPAWNER_DNA = 
        ITEM_ATTRIBUTES.register("has_spawner_dna", () -> singleton("has_spawner_dna", stack -> stack.has(ModDataComponents.SPAWNER_ENTITY.get())));

    public static final Supplier<ItemAttributeType> IS_SPAWNER_CHUNK = 
        ITEM_ATTRIBUTES.register("is_spawner_chunk", () -> singleton("is_spawner_chunk", stack -> 
            stack.is(ModItems.BLANK_SPAWNER_CHUNK.get()) || 
            stack.is(ModItems.BROKEN_SPAWNER_CHUNK.get()) || 
            stack.is(ModItems.UNFINISHED_SPAWNER_CHUNK.get()) || 
            stack.is(ModItems.MOB_SPAWNER_CHUNK.get())
        ));

    private static ItemAttributeType singleton(String id, Predicate<ItemStack> predicate) {
        return new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(type, (stack, level) -> predicate.test(stack), "createmobgrinding." + id));
    }
}

