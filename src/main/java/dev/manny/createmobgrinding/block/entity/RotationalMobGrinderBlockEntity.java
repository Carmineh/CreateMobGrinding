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
    private static final float ATTACK_THRESHOLD = 2000f; // Attacks in 0.39s at 256 RPM
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
        }
    };
    
    private int xpBuffer = 0;

    public RotationalMobGrinderBlockEntity(BlockPos pos, BlockState state) {
        super(dev.manny.createmobgrinding.registry.ModBlockEntities.ROTATIONAL_MOB_GRINDER.get(), pos, state);
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

    public boolean applyEnchantedBook(ItemStack book) {
        if (!book.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) return false;
        
        ItemEnchantments bookEnchants = book.getOrDefault(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (bookEnchants.isEmpty()) return false;
        
        ItemEnchantments existingEnchants = internalWeapon.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(existingEnchants);
        boolean appliedAny = false;
        
        for (var ench : bookEnchants.keySet()) {
            net.minecraft.resources.ResourceLocation loc = ench.unwrapKey().get().location();
            if (loc.equals(net.minecraft.resources.ResourceLocation.withDefaultNamespace("looting")) ||
                loc.equals(net.minecraft.resources.ResourceLocation.withDefaultNamespace("fire_aspect")) ||
                loc.equals(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(dev.manny.createmobgrinding.CreateMobGrinding.MOD_ID, "beheading"))) {
                
                int current = mutable.getLevel(ench);
                int added = bookEnchants.getLevel(ench);
                int vanillaMax = ench.value().getMaxLevel();
                
                int newLevel;
                if (current == added) newLevel = Math.min(vanillaMax, current + 1);
                else newLevel = Math.min(vanillaMax, Math.max(current, added));
                
                if (newLevel > current) {
                    mutable.set(ench, newLevel);
                    appliedAny = true;
                }
            }
        }
        
        if (!appliedAny) return false;
        
        EnchantmentHelper.setEnchantments(internalWeapon, mutable.toImmutable());
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        if (hasNetwork()) getOrCreateNetwork().updateNetwork();
        return true;
    }
    
    public ItemStack extractEnchantments() {
        ItemEnchantments enchants = internalWeapon.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchants.isEmpty()) return ItemStack.EMPTY;
        
        ItemStack book = new ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK);
        book.set(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, enchants);
        
        internalWeapon = new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        if (hasNetwork()) getOrCreateNetwork().updateNetwork();
        
        return book;
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
        float impact = 8.0f; // Base impact for Iron blade
        if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.BRASS_GRINDER_BLADE.get())) impact = 16.0f;
        else if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.DIAMOND_GRINDER_BLADE.get())) impact = 32.0f;
        else if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.NETHERITE_GRINDER_BLADE.get())) impact = 64.0f;

        // Add impact for utility enchantments
        ItemEnchantments enchants = getCombinedEnchantments();
        if (!enchants.isEmpty()) {
            int totalLevels = enchants.keySet().stream().mapToInt(enchants::getLevel).sum();
            impact += totalLevels * 4.0f; // Increase stress based on enchantments
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

        if (attackTimer >= ATTACK_THRESHOLD) {
            attackTimer -= ATTACK_THRESHOLD;
            performAttack();
        }
    }
    
    private void vacuumItems() {
        ServerLevel serverLevel = (ServerLevel) level;
        net.minecraft.core.Direction facing = getBlockState().hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING) ? 
                getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING) : 
                net.minecraft.core.Direction.NORTH;
                
        BlockPos targetPos = worldPosition.relative(facing);
        // Vacuum area: 3x3 area in front of the blade
        AABB vacuumZone = new AABB(targetPos).inflate(1.0);
        
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
            ItemStack nuggetStack = new ItemStack(BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create", "experience_nugget")), nuggetsToProduce);
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
            
            float baseDamage = 2.0f; // Reduced for balance
            float multiplier = 1.0f;
            if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.BRASS_GRINDER_BLADE.get())) multiplier = 2.0f;
            else if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.DIAMOND_GRINDER_BLADE.get())) multiplier = 4.0f;
            else if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.NETHERITE_GRINDER_BLADE.get())) multiplier = 8.0f;
            else if (installedBlade.is(dev.manny.createmobgrinding.registry.ModItems.CREATIVE_GRINDER_BLADE.get())) multiplier = 9999.0f;
            
            float damage = baseDamage * multiplier;
            
            if (fireAspect > 0) {
                target.igniteForSeconds(fireAspect * 4); // igniteForSeconds in 1.21
            }
            
            target.hurt(serverLevel.damageSources().playerAttack(fakePlayer), damage);

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
        if (compound.contains("Upgrades")) {
            upgrades.deserializeNBT(registries, compound.getCompound("Upgrades"));
        }
        if (compound.contains("VacuumInventory")) {
            vacuumInventory.deserializeNBT(registries, compound.getCompound("VacuumInventory"));
        }
        attackTimer = compound.getFloat("AttackTimer");
        xpBuffer = compound.getInt("XpBuffer");
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
    }
}

