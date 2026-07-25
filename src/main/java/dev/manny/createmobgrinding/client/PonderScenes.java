package dev.manny.createmobgrinding.client;

import dev.manny.createmobgrinding.registry.ModBlocks;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PonderScenes {

    public static void spawnerConversion(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("spawner_conversion", "Upgrading a Vanilla Spawner");
        scene.configureBasePlate(0, 0, 5);

        BlockPos spawnerPos = util.grid().at(2, 1, 2);
        Selection spawnerSelection = util.select().position(spawnerPos);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        scene.world().showSection(spawnerSelection, Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(60)
                .text("To create a Rotational Mob Spawner...")
                .pointAt(util.vector().topOf(spawnerPos))
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showText(50)
                .text("Right-click it with a Brass Casing.")
                .pointAt(util.vector().topOf(spawnerPos))
                .placeNearTarget();
        scene.idle(40);

        scene.world().setBlock(spawnerPos, ModBlocks.ROTATIONAL_MOB_SPAWNER.get().defaultBlockState(), true);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("Then insert a Mob Spawner Chunk with the desired soul.")
                .pointAt(util.vector().topOf(spawnerPos))
                .placeNearTarget();
        scene.idle(70);
    }

    public static void spawnerPowering(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("spawner_powering", "Powering the Spawner");
        scene.configureBasePlate(0, 0, 5);

        BlockPos motorPos = util.grid().at(2, 1, 2);
        BlockPos spawnerPos = util.grid().at(2, 2, 2);
        Selection spawnerSelection = util.select().position(spawnerPos);
        Selection motorSelection = util.select().position(motorPos);

        scene.world().modifyBlockEntityNBT(spawnerSelection, BlockEntity.class, nbt -> nbt.putFloat("Speed", 0f));
        scene.world().modifyBlockEntityNBT(motorSelection, BlockEntity.class, nbt -> nbt.putFloat("Speed", 0f));

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        
        scene.world().showSection(spawnerSelection, Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(50)
                .text("The Rotational Spawner requires kinetic energy to work.")
                .pointAt(util.vector().centerOf(spawnerPos))
                .placeNearTarget();
        scene.idle(60);

        scene.world().showSection(motorSelection, Direction.NORTH);
        scene.idle(15);

        scene.overlay().showText(50)
                .text("Provide rotational power to its axis.")
                .pointAt(util.vector().centerOf(motorPos))
                .placeNearTarget();
        scene.idle(40);

        scene.world().modifyBlockEntityNBT(spawnerSelection, BlockEntity.class, nbt -> nbt.putFloat("Speed", 32f));
        scene.world().modifyBlockEntityNBT(motorSelection, BlockEntity.class, nbt -> nbt.putFloat("Speed", 32f));
        
        scene.idle(40);
        
        scene.overlay().showText(60)
                .text("The speed of rotation determines the spawn rate.")
                .pointAt(util.vector().topOf(spawnerPos))
                .placeNearTarget();
        scene.idle(70);
    }

    public static void spawnerStacking(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("spawner_stacking", "Stacking Spawners");
        scene.configureBasePlate(0, 0, 5);

        BlockPos motorPos = util.grid().at(2, 1, 2);
        Selection motorSelection = util.select().position(motorPos);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(motorSelection, Direction.UP);
        scene.idle(10);

        scene.overlay().showText(60)
                .text("The internal axis allows you to stack them.")
                .pointAt(util.vector().topOf(motorPos))
                .placeNearTarget();
        scene.idle(70);

        for (int i = 2; i <= 5; i++) {
            BlockPos spawnerPos = util.grid().at(2, i, 2);
            Selection spawnerSelection = util.select().position(spawnerPos);
            scene.world().showSection(spawnerSelection, Direction.DOWN);
            scene.idle(5);
        }

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Kinetic power is passed through directly to adjacent spawners!")
                .pointAt(util.vector().centerOf(util.grid().at(2, 3, 2)))
                .placeNearTarget();
        scene.idle(80);
    }

    public static void grinderBlades(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("grinder_blades", "Upgrading Grinder Blades");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(20);
        
        BlockPos grinderPos = util.grid().at(2, 1, 2);
        Selection grinderSelection = util.select().position(grinderPos);
        
        scene.overlay().showText(60)
                .text("The Rotational Mob Grinder uses kinetic power to damage entities.")
                .pointAt(util.vector().centerOf(grinderPos))
                .placeNearTarget();
        scene.idle(70);
        
        scene.overlay().showText(60)
                .text("The Iron Blade is the most basic tier.")
                .pointAt(util.vector().topOf(grinderPos))
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Right-click the Grinder with a new blade to swap it out.")
                .pointAt(util.vector().topOf(grinderPos))
                .placeNearTarget();
        scene.idle(70);

        scene.world().modifyBlockEntityNBT(grinderSelection, BlockEntity.class, nbt -> {
            net.minecraft.nbt.CompoundTag itemTag = new net.minecraft.nbt.CompoundTag();
            itemTag.putString("id", "createmobgrinding:brass_grinder_blade");
            itemTag.putInt("count", 1);
            nbt.put("InstalledBlade", itemTag);
        });
        scene.effects().indicateSuccess(grinderPos);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("The Brass Blade deals double the damage.")
                .pointAt(util.vector().centerOf(grinderPos))
                .placeNearTarget();
        scene.idle(70);

        scene.world().modifyBlockEntityNBT(grinderSelection, BlockEntity.class, nbt -> {
            net.minecraft.nbt.CompoundTag itemTag = new net.minecraft.nbt.CompoundTag();
            itemTag.putString("id", "createmobgrinding:diamond_grinder_blade");
            itemTag.putInt("count", 1);
            nbt.put("InstalledBlade", itemTag);
        });
        scene.effects().indicateSuccess(grinderPos);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("The Diamond Blade deals four times the damage.")
                .pointAt(util.vector().centerOf(grinderPos))
                .placeNearTarget();
        scene.idle(70);

        scene.world().modifyBlockEntityNBT(grinderSelection, BlockEntity.class, nbt -> {
            net.minecraft.nbt.CompoundTag itemTag = new net.minecraft.nbt.CompoundTag();
            itemTag.putString("id", "createmobgrinding:netherite_grinder_blade");
            itemTag.putInt("count", 1);
            nbt.put("InstalledBlade", itemTag);
        });
        scene.effects().indicateSuccess(grinderPos);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("The Netherite Blade is the ultimate tier, dealing massive damage!")
                .pointAt(util.vector().centerOf(grinderPos))
                .placeNearTarget();
        scene.idle(70);

        // Enchantments
        scene.overlay().showText(60)
                .text("You can also apply Enchanted Books directly to the Grinder!")
                .pointAt(util.vector().topOf(grinderPos))
                .placeNearTarget();
        scene.idle(10);
        
        scene.world().createItemEntity(
            util.vector().topOf(grinderPos), 
            util.vector().of(0, 0.1, 0), 
            new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK)
        );
        scene.idle(60);

        scene.overlay().showText(60)
                .text("Valid enchantments: Looting, Fire Aspect, and Beheading.")
                .pointAt(util.vector().centerOf(grinderPos))
                .placeNearTarget();
        scene.idle(70);
    }
}
