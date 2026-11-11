package dev.manny.createmobgrinding.compat.create;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import dev.manny.createmobgrinding.block.entity.RotationalMobGrinderBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Display Link source: how many mobs the grinder has killed.
 */
public class GrinderKillsDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof RotationalMobGrinderBlockEntity grinder)) return EMPTY_LINE;
        return Component.literal(String.valueOf(grinder.getKillCount()));
    }

    @Override
    protected String getTranslationKey() {
        return "grinder_kills";
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
