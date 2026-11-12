package com.piotrek.groundworksconcrete.block;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import com.piotrek.groundworksconcrete.machine.ConcreteMixerBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Registry must initialize after ConcreteBlocks.register(). */
public final class ConcreteBlockEntities {
    public static BlockEntityType<ConcreteMixerBlockEntity> MIXER;

    private ConcreteBlockEntities() {}

    public static void register() {
        Identifier id = Identifier.fromNamespaceAndPath(
                GroundworksConcreteMod.MOD_ID, "stationary_concrete_mixer");
        ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id);
        MIXER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key,
                FabricBlockEntityTypeBuilder.create(
                        ConcreteMixerBlockEntity::new, ConcreteBlocks.MIXER).build());
    }
}
