package dev.manny.createmobgrinding.compat.jade;

import dev.manny.createmobgrinding.util.MobTierHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum MobTierComponentProvider implements IEntityComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (accessor.getEntity() instanceof LivingEntity livingEntity && !(livingEntity instanceof net.minecraft.world.entity.player.Player)) {
            if (MobTierHelper.hasExplicitTier(livingEntity.getType())) {
                int tier = MobTierHelper.getMobTier(livingEntity.getType());
                tooltip.add(Component.translatable("jade.createmobgrinding.mob.tier", tier).withStyle(net.minecraft.ChatFormatting.GOLD));
            } else {
                tooltip.add(Component.translatable("jade.createmobgrinding.mob.tier.unknown").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath("createmobgrinding", "mob_tier_provider");
    }
}
