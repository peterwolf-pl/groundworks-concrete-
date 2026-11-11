package com.piotrek.groundworksconcrete.block;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class ConcreteBlocks {
    private static final Map<ConcreteGrade, WetConcreteBlock> WET =
            new EnumMap<>(ConcreteGrade.class);
    private static final Map<ConcreteGrade, HardenedConcreteBlock> SOLID =
            new EnumMap<>(ConcreteGrade.class);
    private static final Map<ConcreteGrade, Item> SOLID_ITEMS =
            new EnumMap<>(ConcreteGrade.class);
    public static Block FORMWORK;
    public static Item FORMWORK_ITEM;

    private ConcreteBlocks() {}

    public static void register() {
        for (ConcreteGrade grade : ConcreteGrade.values()) {
            String slug = grade.name().toLowerCase(Locale.ROOT);
            float hardness = switch (grade) {
                case B10 -> 1.5F;
                case B20 -> 2.2F;
                case B30 -> 3.0F;
            };
            String wetName = "wet_concrete_" + slug;
            ResourceKey<Block> wetKey = blockKey(wetName);
            WetConcreteBlock wet = Registry.register(BuiltInRegistries.BLOCK, wetKey,
                    new WetConcreteBlock(grade, BlockBehaviour.Properties.of()
                            .setId(wetKey).strength(0.3F).noOcclusion().noCollision()));
            WET.put(grade, wet);

            String solidName = "hardened_concrete_" + slug;
            ResourceKey<Block> solidKey = blockKey(solidName);
            HardenedConcreteBlock solid = Registry.register(BuiltInRegistries.BLOCK, solidKey,
                    new HardenedConcreteBlock(grade, BlockBehaviour.Properties.of()
                            .setId(solidKey).strength(hardness, hardness * 4F).noOcclusion()));
            SOLID.put(grade, solid);
            SOLID_ITEMS.put(grade, registerBlockItem(solidName, solid));
        }

        ResourceKey<Block> formKey = blockKey("concrete_formwork");
        FORMWORK = Registry.register(BuiltInRegistries.BLOCK, formKey,
                new Block(BlockBehaviour.Properties.of().setId(formKey).strength(1.25F)));
        FORMWORK_ITEM = registerBlockItem("concrete_formwork", FORMWORK);
    }

    private static ResourceKey<Block> blockKey(String path) {
        return ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID, path));
    }

    private static Item registerBlockItem(String path, Block block) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID, path));
        return Registry.register(BuiltInRegistries.ITEM, key,
                new BlockItem(block, new Item.Properties().setId(key)));
    }

    public static WetConcreteBlock wet(ConcreteGrade grade) {
        return WET.get(grade);
    }

    public static HardenedConcreteBlock solid(ConcreteGrade grade) {
        return SOLID.get(grade);
    }

    public static Item solidItem(ConcreteGrade grade) {
        return SOLID_ITEMS.get(grade);
    }
}
