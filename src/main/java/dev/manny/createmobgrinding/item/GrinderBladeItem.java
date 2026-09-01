package dev.manny.createmobgrinding.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GrinderBladeItem extends Item {
    
    private final int enchantability;

    public GrinderBladeItem(Properties properties, int enchantability) {
        super(properties);
        this.enchantability = enchantability;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantability;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return this.enchantability > 0;
    }
}
