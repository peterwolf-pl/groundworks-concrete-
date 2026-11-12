package com.piotrek.groundworksconcrete.item;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import com.piotrek.groundworksconcrete.block.ConcreteBlocks;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import com.piotrek.groundworksconcrete.vehicle.ConcreteTruckRegistry;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public final class ConcreteItems {
    public static Item CEMENT_BAG;
    public static Item DRY_MIX_B10;
    public static Item DRY_MIX_B20;
    public static Item DRY_MIX_B30;

    private ConcreteItems() {}

    private static Item registerItem(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(BuiltInRegistries.ITEM, key,
                new Item(new Item.Properties().setId(key)));
    }

    public static void register() {
        CEMENT_BAG = registerItem("cement_bag");
        DRY_MIX_B10 = registerItem("dry_mix_b10");
        DRY_MIX_B20 = registerItem("dry_mix_b20");
        DRY_MIX_B30 = registerItem("dry_mix_b30");

        CreativeModeTab tab = FabricCreativeModeTab.builder()
                .title(Component.translatable("itemGroup.pw_groundworks_concrete.main"))
                .icon(CEMENT_BAG::getDefaultInstance)
                .displayItems((parameters, output) -> {
                    output.accept(CEMENT_BAG);
                    output.accept(DRY_MIX_B10);
                    output.accept(DRY_MIX_B20);
                    output.accept(DRY_MIX_B30);
                    output.accept(ConcreteBlocks.FORMWORK_ITEM);
                    output.accept(ConcreteBlocks.MIXER_ITEM);
                    output.accept(ConcreteTruckRegistry.MIXER_TRUCK_ITEM);
                    for (ConcreteGrade grade : ConcreteGrade.values()) {
                        output.accept(ConcreteBlocks.solidItem(grade));
                    }
                }).build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID, "main"), tab);
    }
}
