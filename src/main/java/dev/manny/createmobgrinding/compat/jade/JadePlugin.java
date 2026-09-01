package dev.manny.createmobgrinding.compat.jade;

import dev.manny.createmobgrinding.block.RotationalMobGrinderBlock;
import dev.manny.createmobgrinding.block.RotationalMobSpawnerBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(SpawnerComponentProvider.INSTANCE, dev.manny.createmobgrinding.block.entity.RotationalMobSpawnerBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(SpawnerComponentProvider.INSTANCE, RotationalMobSpawnerBlock.class);
        registration.registerBlockComponent(GrinderComponentProvider.INSTANCE, RotationalMobGrinderBlock.class);
        registration.registerEntityComponent(MobTierComponentProvider.INSTANCE, net.minecraft.world.entity.LivingEntity.class);
    }
}

