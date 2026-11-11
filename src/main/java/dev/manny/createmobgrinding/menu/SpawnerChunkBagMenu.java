package dev.manny.createmobgrinding.menu;

import dev.manny.createmobgrinding.block.entity.RotationalMobSpawnerBlockEntity;
import dev.manny.createmobgrinding.config.ModConfigs;
import dev.manny.createmobgrinding.registry.ModDataComponents;
import dev.manny.createmobgrinding.registry.ModItems;
import dev.manny.createmobgrinding.registry.ModMenuTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.Nullable;

public class SpawnerChunkBagMenu extends AbstractContainerMenu {
    public static final int ROWS = 3;
    public static final int BAG_SLOTS = ROWS * 9;
    private static final int OFFHAND_SWAP_BUTTON = 40;

    private final Player player;
    private final InteractionHand hand;
    private final ItemStack bag;
    private final SimpleContainer container;
    @Nullable
    private final BlockPos spawnerPos;
    // Menu index of the hotbar slot holding the bag, -1 when the bag is in the offhand
    private final int lockedSlot;

    public static void writeOpenData(RegistryFriendlyByteBuf buf, InteractionHand hand, @Nullable BlockPos spawnerPos) {
        buf.writeEnum(hand);
        buf.writeBoolean(spawnerPos != null);
        if (spawnerPos != null) buf.writeBlockPos(spawnerPos);
    }

    public static SpawnerChunkBagMenu fromNetwork(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        BlockPos pos = buf.readBoolean() ? buf.readBlockPos() : null;
        return new SpawnerChunkBagMenu(containerId, inventory, hand, pos);
    }

    public SpawnerChunkBagMenu(int containerId, Inventory inventory, InteractionHand hand, @Nullable BlockPos spawnerPos) {
        super(ModMenuTypes.SPAWNER_CHUNK_BAG.get(), containerId);
        this.player = inventory.player;
        this.hand = hand;
        this.bag = player.getItemInHand(hand);
        this.spawnerPos = spawnerPos;

        this.container = new SimpleContainer(BAG_SLOTS) {
            @Override
            public void setChanged() {
                super.setChanged();
                saveToBag();
            }
        };
        // Fill the backing list directly: setItem() would save a half-loaded bag on every call
        bag.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(container.getItems());

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return stack.is(ModItems.MOB_SPAWNER_CHUNK.get());
                    }
                });
            }
        }

        int playerInvY = 18 + ROWS * 18 + 13;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        int locked = -1;
        for (int col = 0; col < 9; col++) {
            boolean isBagSlot = hand == InteractionHand.MAIN_HAND && col == inventory.selected;
            if (isBagSlot) locked = slots.size();
            addSlot(new Slot(inventory, col, 8 + col * 18, playerInvY + 58) {
                @Override
                public boolean mayPickup(Player player) {
                    return !isBagSlot;
                }
            });
        }
        this.lockedSlot = locked;
    }

    public boolean isSwapMode() {
        return spawnerPos != null;
    }

    private void saveToBag() {
        if (player.level().isClientSide) return;
        NonNullList<ItemStack> items = NonNullList.withSize(BAG_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < BAG_SLOTS; i++) {
            items.set(i, container.getItem(i));
        }
        bag.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // The bag itself must never move while it is open
        if (slotId == lockedSlot && lockedSlot != -1) return;
        if (clickType == ClickType.SWAP) {
            if (hand == InteractionHand.MAIN_HAND && button == player.getInventory().selected) return;
            if (hand == InteractionHand.OFF_HAND && button == OFFHAND_SWAP_BUTTON) return;
        }

        if (isSwapMode() && clickType == ClickType.PICKUP && slotId >= 0 && slotId < BAG_SLOTS && getCarried().isEmpty()) {
            if (!container.getItem(slotId).isEmpty()) {
                // Handled on the server only; the slot change is then synced back to the client
                if (!player.level().isClientSide) swapWithSpawner(slotId);
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void swapWithSpawner(int slot) {
        if (!(player.level().getBlockEntity(spawnerPos) instanceof RotationalMobSpawnerBlockEntity spawnerBE)) return;

        ItemStack chosen = container.getItem(slot);
        ResourceLocation entity = chosen.get(ModDataComponents.SPAWNER_ENTITY.get());
        if (entity != null && ModConfigs.COMMON.spawnerBlacklist.get().contains(entity.toString())) {
            player.displayClientMessage(Component.translatable("message.createmobgrinding.mob_blacklisted").withStyle(ChatFormatting.RED), true);
            return;
        }

        ItemStack current = spawnerBE.inventory.getStackInSlot(0);
        int limit = spawnerBE.inventory.getSlotLimit(0);

        // Same mob: top up the spawner's stack from the bag
        if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, chosen)) {
            int moved = Math.min(chosen.getCount(), limit - current.getCount());
            if (moved <= 0) {
                player.displayClientMessage(Component.translatable("message.createmobgrinding.spawner_full").withStyle(ChatFormatting.RED), true);
                return;
            }
            spawnerBE.inventory.setStackInSlot(0, current.copyWithCount(current.getCount() + moved));
            chosen.shrink(moved);
            container.setChanged();
            player.level().playSound(null, spawnerPos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 0.8F, 1.2F);
            return;
        }

        // Different mob: the whole stack goes back to the bag and as many chosen chunks as fit go in
        ItemStack previous = current.copy();
        int moved = Math.min(chosen.getCount(), limit);
        spawnerBE.inventory.setStackInSlot(0, chosen.copyWithCount(moved));
        chosen.shrink(moved);

        if (!previous.isEmpty()) {
            // Put the old chunk where the new one was when possible, so the swap feels instant
            if (container.getItem(slot).isEmpty()) {
                container.setItem(slot, previous);
                previous = ItemStack.EMPTY;
            } else {
                previous = container.addItem(previous);
            }
            if (!previous.isEmpty()) {
                net.neoforged.neoforge.items.ItemHandlerHelper.giveItemToPlayer(player, previous);
            }
        }
        container.setChanged();
        player.level().playSound(null, spawnerPos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 0.8F, 1.2F);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < BAG_SLOTS) {
            if (!moveItemStackTo(stack, BAG_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!stack.is(ModItems.MOB_SPAWNER_CHUNK.get()) || !moveItemStackTo(stack, 0, BAG_SLOTS, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.getItemInHand(hand) != bag) return false;
        if (spawnerPos == null) return true;
        return player.level().getBlockEntity(spawnerPos) instanceof RotationalMobSpawnerBlockEntity
                && player.distanceToSqr(spawnerPos.getCenter()) <= 64.0;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        saveToBag();
    }
}
