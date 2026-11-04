package dev.manny.createmobgrinding.item;

import dev.manny.createmobgrinding.block.entity.RotationalMobSpawnerBlockEntity;
import dev.manny.createmobgrinding.menu.SpawnerChunkBagMenu;
import dev.manny.createmobgrinding.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpawnerChunkBagItem extends Item {
    private static final int MAX_TOOLTIP_LINES = 6;

    public SpawnerChunkBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            open(serverPlayer, hand, null);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /**
     * Sneak + right-click on a spawner: pulls its chunk into the bag. Sneaking skips the block's
     * own interaction, so this is only reached in that case (the normal click opens swap mode).
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || !(level.getBlockEntity(context.getClickedPos()) instanceof RotationalMobSpawnerBlockEntity spawnerBE)) {
            return super.useOn(context);
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack chunk = spawnerBE.inventory.getStackInSlot(0);
        if (chunk.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.createmobgrinding.spawner_chunk_bag.spawner_empty").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        ItemStack bag = context.getItemInHand();
        NonNullList<ItemStack> items = NonNullList.withSize(SpawnerChunkBagMenu.BAG_SLOTS, ItemStack.EMPTY);
        bag.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);

        SimpleContainer container = new SimpleContainer(items.toArray(ItemStack[]::new));
        ItemStack remainder = container.addItem(chunk.copy());
        if (!remainder.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.createmobgrinding.spawner_chunk_bag.full").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        bag.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(container.getItems()));
        spawnerBE.inventory.setStackInSlot(0, ItemStack.EMPTY);
        level.playSound(null, context.getClickedPos(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.8F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    /**
     * Opens the bag. With a spawner position the bag opens in swap mode:
     * clicking a chunk swaps it with the one inside that spawner.
     */
    public static void open(ServerPlayer player, InteractionHand hand, @Nullable BlockPos spawnerPos) {
        Component title = Component.translatable("container.createmobgrinding.spawner_chunk_bag");
        if (spawnerPos != null && player.level().getBlockEntity(spawnerPos) instanceof RotationalMobSpawnerBlockEntity spawnerBE) {
            ResourceLocation current = spawnerBE.inventory.getStackInSlot(0).get(ModDataComponents.SPAWNER_ENTITY.get());
            Component mob = current != null
                    ? Component.translatable(net.minecraft.Util.makeDescriptionId("entity", current))
                    : Component.translatable("container.createmobgrinding.spawner_chunk_bag.empty");
            title = Component.translatable("container.createmobgrinding.spawner_chunk_bag.swap", mob);
        }

        player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> new SpawnerChunkBagMenu(id, inventory, hand, spawnerPos), title),
                buf -> SpawnerChunkBagMenu.writeOpenData(buf, hand, spawnerPos));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

        // Group chunks by mob, keeping the bag order
        Map<ResourceLocation, Integer> counts = new LinkedHashMap<>();
        contents.nonEmptyItems().forEach(chunk -> {
            ResourceLocation entity = chunk.get(ModDataComponents.SPAWNER_ENTITY.get());
            if (entity != null) counts.merge(entity, chunk.getCount(), Integer::sum);
        });

        if (counts.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.createmobgrinding.spawner_chunk_bag.empty").withStyle(ChatFormatting.GRAY));
        } else {
            int shown = 0;
            for (Map.Entry<ResourceLocation, Integer> entry : counts.entrySet()) {
                if (shown++ == MAX_TOOLTIP_LINES) {
                    tooltip.add(Component.translatable("tooltip.createmobgrinding.spawner_chunk_bag.more", counts.size() - MAX_TOOLTIP_LINES).withStyle(ChatFormatting.DARK_GRAY));
                    break;
                }
                tooltip.add(Component.literal(entry.getValue() + "x ").withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable(net.minecraft.Util.makeDescriptionId("entity", entry.getKey())).withStyle(ChatFormatting.AQUA)));
            }
        }
        tooltip.add(Component.translatable("tooltip.createmobgrinding.spawner_chunk_bag.hint").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
