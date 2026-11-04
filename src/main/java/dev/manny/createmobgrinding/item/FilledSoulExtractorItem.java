package dev.manny.createmobgrinding.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import dev.manny.createmobgrinding.registry.ModDataComponents;

import java.util.List;

public class FilledSoulExtractorItem extends Item {
    
    public FilledSoulExtractorItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Always has enchanted glint
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ResourceLocation entityLoc = stack.get(ModDataComponents.SPAWNER_ENTITY.get());
        if (entityLoc != null) {
            tooltipComponents.add(Component.translatable("tooltip.createmobgrinding.dna",
                Component.translatable(net.minecraft.Util.makeDescriptionId("entity", entityLoc)).withStyle(net.minecraft.ChatFormatting.AQUA)).withStyle(net.minecraft.ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.createmobgrinding.dna.unknown").withStyle(net.minecraft.ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
