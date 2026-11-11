package dev.manny.createmobgrinding.registry;

import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import dev.manny.createmobgrinding.CreateMobGrinding;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModArmInteractionPoints {
    public static final DeferredRegister<ArmInteractionPointType> ARM_INTERACTION_POINTS = DeferredRegister.create(CreateRegistries.ARM_INTERACTION_POINT_TYPE, CreateMobGrinding.MOD_ID);

    // The default point inserts into / extracts from the block's item handler, which is the spawner's chunk slot
    public static final Supplier<ArmInteractionPointType> ROTATIONAL_MOB_SPAWNER = ARM_INTERACTION_POINTS.register("rotational_mob_spawner",
            () -> new ArmInteractionPointType() {
                @Override
                public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
                    return state.is(ModBlocks.ROTATIONAL_MOB_SPAWNER.get());
                }

                @Override
                public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
                    return new ArmInteractionPoint(this, level, pos, state);
                }
            });
}
