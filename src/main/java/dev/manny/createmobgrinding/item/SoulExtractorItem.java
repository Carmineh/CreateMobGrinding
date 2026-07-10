package dev.manny.createmobgrinding.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.manny.createmobgrinding.registry.ModDataComponents;
import dev.manny.createmobgrinding.registry.ModItems;
import dev.manny.createmobgrinding.registry.ModTags;
import net.minecraft.network.chat.Component;

public class SoulExtractorItem extends Item {
    private final int maxTier;

    public SoulExtractorItem(Properties properties, int maxTier) {
        super(properties);
        this.maxTier = maxTier;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (player.level().isClientSide) return InteractionResult.PASS;

        // Check health <= 10%
        float healthPct = interactionTarget.getHealth() / interactionTarget.getMaxHealth();
        if (healthPct > 0.1f) {
            player.displayClientMessage(Component.literal("Target has too much health to extract!").withStyle(net.minecraft.ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        ResourceLocation entityType = BuiltInRegistries.ENTITY_TYPE.getKey(interactionTarget.getType());
        
        // Determine tier
        int targetTier = 1;
        if (interactionTarget.getType().is(ModTags.TIER_5)) targetTier = 5;
        else if (interactionTarget.getType().is(ModTags.TIER_4)) targetTier = 4;
        else if (interactionTarget.getType().is(ModTags.TIER_3)) targetTier = 3;
        else if (interactionTarget.getType().is(ModTags.TIER_2)) targetTier = 2;

        if (targetTier > this.maxTier) {
            player.displayClientMessage(Component.literal("This extractor is too weak for this mob!").withStyle(net.minecraft.ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        // Kill entity
        interactionTarget.discard(); // Instantly remove without dropping items

        // Consume extractor
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        // Give filled extractor
        ItemStack filled = new ItemStack(ModItems.FILLED_SOUL_EXTRACTOR.get());
        filled.set(ModDataComponents.SPAWNER_ENTITY.get(), entityType);
        
        if (!player.getInventory().add(filled)) {
            player.drop(filled, false);
        }

        // Play a nice sound (using standard minecraft event sound)
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.SOUL_ESCAPE.value(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

        return InteractionResult.SUCCESS;
    }
}
