package dev.manny.createmobgrinding.fan;

import org.jetbrains.annotations.Nullable;

/**
 * Added to Create's EncasedFanBlockEntity by a mixin. A null mode means no upgrade installed.
 */
public interface FanFilterHolder {
    @Nullable
    FanFilterMode cmg$getFanFilter();

    void cmg$setFanFilter(@Nullable FanFilterMode mode);
}
