package dev.manny.createmobgrinding.mixin;

import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import dev.manny.createmobgrinding.fan.FanFilterHolder;
import dev.manny.createmobgrinding.fan.FanFilterMode;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = AirCurrent.class, remap = false)
public abstract class AirCurrentMixin {
    @Shadow
    @Final
    public IAirCurrentSource source;

    @Shadow
    protected List<Entity> caughtEntities;

    // Entities dropped here are never pushed or processed by this fan
    @Inject(method = "findEntities", at = @At("TAIL"))
    private void cmg$filterEntities(CallbackInfo ci) {
        if (!(source instanceof FanFilterHolder holder)) return;
        FanFilterMode mode = holder.cmg$getFanFilter();
        if (mode == null) return;

        // getEntities() may return an immutable list
        List<Entity> filtered = new ArrayList<>(caughtEntities);
        filtered.removeIf(mode::ignores);
        caughtEntities = filtered;
    }
}
