package dev.manny.createmobgrinding.event;

import com.simibubi.create.content.kinetics.fan.EncasedFanBlock;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import dev.manny.createmobgrinding.CreateMobGrinding;
import dev.manny.createmobgrinding.fan.FanFilterHolder;
import dev.manny.createmobgrinding.fan.FanFilterMode;
import dev.manny.createmobgrinding.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Fan Filter Upgrade interactions on Create's Encased Fan:
 * - right-click with the upgrade: install it
 * - Wrench on the front or back face: cycle the mode (Create ignores those faces, so rotation is unaffected)
 * - sneak + right-click with an empty hand: take the upgrade back
 */
@EventBusSubscriber(modid = CreateMobGrinding.MOD_ID)
public class FanFilterEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        if (!(state.getBlock() instanceof EncasedFanBlock)) return;
        if (!(level.getBlockEntity(event.getPos()) instanceof EncasedFanBlockEntity fan)) return;
        if (!(fan instanceof FanFilterHolder holder)) return;

        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        FanFilterMode current = holder.cmg$getFanFilter();

        if (stack.is(ModItems.FAN_FILTER_UPGRADE.get())) {
            consume(event);
            if (level.isClientSide) return;
            if (current != null) {
                player.displayClientMessage(Component.translatable("fan_filter.createmobgrinding.already_installed").withStyle(ChatFormatting.RED), true);
                return;
            }
            setFilter(fan, holder, FanFilterMode.ITEMS);
            if (!player.isCreative()) stack.shrink(1);
            notifyMode(player, FanFilterMode.ITEMS);
            level.playSound(null, event.getPos(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }

        if (current == null) return;

        boolean airflowFace = event.getFace() != null
                && event.getFace().getAxis() == state.getValue(EncasedFanBlock.FACING).getAxis();
        if (stack.is(net.neoforged.neoforge.common.Tags.Items.TOOLS_WRENCH) && !player.isShiftKeyDown() && airflowFace) {
            consume(event);
            if (level.isClientSide) return;
            FanFilterMode next = current.next();
            setFilter(fan, holder, next);
            notifyMode(player, next);
            level.playSound(null, event.getPos(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.5F, 1.2F);
            return;
        }

        if (stack.isEmpty() && player.isShiftKeyDown()) {
            consume(event);
            if (level.isClientSide) return;
            setFilter(fan, holder, null);
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(ModItems.FAN_FILTER_UPGRADE.get()));
            level.playSound(null, event.getPos(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static void consume(PlayerInteractEvent.RightClickBlock event) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static void setFilter(EncasedFanBlockEntity fan, FanFilterHolder holder, FanFilterMode mode) {
        holder.cmg$setFanFilter(mode);
        fan.setChanged();
        fan.sendData();
        // Re-scan right away so the new mode applies without waiting for the next search
        fan.airCurrent.findEntities();
    }

    private static void notifyMode(Player player, FanFilterMode mode) {
        player.displayClientMessage(Component.translatable("fan_filter.createmobgrinding.mode_set",
                Component.translatable(mode.getTranslationKey()).withStyle(ChatFormatting.AQUA)), true);
    }
}
