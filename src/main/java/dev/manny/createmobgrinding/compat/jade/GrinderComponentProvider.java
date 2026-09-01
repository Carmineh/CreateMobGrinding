package dev.manny.createmobgrinding.compat.jade;

import dev.manny.createmobgrinding.block.entity.RotationalMobGrinderBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum GrinderComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof RotationalMobGrinderBlockEntity grinderBE) {
            ItemStack weapon = grinderBE.getInternalWeapon();
            ItemEnchantments enchants = weapon.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

            if (enchants.isEmpty()) {
                tooltip.add(Component.translatable("jade.createmobgrinding.grinder.no_enchants"));
                return;
            }

            if (accessor.getPlayer().isShiftKeyDown()) {
                enchants.keySet().forEach(ench -> {
                    tooltip.add(Component.translatable(ench.unwrapKey().get().location().toLanguageKey("enchantment")).append(" " + enchants.getLevel(ench)));
                });
            } else {
                tooltip.add(Component.translatable("jade.createmobgrinding.grinder.shift_for_enchants"));
            }
            
            boolean hasUpgrades = false;
            for (int i = 0; i < grinderBE.upgrades.getSlots(); i++) {
                if (!grinderBE.upgrades.getStackInSlot(i).isEmpty()) {
                    hasUpgrades = true;
                    break;
                }
            }
            
            if (hasUpgrades) {
                tooltip.add(Component.translatable("tooltip.createmobgrinding.spawner.upgrades"));
                for (int i = 0; i < grinderBE.upgrades.getSlots(); i++) {
                    ItemStack upgrade = grinderBE.upgrades.getStackInSlot(i);
                    if (!upgrade.isEmpty()) {
                        tooltip.append(tooltip.getElementHelper().item(upgrade, 0.5f));
                    }
                }
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath("createmobgrinding", "grinder_provider");
    }
}


