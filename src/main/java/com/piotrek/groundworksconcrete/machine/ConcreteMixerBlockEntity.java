package com.piotrek.groundworksconcrete.machine;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import com.piotrek.groundworksconcrete.block.ConcreteBlockEntities;
import com.piotrek.groundworksconcrete.block.ConcreteMixerBlock;
import com.piotrek.groundworksconcrete.item.ConcreteItems;
import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import com.piotrek.groundworksconcrete.material.IngredientAmounts;
import com.piotrek.groundworksconcrete.world.ConcreteFlowWorld;
import com.piotrek.groundworksconcrete.world.ConcreteWorldApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Physical mixer with exact ingredient inventory and one 512-unit output batch.
 * UI is deliberately server-authoritative: right click adds ingredients, empty
 * hand operates, sneak + empty hand selects the grade.
 */
public final class ConcreteMixerBlockEntity extends BlockEntity {
    private ConcreteMixerProcess process = new ConcreteMixerProcess();

    public ConcreteMixerBlockEntity(BlockPos pos, BlockState state) {
        super(ConcreteBlockEntities.MIXER, pos, state);
    }

    public static boolean accepts(ItemStack stack) {
        return stack.is(ConcreteItems.CEMENT_BAG)
                || stack.is(Blocks.SAND.asItem())
                || stack.is(Blocks.GRAVEL.asItem())
                || stack.is(Items.WATER_BUCKET)
                || stack.is(ConcreteItems.DRY_MIX_B10)
                || stack.is(ConcreteItems.DRY_MIX_B20)
                || stack.is(ConcreteItems.DRY_MIX_B30);
    }

    public void acceptIngredient(Player player, InteractionHand hand, ItemStack held) {
        if (!(level instanceof ServerLevel server) || held.isEmpty()) return;
        boolean accepted;
        if (held.is(ConcreteItems.CEMENT_BAG)) {
            accepted = process.add(ConcreteMixerProcess.CEMENT_BAG);
        } else if (held.is(Blocks.SAND.asItem())) {
            accepted = process.add(ConcreteMixerProcess.SAND_BLOCK);
        } else if (held.is(Blocks.GRAVEL.asItem())) {
            accepted = process.add(ConcreteMixerProcess.GRAVEL_BLOCK);
        } else if (held.is(Items.WATER_BUCKET)) {
            accepted = process.add(ConcreteMixerProcess.WATER_BUCKET);
        } else if (held.is(ConcreteItems.DRY_MIX_B10)) {
            accepted = process.addDryMix(ConcreteGrade.B10);
        } else if (held.is(ConcreteItems.DRY_MIX_B20)) {
            accepted = process.addDryMix(ConcreteGrade.B20);
        } else if (held.is(ConcreteItems.DRY_MIX_B30)) {
            accepted = process.addDryMix(ConcreteGrade.B30);
        } else {
            return;
        }

        if (!accepted) {
            player.sendOverlayMessage(Component.literal("Concrete mixer: wrong mix grade or hopper full"));
            return;
        }

        if (!player.getAbilities().instabuild) {
            boolean waterBucket = held.is(Items.WATER_BUCKET);
            held.shrink(1);
            if (waterBucket) {
                ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                if (held.isEmpty()) {
                    player.setItemInHand(hand, emptyBucket);
                } else {
                    player.getInventory().add(emptyBucket);
                }
            }
        }
        syncVisual(false);
        server.playSound(null, worldPosition, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.6F, 1.0F);
        sendStatus(player);
    }

    public void cycleGrade(Player player) {
        process.cycleGrade();
        syncVisual(false);
        sendStatus(player);
    }

    public void operate(Player player) {
        if (!(level instanceof ServerLevel server)) return;
        if (process.ready()) {
            BlockState state = getBlockState();
            BlockPos outlet = worldPosition.relative(state.getValue(ConcreteMixerBlock.FACING));
            ConcreteBatch output = process.output();
            ConcreteFlowWorld.PourResult result = ConcreteWorldApi.pour(server, outlet, output);
            if (result.acceptedUnits() > 0) {
                process.dischargeAccepted(result.acceptedUnits());
                syncVisual(false);
                server.playSound(null, worldPosition, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.9F, 0.75F);
            } else {
                player.sendOverlayMessage(Component.literal("Mixer outlet obstructed. Clear space in front."));
            }
            sendStatus(player);
            return;
        }
        if (process.mixing()) {
            sendStatus(player);
            return;
        }
        ConcreteMixerProcess.StartResult outcome = process.start();
        if (outcome == ConcreteMixerProcess.StartResult.STARTED) {
            syncVisual(true);
            server.playSound(null, worldPosition, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.7F, 0.7F);
        } else if (outcome == ConcreteMixerProcess.StartResult.INSUFFICIENT_INGREDIENTS) {
            player.sendOverlayMessage(Component.literal("Mixer: add cement, sand, gravel and water."));
        }
        sendStatus(player);
    }

