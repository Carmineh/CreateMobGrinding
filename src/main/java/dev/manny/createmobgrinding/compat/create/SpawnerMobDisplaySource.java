package dev.manny.createmobgrinding.compat.create;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import dev.manny.createmobgrinding.block.entity.RotationalMobSpawnerBlockEntity;
import dev.manny.createmobgrinding.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Display Link source: name of the mob stored in the spawner's chunk.
 */
public class SpawnerMobDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof RotationalMobSpawnerBlockEntity spawner)) return EMPTY_LINE;
        ResourceLocation entity = spawner.inventory.getStackInSlot(0).get(ModDataComponents.SPAWNER_ENTITY.get());
        if (entity == null) return EMPTY_LINE;
        return Component.translatable(net.minecraft.Util.makeDescriptionId("entity", entity));
    }

    // The mob only changes when the chunk is swapped (Create's default is 100 ticks)
    @Override
    public int getPassiveRefreshTicks() {
        return 20;
    }

    @Override
    protected String getTranslationKey() {
        return "spawner_mob";
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
