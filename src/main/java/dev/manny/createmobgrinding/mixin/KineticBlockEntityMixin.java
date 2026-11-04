package dev.manny.createmobgrinding.mixin;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.manny.createmobgrinding.fan.FanFilterHolder;
import dev.manny.createmobgrinding.fan.FanFilterMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Goggle line for the fan filter. Lives on KineticBlockEntity because EncasedFanBlockEntity doesn't override it.
 */
@Mixin(value = KineticBlockEntity.class, remap = false)
public abstract class KineticBlockEntityMixin {

    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"), cancellable = true)
    private void cmg$addFanFilterTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof FanFilterHolder holder)) return;
        FanFilterMode mode = holder.cmg$getFanFilter();
        if (mode == null) return;

        tooltip.add(Component.literal("    ")
                .append(Component.translatable("fan_filter.createmobgrinding.goggle", Component.translatable(mode.getTranslationKey())))
                .withStyle(ChatFormatting.GRAY));
        cir.setReturnValue(true);
    }
}
