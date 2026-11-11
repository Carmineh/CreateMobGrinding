package dev.manny.createmobgrinding.client;

import dev.manny.createmobgrinding.registry.ModBlocks;
import net.createmod.catnip.math.Pointing;
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
                .attachKeyFrame()
                .pointAt(util.vector().topOf(spawnerPos))
                .placeNearTarget();
        scene.idle(40);

        scene.world().setBlock(spawnerPos, ModBlocks.ROTATIONAL_MOB_SPAWNER.get().defaultBlockState(), true);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("Then insert a Mob Spawner Chunk with the desired soul.")
                .attachKeyFrame()
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
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(motorPos))
                .placeNearTarget();
        scene.idle(40);

        scene.world().modifyBlockEntityNBT(spawnerSelection, BlockEntity.class, nbt -> nbt.putFloat("Speed", 32f));
        scene.world().modifyBlockEntityNBT(motorSelection, BlockEntity.class, nbt -> nbt.putFloat("Speed", 32f));
        
        scene.idle(40);
        
        scene.overlay().showText(60)
                .text("The speed of rotation determines the spawn rate.")
                .attachKeyFrame()
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
                .attachKeyFrame()
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
                .attachKeyFrame()
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
                .attachKeyFrame()
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
                .attachKeyFrame()
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
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(grinderPos))
                .placeNearTarget();
        scene.idle(70);

        // Enchantments
        scene.overlay().showText(60)
                .text("You can also apply Enchanted Books directly to the Grinder!")
                .attachKeyFrame()
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

    // Spawner chunk NBT in the ItemStackHandler format, so the ponder shows the mob inside the cage
    private static void setSpawnerChunks(SceneBuilder scene, Selection spawner, String entity, int count) {
        scene.world().modifyBlockEntityNBT(spawner, BlockEntity.class, nbt -> {
            net.minecraft.nbt.ListTag items = new net.minecraft.nbt.ListTag();
            if (count > 0) {
                net.minecraft.nbt.CompoundTag item = new net.minecraft.nbt.CompoundTag();
                item.putInt("Slot", 0);
                item.putString("id", "createmobgrinding:mob_spawner_chunk");
                item.putInt("count", count);
                net.minecraft.nbt.CompoundTag components = new net.minecraft.nbt.CompoundTag();
                components.putString("createmobgrinding:spawner_entity", entity);
                item.put("components", components);
                items.add(item);
            }
            net.minecraft.nbt.CompoundTag inventory = new net.minecraft.nbt.CompoundTag();
            inventory.put("Items", items);
            inventory.putInt("Size", 1);
            nbt.put("Inventory", inventory);
        });
    }

    private static net.minecraft.world.item.ItemStack chunkOf(String entity) {
        net.minecraft.world.item.ItemStack chunk = new net.minecraft.world.item.ItemStack(dev.manny.createmobgrinding.registry.ModItems.MOB_SPAWNER_CHUNK.get());
        chunk.set(dev.manny.createmobgrinding.registry.ModDataComponents.SPAWNER_ENTITY.get(), net.minecraft.resources.ResourceLocation.parse(entity));
        return chunk;
    }

    private static net.minecraft.world.item.ItemStack createItem(String path) {
        return new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create", path)));
    }

    public static void spawnerChunkStacking(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("spawner_chunk_stacking", "Stacking Spawner Chunks");
        scene.configureBasePlate(0, 0, 5);

        BlockPos spawnerPos = util.grid().at(2, 2, 2);
        Selection spawnerSelection = util.select().position(spawnerPos);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        setSpawnerChunks(scene, spawnerSelection, "minecraft:zombie", 1);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("A spawner can hold more Spawner Chunks of the same mob.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(spawnerPos))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(spawnerPos, Direction.NORTH), Pointing.RIGHT, 40)
                .rightClick().withItem(chunkOf("minecraft:zombie"));
        scene.idle(10);
        setSpawnerChunks(scene, spawnerSelection, "minecraft:zombie", 2);
        scene.effects().indicateSuccess(spawnerPos);
        scene.idle(40);

        scene.overlay().showText(70)
                .text("Right-click with a matching chunk to add it, up to 8 chunks.")
                .pointAt(util.vector().topOf(spawnerPos))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(80)
                .text("Every extra chunk makes it spawn faster. With 4 and 8 chunks it spawns one more mob at a time.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(spawnerPos))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("More chunks also need more Stress Units.")
                .pointAt(util.vector().centerOf(spawnerPos))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(spawnerPos, Direction.NORTH), Pointing.RIGHT, 40)
                .rightClick().withItem(createItem("wrench"));
        scene.idle(10);
        setSpawnerChunks(scene, spawnerSelection, "minecraft:zombie", 0);
        scene.idle(40);

        scene.overlay().showText(70)
                .text("Use a Wrench to take all the chunks back.")
                .pointAt(util.vector().topOf(spawnerPos))
                .placeNearTarget();
        scene.idle(80);
    }

    public static void spawnerAutomation(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("spawner_automation", "Automating the Spawner");
        scene.configureBasePlate(0, 0, 5);

        BlockPos spawnerPos = util.grid().at(2, 2, 2);
        BlockPos funnelPos = util.grid().at(2, 2, 1);
        Selection spawnerSelection = util.select().position(spawnerPos);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        setSpawnerChunks(scene, spawnerSelection, "minecraft:zombie", 1);
        scene.idle(20);

        // Andesite Funnel attached to the spawner's north face (a funnel faces away from the block it's attached to)
        net.minecraft.world.level.block.Block funnel = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create", "andesite_funnel"));
        scene.world().setBlock(funnelPos, funnel.defaultBlockState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, Direction.NORTH), false);
        scene.world().showSection(util.select().position(funnelPos), Direction.SOUTH);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("Funnels, Hoppers and Mechanical Arms can insert and extract Spawner Chunks.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(funnelPos))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(80)
                .text("Only chunks of the mob already inside are accepted, up to the stacking limit.")
                .pointAt(util.vector().centerOf(spawnerPos))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(80)
                .text("A Comparator reads the progress towards the next spawn.")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(spawnerPos, Direction.EAST))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(80)
                .text("A Display Link can show the spawn progress or the spawner's mob.")
                .pointAt(util.vector().topOf(spawnerPos))
                .placeNearTarget();
        scene.idle(90);
    }

    public static void chunkBag(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("spawner_chunk_bag", "Using the Spawner Chunk Bag");
        scene.configureBasePlate(0, 0, 5);

        BlockPos spawnerPos = util.grid().at(2, 2, 2);
        Selection spawnerSelection = util.select().position(spawnerPos);
        net.minecraft.world.item.ItemStack bag = new net.minecraft.world.item.ItemStack(dev.manny.createmobgrinding.registry.ModItems.SPAWNER_CHUNK_BAG.get());

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        setSpawnerChunks(scene, spawnerSelection, "minecraft:zombie", 1);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("The Spawner Chunk Bag holds up to 27 stacks of Spawner Chunks.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(spawnerPos))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(spawnerPos, Direction.NORTH), Pointing.RIGHT, 50)
                .rightClick().withItem(bag);
        scene.idle(10);
        scene.overlay().showText(80)
                .text("Use it on a spawner and click a chunk: it replaces the spawner's chunks, or is added to them if it's the same mob.")
                .pointAt(util.vector().topOf(spawnerPos))
                .placeNearTarget();
        scene.idle(60);
        setSpawnerChunks(scene, spawnerSelection, "minecraft:skeleton", 1);
        scene.effects().indicateSuccess(spawnerPos);
        scene.idle(40);

        scene.overlay().showControls(util.vector().blockSurface(spawnerPos, Direction.NORTH), Pointing.RIGHT, 50)
                .rightClick().whileSneaking().withItem(bag);
        scene.idle(10);
        setSpawnerChunks(scene, spawnerSelection, "minecraft:skeleton", 0);
        scene.overlay().showText(70)
                .text("Sneak and use it to put the spawner's chunks back in the bag.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(spawnerPos))
                .placeNearTarget();
        scene.idle(80);
    }

    public static void fanFilter(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("fan_filter_upgrade", "Filtering an Encased Fan");
        scene.configureBasePlate(0, 1, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(20);

        // Uses Create's "fan/direction" schematic: the fan sits at (1, 1, 4) and blows north
        BlockPos fanPos = util.grid().at(1, 1, 4);
        net.minecraft.world.item.ItemStack upgrade = new net.minecraft.world.item.ItemStack(dev.manny.createmobgrinding.registry.ModItems.FAN_FILTER_UPGRADE.get());

        scene.overlay().showControls(util.vector().topOf(fanPos), Pointing.DOWN, 40)
                .rightClick().withItem(upgrade);
        scene.idle(10);
        scene.effects().indicateSuccess(fanPos);
        scene.overlay().showText(70)
                .text("Right-click an Encased Fan with a Fan Filter Upgrade to install it.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(fanPos))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("The fan then ignores Items, Players or Mobs: they are not pushed or processed.")
                .pointAt(util.vector().centerOf(fanPos))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().blockSurface(fanPos, Direction.NORTH), Pointing.RIGHT, 40)
                .rightClick().withItem(createItem("wrench"));
        scene.idle(10);
        scene.overlay().showText(80)
                .text("Use a Wrench on the front or back of the fan to change what it ignores.")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(fanPos, Direction.NORTH))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showControls(util.vector().topOf(fanPos), Pointing.DOWN, 40)
                .rightClick().whileSneaking();
        scene.idle(10);
        scene.overlay().showText(70)
                .text("Sneak and right-click with an empty hand to take the upgrade back.")
                .pointAt(util.vector().topOf(fanPos))
                .placeNearTarget();
        scene.idle(80);
    }

    public static void grinderMonitoring(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("grinder_monitoring", "Monitoring the Grinder");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().layersFrom(1), Direction.DOWN);
        scene.idle(20);

        BlockPos grinderPos = util.grid().at(2, 1, 2);

        scene.overlay().showText(80)
                .text("With the Vacuum Upgrade, the grinder collects drops and experience into its inventory.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(grinderPos))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("Funnels and Hoppers can pull the collected items out.")
                .pointAt(util.vector().centerOf(grinderPos))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("A Comparator reads how full the vacuum inventory is.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(grinderPos))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(70)
                .text("The grinder counts its kills: show them with a Display Link.")
                .pointAt(util.vector().topOf(grinderPos))
                .placeNearTarget();
        scene.idle(80);
    }
}
