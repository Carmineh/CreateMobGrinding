package dev.manny.createmobgrinding.compat.create;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.PercentOrProgressBarDisplaySource;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.manny.createmobgrinding.block.entity.RotationalMobSpawnerBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Display Link source: progress towards the next spawn, as a percentage or a progress bar.
 */
public class SpawnerProgressDisplaySource extends PercentOrProgressBarDisplaySource {

    @Override
    protected Float getProgress(DisplayLinkContext context) {
        if (!(context.getSourceBlockEntity() instanceof RotationalMobSpawnerBlockEntity spawner)) return null;
        if (spawner.inventory.getStackInSlot(0).isEmpty()) return null;
        return (float) (spawner.getSpawnProgress() / spawner.getSpawnThreshold());
    }

    @Override
    protected boolean progressBarActive(DisplayLinkContext context) {
        return context.sourceConfig().getInt("Mode") != 0;
    }

    // Progress changes every tick: refresh often so the bar moves smoothly (Create's default is 100 ticks)
    @Override
    public int getPassiveRefreshTicks() {
        return 5;
    }

    @Override
    protected String getTranslationKey() {
        return "spawner_progress";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(context, builder, isFirstLine);
        if (isFirstLine) return;
        // Same percent / progress bar switch as Create's Fill Level source, reusing its translations
        builder.addSelectionScrollInput(0, 120,
                (si, l) -> si.forOptions(CreateLang.translatedOptions("display_source.fill_level", "percent", "progress_bar"))
                        .titled(CreateLang.translateDirect("display_source.fill_level.display")),
                "Mode");
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
