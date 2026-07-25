package dev.manny.createmobgrinding.event;

import dev.manny.createmobgrinding.CreateMobGrinding;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.Optional;

@EventBusSubscriber(modid = CreateMobGrinding.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ModEvents {

    @SubscribeEvent
    public static void onCommandsRegister(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        dev.manny.createmobgrinding.command.ModCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            ItemEnchantments enchants = weapon.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (enchants.isEmpty()) return;

            ResourceKey<Enchantment> beheadingKey = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "beheading"));
            Optional<net.minecraft.core.Holder.Reference<Enchantment>> holderOpt = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(beheadingKey);
            
            if (holderOpt.isPresent()) {
                int level = enchants.getLevel(holderOpt.get());
                if (level > 0) {
                    double chance = 0.0;
                    if (level == 1) chance = 0.10;
                    else if (level == 2) chance = 0.25;
                    else if (level >= 3) chance = 0.40;

                    if (player.getRandom().nextDouble() <= chance) {
                        LivingEntity target = event.getEntity();
                        ItemStack headDrop = ItemStack.EMPTY;

                        if (target.getType() == EntityType.ZOMBIE) headDrop = new ItemStack(Items.ZOMBIE_HEAD);
                        else if (target.getType() == EntityType.SKELETON) headDrop = new ItemStack(Items.SKELETON_SKULL);
                        else if (target.getType() == EntityType.CREEPER) headDrop = new ItemStack(Items.CREEPER_HEAD);
                        else if (target.getType() == EntityType.WITHER_SKELETON) headDrop = new ItemStack(Items.WITHER_SKELETON_SKULL);
                        else if (target.getType() == EntityType.PIGLIN) headDrop = new ItemStack(Items.PIGLIN_HEAD);

                        if (!headDrop.isEmpty()) {
                            event.getDrops().add(new ItemEntity(target.level(), target.getX(), target.getY(), target.getZ(), headDrop));
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAnvilUpdate(net.neoforged.neoforge.event.AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.getItem() instanceof net.minecraft.world.item.Item && 
            (left.is(dev.manny.createmobgrinding.registry.ModItems.IRON_GRINDER_BLADE.get()) ||
             left.is(dev.manny.createmobgrinding.registry.ModItems.BRASS_GRINDER_BLADE.get()) ||
             left.is(dev.manny.createmobgrinding.registry.ModItems.DIAMOND_GRINDER_BLADE.get()) ||
             left.is(dev.manny.createmobgrinding.registry.ModItems.NETHERITE_GRINDER_BLADE.get()) ||
             left.is(dev.manny.createmobgrinding.registry.ModItems.CREATIVE_GRINDER_BLADE.get()))) {
            
            ItemEnchantments enchantsToApply = ItemEnchantments.EMPTY;
            if (right.is(Items.ENCHANTED_BOOK)) {
                enchantsToApply = right.getOrDefault(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            } else if (right.is(left.getItem())) {
                enchantsToApply = right.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            }

            if (!enchantsToApply.isEmpty()) {
                ItemEnchantments existing = left.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(existing);
                
                boolean changed = false;
                int cost = 0;
                
                for (var ench : enchantsToApply.keySet()) {
                    ResourceLocation loc = ench.unwrapKey().get().location();
                    if (loc.equals(ResourceLocation.withDefaultNamespace("looting")) ||
                        loc.equals(ResourceLocation.withDefaultNamespace("fire_aspect")) ||
                        loc.equals(ResourceLocation.fromNamespaceAndPath(CreateMobGrinding.MOD_ID, "beheading"))) {
                        
                        int currentLvl = mutable.getLevel(ench);
                        int addedLvl = enchantsToApply.getLevel(ench);
                        int maxLvl = ench.value().getMaxLevel();
                        
                        int newLvl;
                        if (currentLvl == addedLvl) newLvl = Math.min(maxLvl, currentLvl + 1);
                        else newLvl = Math.max(currentLvl, addedLvl);
                        
                        if (newLvl > currentLvl) {
                            mutable.set(ench, newLvl);
                            changed = true;
                            cost += newLvl * 2;
                        }
                    }
                }
                
                if (changed) {
                    ItemStack out = left.copy();
                    net.minecraft.world.item.enchantment.EnchantmentHelper.setEnchantments(out, mutable.toImmutable());
                    event.setOutput(out);
                    event.setCost(cost == 0 ? 1 : cost);
                }
            }
        }
    }
}
