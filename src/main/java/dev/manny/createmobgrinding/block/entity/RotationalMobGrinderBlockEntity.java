package dev.manny.createmobgrinding.block.entity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.List;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.network.chat.Component;

public class RotationalMobGrinderBlockEntity extends KineticBlockEntity {

    private ItemStack internalWeapon = new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
    private ItemStack installedBlade = new ItemStack(dev.manny.createmobgrinding.registry.ModItems.IRON_GRINDER_BLADE.get());
    private float attackTimer = 0;
    // The fake attacker is kept far below the world: a creeper trying to take revenge
    // fails the SwellGoal check ("target within 3 blocks") and never swells.
    private static final double FAKE_PLAYER_Y = -500.0;
    
    public final net.neoforged.neoforge.items.ItemStackHandler upgrades = new net.neoforged.neoforge.items.ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };
    
    public final net.neoforged.neoforge.items.ItemStackHandler vacuumInventory = new net.neoforged.neoforge.items.ItemStackHandler(27) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            // Comparators read how full the vacuum inventory is
            if (level != null && !level.isClientSide) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    };
    
    private int xpBuffer = 0;
    private long killCount = 0;

    public RotationalMobGrinderBlockEntity(BlockPos pos, BlockState state) {
        super(dev.manny.createmobgrinding.registry.ModBlockEntities.ROTATIONAL_MOB_GRINDER.get(), pos, state);
    }

    public long getKillCount() {
        return killCount;
    }

    public ItemStack getInternalWeapon() {
        return internalWeapon;
    }

    public ItemStack getInstalledBlade() {
        return installedBlade;
    }

    public void setInstalledBlade(ItemStack blade) {
        this.installedBlade = blade.copy();
        setChanged();
        sendData();
        if (hasNetwork()) getOrCreateNetwork().updateNetwork();
    }
    
    public boolean hasProtectionUpgrade() {
        for (int i = 0; i < upgrades.getSlots(); i++) {
            if (upgrades.getStackInSlot(i).is(dev.manny.createmobgrinding.registry.ModItems.GRINDER_UPGRADE_PROTECTION.get())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasVacuumUpgrade() {
        for (int i = 0; i < upgrades.getSlots(); i++) {
            if (upgrades.getStackInSlot(i).is(dev.manny.createmobgrinding.registry.ModItems.GRINDER_UPGRADE_VACUUM.get())) {
                return true;
            }
        }
        return false;
    }


    private ItemEnchantments getCombinedEnchantments() {
        ItemEnchantments internal = internalWeapon.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments blade = installedBlade.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        
        if (blade.isEmpty()) return internal;
        if (internal.isEmpty()) return blade;
        
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(internal);
        for (var ench : blade.keySet()) {
            int current = mutable.getLevel(ench);
            int added = blade.getLevel(ench);
            mutable.set(ench, Math.max(current, added));
        }
        return mutable.toImmutable();
    }

    @Override
    public float calculateStressApplied() {
        var config = dev.manny.createmobgrinding.config.ModConfigs.COMMON;
        float impact = config.ironBladeStress.get().floatValue();
        if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.BRASS_GRINDER_BLADE.get())) impact = config.brassBladeStress.get().floatValue();
        else if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.DIAMOND_GRINDER_BLADE.get())) impact = config.diamondBladeStress.get().floatValue();
        else if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.NETHERITE_GRINDER_BLADE.get())) impact = config.netheriteBladeStress.get().floatValue();
        else if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.CREATIVE_GRINDER_BLADE.get())) impact = 0.0f;

        // Add impact for utility enchantments
        ItemEnchantments enchants = getCombinedEnchantments();
        if (!enchants.isEmpty()) {
            int totalLevels = enchants.keySet().stream().mapToInt(enchants::getLevel).sum();
            impact += totalLevels * config.stressPerEnchantmentLevel.get().floatValue();
        }
        this.lastStressApplied = impact;
        return impact;
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) return;

        float speed = Math.abs(getSpeed());
        if (speed == 0) return;
        
        if (hasVacuumUpgrade() && level.getGameTime() % 4 == 0) {
            vacuumItems();
        }

        attackTimer += speed;

        float attackInterval = dev.manny.createmobgrinding.config.ModConfigs.COMMON.attackInterval.get().floatValue();
        if (attackTimer >= attackInterval) {
            attackTimer -= attackInterval;
            performAttack();
        }
    }
    
    private void vacuumItems() {
        ServerLevel serverLevel = (ServerLevel) level;
        net.minecraft.core.Direction facing = getBlockState().hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING) ? 
                getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING) : 
                net.minecraft.core.Direction.NORTH;
                
        BlockPos targetPos = worldPosition.relative(facing);
        // Vacuum area: 3x3 plane exactly at the target block (1 block deep)
        AABB vacuumZone = new AABB(targetPos);
        double inflateX = facing.getAxis() == net.minecraft.core.Direction.Axis.X ? 0 : 1.0;
        double inflateY = facing.getAxis() == net.minecraft.core.Direction.Axis.Y ? 0 : 1.0;
        double inflateZ = facing.getAxis() == net.minecraft.core.Direction.Axis.Z ? 0 : 1.0;
        vacuumZone = vacuumZone.inflate(inflateX, inflateY, inflateZ);
        
        // Items
        List<net.minecraft.world.entity.item.ItemEntity> items = serverLevel.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, vacuumZone, net.minecraft.world.entity.Entity::isAlive);
        for (net.minecraft.world.entity.item.ItemEntity itemEntity : items) {
            ItemStack stack = itemEntity.getItem();
            ItemStack remainder = net.neoforged.neoforge.items.ItemHandlerHelper.insertItemStacked(vacuumInventory, stack, false);
            if (remainder.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(remainder);
            }
        }
        
        // Experience
        List<net.minecraft.world.entity.ExperienceOrb> orbs = serverLevel.getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class, vacuumZone, net.minecraft.world.entity.Entity::isAlive);
        for (net.minecraft.world.entity.ExperienceOrb orb : orbs) {
            xpBuffer += orb.getValue();
            orb.discard();
        }
        
        if (xpBuffer >= 3) {
            int nuggetsToProduce = xpBuffer / 3;
            ItemStack nuggetStack = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create", "experience_nugget")), nuggetsToProduce);
            ItemStack remainder = net.neoforged.neoforge.items.ItemHandlerHelper.insertItemStacked(vacuumInventory, nuggetStack, false);
            
            int successfullyInserted = nuggetsToProduce - remainder.getCount();
            xpBuffer -= successfullyInserted * 3;
            
            // If the inventory is full, we don't consume the xpBuffer completely, it just waits until there's space.
            if (!remainder.isEmpty()) {
                // Alternatively, we could spawn the remainder nuggets in the world, but the buffer approach is safer and less laggy.
            }
        }
    }

    private void performAttack() {
        ServerLevel serverLevel = (ServerLevel) level;
        
        net.minecraft.core.Direction facing = getBlockState().hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING) ? 
                getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING) : 
                net.minecraft.core.Direction.NORTH;
                
        BlockPos targetPos = worldPosition.relative(facing);
        // No inflate, area is perfectly 1 block (1x1x1) on the blade
        AABB killZone = new AABB(targetPos);

        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, killZone, net.minecraft.world.entity.Entity::isAlive);

        if (targets.isEmpty()) return;

        FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(serverLevel);
        // X/Z remain on the grinder, so knockback direction doesn't change.
        fakePlayer.setPos(worldPosition.getX() + 0.5, FAKE_PLAYER_Y, worldPosition.getZ() + 0.5);

        ItemEnchantments enchants = getCombinedEnchantments();
        ItemStack weaponToUse = internalWeapon.copy();
        net.minecraft.world.item.enchantment.EnchantmentHelper.setEnchantments(weaponToUse, enchants);

        fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, weaponToUse);

        net.minecraft.core.Registry<net.minecraft.world.item.enchantment.Enchantment> registry = serverLevel.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        
        int fireAspect = enchants.getLevel(registry.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT));
        
        boolean hasProtection = hasProtectionUpgrade();

        for (LivingEntity target : targets) {
            if (target instanceof Player && hasProtection) continue;
            
            float baseDamage = dev.manny.createmobgrinding.config.ModConfigs.COMMON.grinderBaseDamage.get().floatValue();
            float multiplier = 1.0f;
            if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.BRASS_GRINDER_BLADE.get())) multiplier = dev.manny.createmobgrinding.config.ModConfigs.COMMON.brassDamageMultiplier.get().floatValue();
            else if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.DIAMOND_GRINDER_BLADE.get())) multiplier = dev.manny.createmobgrinding.config.ModConfigs.COMMON.diamondDamageMultiplier.get().floatValue();
            else if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.NETHERITE_GRINDER_BLADE.get())) multiplier = dev.manny.createmobgrinding.config.ModConfigs.COMMON.netheriteDamageMultiplier.get().floatValue();
            else if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.CREATIVE_GRINDER_BLADE.get())) multiplier = 9999.0f;
            
            float damage = baseDamage * multiplier;
            
            if (fireAspect > 0) {
                target.igniteForSeconds(fireAspect * 4); // igniteForSeconds in 1.21
            }

            // The fake player keeps weaponToUse (with the blade's enchantments) in hand,
            // so looting and beheading read them when the mob dies.
            net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> grinderDamageKey = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(dev.manny.createmobgrinding.CreateMobGrinding.MOD_ID, "grinder"));
            net.minecraft.world.damagesource.DamageSource customSource = new net.minecraft.world.damagesource.DamageSource(serverLevel.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(grinderDamageKey), fakePlayer, fakePlayer);
            
            target.hurt(customSource, damage);
            if (target.isDeadOrDying()) {
                killCount++;
                setChanged();
                sendData();
            }

            // The grinder is not a valid revenge target. HurtByTargetGoal has no
            // distance limit, so without this the mob would lock onto the fake player
            // and try to reach it, walking out of the kill zone.
            // lastHurtByPlayer remains intact: drops, looting, and XP are unchanged.
            target.setLastHurtByMob(null);
            if (target instanceof net.minecraft.world.entity.Mob mob) {
                mob.setTarget(null);
            }
            if (target instanceof net.minecraft.world.entity.monster.Creeper creeper) {
                creeper.setSwellDir(-1);
            }
        }

        // The Minecraft fake player is shared with other mods: don't leave our weapon in its hand.
        fakePlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        
        ItemEnchantments enchants = getCombinedEnchantments();
        if (enchants.isEmpty()) {
            tooltip.add(Component.literal("    ").append(Component.translatable("jade.createmobgrinding.grinder.no_enchants")).withStyle(net.minecraft.ChatFormatting.GRAY));
        } else {
            enchants.keySet().forEach(ench -> {
                tooltip.add(Component.literal("    ").append(Component.translatable(ench.unwrapKey().get().location().toLanguageKey("enchantment")).append(" " + enchants.getLevel(ench))).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            });
        }
        
        boolean hasUpgrades = false;
        for (int i = 0; i < upgrades.getSlots(); i++) {
            if (!upgrades.getStackInSlot(i).isEmpty()) {
                hasUpgrades = true;
                break;
            }
        }
        
        if (hasUpgrades) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("    ").append(Component.translatable("tooltip.createmobgrinding.spawner.upgrades")).withStyle(net.minecraft.ChatFormatting.GRAY));
            for (int i = 0; i < upgrades.getSlots(); i++) {
                net.minecraft.world.item.ItemStack upgrade = upgrades.getStackInSlot(i);
                if (!upgrade.isEmpty()) {
                    tooltip.add(Component.literal("      - ").withStyle(net.minecraft.ChatFormatting.DARK_GRAY).append(upgrade.getHoverName().copy().withStyle(net.minecraft.ChatFormatting.GREEN)));
                }
            }
        }
        
        return true;
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("InternalWeapon")) {
            internalWeapon = ItemStack.parseOptional(registries, compound.getCompound("InternalWeapon"));
        }
        if (compound.contains("InstalledBlade")) {
            installedBlade = ItemStack.parseOptional(registries, compound.getCompound("InstalledBlade"));
        }
        
        // Migration: Move enchantments from internalWeapon to installedBlade
        ItemEnchantments legacyEnchants = internalWeapon.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!legacyEnchants.isEmpty()) {
            ItemEnchantments bladeEnchants = installedBlade.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(legacyEnchants);
            for (var ench : bladeEnchants.keySet()) {
                int current = mutable.getLevel(ench);
                int added = bladeEnchants.getLevel(ench);
                mutable.set(ench, Math.max(current, added));
            }
            
            EnchantmentHelper.setEnchantments(installedBlade, mutable.toImmutable());
            
            // Clear the internal weapon so we don't migrate again
            internalWeapon = new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
        }
        
        if (compound.contains("Upgrades")) {
            upgrades.deserializeNBT(registries, compound.getCompound("Upgrades"));
        }
        if (compound.contains("VacuumInventory")) {
            vacuumInventory.deserializeNBT(registries, compound.getCompound("VacuumInventory"));
        }
        attackTimer = compound.getFloat("AttackTimer");
        xpBuffer = compound.getInt("XpBuffer");
        killCount = compound.getLong("KillCount");
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.put("InternalWeapon", internalWeapon.saveOptional(registries));
        compound.put("InstalledBlade", installedBlade.saveOptional(registries));
        compound.put("Upgrades", upgrades.serializeNBT(registries));
        compound.put("VacuumInventory", vacuumInventory.serializeNBT(registries));
        compound.putFloat("AttackTimer", attackTimer);
        compound.putInt("XpBuffer", xpBuffer);
        compound.putLong("KillCount", killCount);
    }
}

