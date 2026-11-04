package dev.manny.createmobgrinding.mixin;

import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import dev.manny.createmobgrinding.fan.FanFilterHolder;
import dev.manny.createmobgrinding.fan.FanFilterMode;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EncasedFanBlockEntity.class, remap = false)
public abstract class EncasedFanBlockEntityMixin implements FanFilterHolder {
    @Unique
    private static final String cmg$FILTER_KEY = "CreateMobGrindingFanFilter";

    @Unique
    @Nullable
    private FanFilterMode cmg$fanFilter;

    @Override
    public FanFilterMode cmg$getFanFilter() {
        return cmg$fanFilter;
    }

    @Override
    public void cmg$setFanFilter(@Nullable FanFilterMode mode) {
        cmg$fanFilter = mode;
    }

    // Written for both saves and client packets, so the client filters the same entities as the server
    @Inject(method = "write", at = @At("TAIL"))
    private void cmg$writeFilter(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (cmg$fanFilter != null) {
            compound.putString(cmg$FILTER_KEY, cmg$fanFilter.name());
        }
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void cmg$readFilter(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        cmg$fanFilter = compound.contains(cmg$FILTER_KEY) ? FanFilterMode.byName(compound.getString(cmg$FILTER_KEY)) : null;
    }
}
