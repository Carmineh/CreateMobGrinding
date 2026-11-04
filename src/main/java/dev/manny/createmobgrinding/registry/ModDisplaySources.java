package dev.manny.createmobgrinding.registry;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import dev.manny.createmobgrinding.CreateMobGrinding;
import dev.manny.createmobgrinding.compat.create.SpawnerMobDisplaySource;
import dev.manny.createmobgrinding.compat.create.SpawnerProgressDisplaySource;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDisplaySources {
    public static final DeferredRegister<DisplaySource> DISPLAY_SOURCES = DeferredRegister.create(CreateRegistries.DISPLAY_SOURCE, CreateMobGrinding.MOD_ID);

    public static final Supplier<DisplaySource> SPAWNER_PROGRESS = DISPLAY_SOURCES.register("spawner_progress", SpawnerProgressDisplaySource::new);
    public static final Supplier<DisplaySource> SPAWNER_MOB = DISPLAY_SOURCES.register("spawner_mob", SpawnerMobDisplaySource::new);

    /**
     * Links the sources to the spawner so a Display Link attached to it offers them. Call after registration.
     */
    public static void associate() {
        DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.ROTATIONAL_MOB_SPAWNER.get(), SPAWNER_PROGRESS.get());
        DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.ROTATIONAL_MOB_SPAWNER.get(), SPAWNER_MOB.get());
    }
}
