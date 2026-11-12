package com.piotrek.groundworksconcrete.vehicle;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import com.piotrek.groundworksconcrete.item.MixerTruckItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

/** Independent registration, not coupled to the Dump Truck submod. */
public final class ConcreteTruckRegistry {
    public static EntityType<MixerTruckEntity> MIXER_TRUCK;
    public static Item MIXER_TRUCK_ITEM;
    private ConcreteTruckRegistry() {}

    public static void register() {
        Identifier id = Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID, "mixer_truck");
        ResourceKey<EntityType<?>> entityKey = ResourceKey.create(Registries.ENTITY_TYPE, id);
        MIXER_TRUCK = Registry.register(BuiltInRegistries.ENTITY_TYPE, entityKey,
                EntityType.Builder.of(MixerTruckEntity::new, MobCategory.MISC)
                        .sized(3.35F, 3.25F)
                        .clientTrackingRange(14)
                        .build(entityKey));
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        MIXER_TRUCK_ITEM = Registry.register(BuiltInRegistries.ITEM, itemKey,
                new MixerTruckItem(new Item.Properties().setId(itemKey).stacksTo(1)));
    }
}