    private void sendStatus(Player player) {
        IngredientAmounts in = process.inventory();
        int output = process.output() == null ? 0 : process.output().volumeUnits();
        player.sendOverlayMessage(Component.literal(String.format(Locale.ROOT,
                "Mixer %s | cement %.1f kg, sand %.1f kg, gravel %.1f kg, water %.1f L | %s | output %d/512",
                process.selectedGrade().name(),
                in.cementGrams() / 1000.0, in.sandGrams() / 1000.0,
                in.gravelGrams() / 1000.0, in.waterMillilitres() / 1000.0,
                process.mixing() ? "mixing " + (ConcreteMixerProcess.MIX_TICKS - process.remainingTicks()) + "/200"
                        : process.ready() ? "ready" : "idle", output)));
    }

    public static void tick(Level level, BlockPos pos, BlockState state,
                            ConcreteMixerBlockEntity machine) {
        if (!(level instanceof ServerLevel) || !machine.process.mixing()) return;
        boolean completed = machine.process.tick();
        int elapsed = ConcreteMixerProcess.MIX_TICKS - machine.process.remainingTicks();
        if (completed || elapsed % 5 == 0) {
            machine.syncVisual(!completed);
        } else if (elapsed % 20 == 0) {
            machine.setChanged();
        }
        if (completed && level instanceof ServerLevel server) {
            server.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.8F, 0.9F);
        }
    }

    private void syncVisual(boolean active) {
        if (level == null) return;
        setChanged();
        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof ConcreteMixerBlock)) return;
        int elapsed = ConcreteMixerProcess.MIX_TICKS - process.remainingTicks();
        int spin = active ? Math.floorMod(elapsed / 5, 4) : 0;
        BlockState next = state.setValue(ConcreteMixerBlock.RUNNING, active)
                .setValue(ConcreteMixerBlock.SPIN, spin);
        if (!state.equals(next)) level.setBlock(worldPosition, next, 3);
        if (level instanceof ServerLevel server) {
            server.sendBlockUpdated(worldPosition, next, next, 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        IngredientAmounts in = process.inventory();
        output.putString("Grade", process.selectedGrade().name());
        output.putLong("CementGrams", in.cementGrams());
        output.putLong("SandGrams", in.sandGrams());
        output.putLong("GravelGrams", in.gravelGrams());
        output.putLong("WaterMl", in.waterMillilitres());
        output.putInt("RemainingTicks", process.remainingTicks());
        if (process.output() != null) {
            ConcreteBatch batch = process.output();
            IngredientAmounts a = batch.ingredients();
            output.putBoolean("HasOutput", true);
            output.putString("OutputGrade", batch.grade().name());
            output.putInt("OutputUnits", batch.volumeUnits());
            output.putLong("OutputCement", a.cementGrams());
            output.putLong("OutputSand", a.sandGrams());
            output.putLong("OutputGravel", a.gravelGrams());
            output.putLong("OutputWater", a.waterMillilitres());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        try {
            ConcreteGrade selected = ConcreteGrade.parse(input.getStringOr("Grade", "B10"));
            IngredientAmounts storage = new IngredientAmounts(
                    input.getLongOr("CementGrams", 0L), input.getLongOr("SandGrams", 0L),
                    input.getLongOr("GravelGrams", 0L), input.getLongOr("WaterMl", 0L));
            ConcreteBatch batch = null;
            if (input.getBooleanOr("HasOutput", false)) {
                batch = new ConcreteBatch(
                        ConcreteGrade.parse(input.getStringOr("OutputGrade", "B10")),
                        input.getIntOr("OutputUnits", 0),
                        new IngredientAmounts(input.getLongOr("OutputCement", 0L),
                                input.getLongOr("OutputSand", 0L),
                                input.getLongOr("OutputGravel", 0L),
                                input.getLongOr("OutputWater", 0L)),
                        ConcreteBatch.Phase.FRESH);
            }
            process = ConcreteMixerProcess.restore(selected, storage, batch,
                    input.getIntOr("RemainingTicks", 0));
        } catch (RuntimeException invalid) {
            GroundworksConcreteMod.LOGGER.warn("Invalid stationary mixer data at {}: {}",
                    worldPosition, invalid.getMessage());
            process = new ConcreteMixerProcess();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
