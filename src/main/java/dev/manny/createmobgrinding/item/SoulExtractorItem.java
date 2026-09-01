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
        // Check health <= 10%
        float healthPct = interactionTarget.getHealth() / interactionTarget.getMaxHealth();
        if (healthPct > 0.1f) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(Component.literal("Target has too much health to extract!").withStyle(net.minecraft.ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }

        ResourceLocation entityType = BuiltInRegistries.ENTITY_TYPE.getKey(interactionTarget.getType());
        
        // Determine tier
        int targetTier = dev.manny.createmobgrinding.util.MobTierHelper.getMobTier(interactionTarget.getType());

        if (targetTier > this.maxTier) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(Component.literal("This extractor is too weak for this mob!").withStyle(net.minecraft.ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }

        if (!player.level().isClientSide) {
            // Kill entity
            interactionTarget.discard(); // Instantly remove without dropping items

            // Determine filled extractor
            Item filledItem = ModItems.BASIC_FILLED_SOUL_EXTRACTOR.get();
            if (this.maxTier == 4) filledItem = ModItems.ADVANCED_FILLED_SOUL_EXTRACTOR.get();
            else if (this.maxTier == 5) filledItem = ModItems.ELITE_FILLED_SOUL_EXTRACTOR.get();
            
            ItemStack filled = new ItemStack(filledItem);
            filled.set(ModDataComponents.SPAWNER_ENTITY.get(), entityType);

            // Consume and replace extractor
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            
            if (stack.isEmpty()) {
                // VANILLA BUG WORKAROUND: Player.interactOn will explicitly wipe the hand slot if 'stack' becomes empty.
                // If we put the filled item in the hand slot, it gets deleted.
                // We temporarily put a dummy item in the hand so 'add' chooses a different safe slot.
                player.setItemInHand(usedHand, new ItemStack(net.minecraft.world.item.Items.BEDROCK));
                if (!player.getInventory().add(filled)) {
                    player.drop(filled, false);
                }
                // The caller will now wipe the BEDROCK from the hand, leaving our filled extractor safe.
            } else {
                if (!player.getInventory().add(filled)) {
                    player.drop(filled, false);
                }
            }

            // Play a nice sound
            player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.SOUL_ESCAPE.value(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}
