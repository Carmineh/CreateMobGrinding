package dev.manny.createmobgrinding.mixin;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.manny.createmobgrinding.fan.FanFilterHolder;
import dev.manny.createmobgrinding.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drops the Fan Filter Upgrade when its fan is broken. Create calls destroy() only when the block
 * is really removed (not when a contraption picks it up), so the upgrade can't be duplicated.
 */
@Mixin(value = SmartBlockEntity.class, remap = false)
public abstract class SmartBlockEntityMixin {

    @Inject(method = "destroy", at = @At("HEAD"))
    private void cmg$dropFanFilter(CallbackInfo ci) {
        if (!((Object) this instanceof FanFilterHolder holder) || holder.cmg$getFanFilter() == null) return;
        BlockEntity be = (BlockEntity) (Object) this;
        if (be.getLevel() == null || be.getLevel().isClientSide) return;

        Block.popResource(be.getLevel(), be.getBlockPos(), new ItemStack(ModItems.FAN_FILTER_UPGRADE.get()));
        holder.cmg$setFanFilter(null);
    }
}
