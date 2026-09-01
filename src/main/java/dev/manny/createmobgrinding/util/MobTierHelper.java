package dev.manny.createmobgrinding.util;

import dev.manny.createmobgrinding.config.ModConfigs;
import dev.manny.createmobgrinding.registry.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class MobTierHelper {
    
    public static int getMobTier(EntityType<?> type) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (id == null) return -1;
        
        String idStr = id.toString();
        
        // 1. Check dynamic config first (so it can override tags if needed)
        List<? extends String> customTiers = ModConfigs.COMMON.customTiers.get();
        for (String entry : customTiers) {
            if (entry.startsWith(idStr + "=")) {
                try {
                    return Integer.parseInt(entry.split("=")[1]);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        
        // 2. Fallback to tags
        if (type.is(ModTags.TIER_5)) return 5;
        if (type.is(ModTags.TIER_4)) return 4;
        if (type.is(ModTags.TIER_3)) return 3;
        if (type.is(ModTags.TIER_2)) return 2;
        if (type.is(ModTags.TIER_1)) return 1;
        
        // Default tier if not tagged and not 1-4
        // Usually most standard mobs are tier 1 if not defined, but some might be unknown.
        // The previous logic defaulted to 1 for anything not matching 2-5.
        // For WAILA, we might want to know if it's explicitly defined or just fallback.
        // But for now, we'll retain the existing fallback to 1.
        return 1;
    }
    
    public static boolean hasExplicitTier(EntityType<?> type) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (id == null) return false;
        
        String idStr = id.toString();
        
        List<? extends String> customTiers = ModConfigs.COMMON.customTiers.get();
        for (String entry : customTiers) {
            if (entry.startsWith(idStr + "=")) {
                return true;
            }
        }
        
        return type.is(ModTags.TIER_5) || 
               type.is(ModTags.TIER_4) || 
               type.is(ModTags.TIER_3) || 
               type.is(ModTags.TIER_2) || 
               type.is(ModTags.TIER_1);
    }
}
