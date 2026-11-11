package com.piotrek.groundworksconcrete.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import com.piotrek.groundworksconcrete.block.ConcreteBlocks;
import com.piotrek.groundworksconcrete.block.ConcreteLayerBlock;
import com.piotrek.groundworksconcrete.block.WetConcreteBlock;
import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import com.piotrek.groundworksconcrete.material.IngredientAmounts;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Dimension-local finite-volume concrete engine.
 *
 * All state is saved through the 26.3 SavedData Codec, exactly like Groundworks.
 * Active flow work uses a bounded priority queue rather than a block-entity
 * ticker per cell. Curing uses world game time and resumes across reloads.
 */
public final class ConcreteFlowWorld extends SavedData {
    private static final int FLOW_INTERVAL_TICKS = 8;
    private static final int MAX_CELLS_PER_TICK = 128;
    private static final int UNLOADED_RETRY_TICKS = 200;
    private static final int MAX_POUR_STACK = 64;
    private static final int MAX_POUR_UNITS = MAX_POUR_STACK * ConcreteBatch.UNITS_PER_CUBIC_METRE;

    public static final Identifier TYPE_ID = Identifier.fromNamespaceAndPath(
            GroundworksConcreteMod.MOD_ID, "world_concrete");

    public record StoredCell(long pos, CompoundTag tag) {
        public static final Codec<StoredCell> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.LONG.fieldOf("pos").forGetter(StoredCell::pos),
                        CompoundTag.CODEC.fieldOf("tag").forGetter(StoredCell::tag)
                ).apply(instance, StoredCell::new));
    }

    public static final Codec<ConcreteFlowWorld> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.list(StoredCell.CODEC)
                    .fieldOf("cells").forGetter(ConcreteFlowWorld::toEntries))
                    .apply(instance, ConcreteFlowWorld::fromEntries));

    public static final SavedDataType<ConcreteFlowWorld> TYPE = new SavedDataType<>(
            TYPE_ID,
            ConcreteFlowWorld::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<Long, ConcreteFlowCell> cells = new HashMap<>();
    private final PriorityQueue<Scheduled> work = new PriorityQueue<>(
            Comparator.comparingLong(Scheduled::when));
    private final Map<Long, Long> scheduledAt = new HashMap<>();
    private boolean initialized;

    private record Scheduled(long pos, long when) {}

    public ConcreteFlowWorld() {}

    public static ConcreteFlowWorld get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private List<StoredCell> toEntries() {
        List<StoredCell> result = new ArrayList<>(cells.size());
        for (var entry : cells.entrySet()) {
            ConcreteFlowCell cell = entry.getValue();
            CompoundTag tag = new CompoundTag();
            tag.putInt("version", 1);
            tag.putString("grade", cell.batch().grade().name());
            tag.putInt("units", cell.units());
            tag.putLong("pouredAt", cell.pouredAt());
            IngredientAmounts a = cell.batch().ingredients();
            tag.putLong("cementGrams", a.cementGrams());
            tag.putLong("sandGrams", a.sandGrams());
            tag.putLong("gravelGrams", a.gravelGrams());
            tag.putLong("waterMl", a.waterMillilitres());
            result.add(new StoredCell(entry.getKey(), tag));
        }
        return result;
    }

    private static ConcreteFlowWorld fromEntries(List<StoredCell> entries) {
        ConcreteFlowWorld loaded = new ConcreteFlowWorld();
        for (StoredCell entry : entries) {
            try {
                CompoundTag tag = entry.tag();
                if (tag.getIntOr("version", 0) != 1) continue;
                ConcreteGrade grade = ConcreteGrade.parse(tag.getStringOr("grade", ""));
                int units = tag.getIntOr("units", 0);
                IngredientAmounts amounts = new IngredientAmounts(
                        tag.getLongOr("cementGrams", 0L),
                        tag.getLongOr("sandGrams", 0L),
                        tag.getLongOr("gravelGrams", 0L),
                        tag.getLongOr("waterMl", 0L));
                ConcreteBatch batch = new ConcreteBatch(grade, units, amounts,
                        ConcreteBatch.Phase.FRESH);
                loaded.cells.put(entry.pos(), new ConcreteFlowCell(batch,
                        tag.getLongOr("pouredAt", 0L)));
            } catch (IllegalArgumentException | ArithmeticException invalid) {
                GroundworksConcreteMod.LOGGER.warn(
                        "Skipping malformed concrete cell {}: {}", entry.pos(), invalid.getMessage());
            }
        }
        return loaded;
    }

    public record PourResult(int acceptedUnits, @Nullable ConcreteBatch remainder) {
        public int rejectedUnits() {
            return remainder == null ? 0 : remainder.volumeUnits();
        }
    }

    /** Only mutates loaded blocks; never loads chunks or destroys obstacles. */
    public PourResult pour(ServerLevel level, BlockPos origin, ConcreteBatch batch) {
        Objects.requireNonNull(batch, "batch");
        if (batch.phase() != ConcreteBatch.Phase.FRESH) {
            throw new IllegalArgumentException("Only fresh concrete can be poured");
        }
        if (batch.volumeUnits() > MAX_POUR_UNITS) {
            throw new IllegalArgumentException("One pour exceeds 64 blocks");
        }

        ConcreteBatch remaining = batch;
        int accepted = 0;
        for (int i = 0; i < MAX_POUR_STACK && remaining != null; i++) {
            BlockPos pos = origin.above(i);
            if (!canReceive(level, pos, batch.grade())) break;
            ConcreteFlowCell current = cells.get(pos.asLong());
            int capacity = ConcreteBatch.UNITS_PER_CUBIC_METRE
                    - (current == null ? 0 : current.units());
            if (capacity <= 0) continue;

            ConcreteBatch.Split split = remaining.split(Math.min(capacity, remaining.volumeUnits()));
            ConcreteFlowCell incoming = new ConcreteFlowCell(split.taken(), level.getGameTime());
            ConcreteFlowCell updated = current == null ? incoming : current.combine(incoming);
            setCell(level, pos, updated);
            accepted += split.taken().volumeUnits();
            remaining = split.remaining();
        }
        return new PourResult(accepted, remaining);
    }

    @Nullable
    public ConcreteFlowCell getCell(BlockPos pos) {
        return cells.get(pos.asLong());
    }

    public int activeCellCount() {
        return cells.size();
    }

    public long activeUnits() {
        long total = 0;
        for (ConcreteFlowCell c : cells.values()) total += c.units();
        return total;
    }

    private void schedule(long pos, long when) {
        if (!cells.containsKey(pos)) return;
        Long previous = scheduledAt.get(pos);
        if (previous != null && previous <= when) return;
        scheduledAt.put(pos, when);
        work.add(new Scheduled(pos, when));
    }

    private void scheduleNeighborhood(BlockPos pos, long at) {
        schedule(pos.asLong(), at);
        schedule(pos.above().asLong(), at);
        schedule(pos.north().asLong(), at);
        schedule(pos.south().asLong(), at);
        schedule(pos.west().asLong(), at);
        schedule(pos.east().asLong(), at);
    }

    private void setCell(ServerLevel level, BlockPos pos, @Nullable ConcreteFlowCell cell) {
        long key = pos.asLong();
        if (cell == null) {
            if (cells.remove(key) != null) setDirty();
            scheduledAt.remove(key);
            if (level.getBlockState(pos).getBlock() instanceof WetConcreteBlock) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
            // When the source empties, wake concrete immediately above and beside it.
            scheduleNeighborhood(pos, level.getGameTime() + FLOW_INTERVAL_TICKS);
        } else {
            cells.put(key, cell);
            setDirty();
            refreshBlock(level, pos, cell);
            scheduleNeighborhood(pos, level.getGameTime() + FLOW_INTERVAL_TICKS);
        }
    }

    private static int layer(int units) {
        return Math.max(1, Math.min(8, (units + 63) / 64));
    }

    private static void refreshBlock(ServerLevel level, BlockPos pos, ConcreteFlowCell cell) {
        int ageTone = ConcreteCuring.tone(ConcreteCuring.elapsed(
                level.getGameTime(), cell.pouredAt()));
        BlockState desired = ConcreteBlocks.wet(cell.batch().grade())
                .defaultBlockState()
                .setValue(ConcreteLayerBlock.LEVEL, layer(cell.units()))
                .setValue(WetConcreteBlock.TONE, ageTone);
        if (!level.getBlockState(pos).equals(desired)) level.setBlock(pos, desired, 3);
    }

    private boolean canReceive(ServerLevel level, BlockPos pos, ConcreteGrade grade) {
        if (!level.hasChunkAt(pos)) return false;
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            // A player may remove wet concrete between scheduled simulation ticks.
            // Remove stale metadata before accepting a new pour into this air cell.
            if (cells.remove(pos.asLong()) != null) {
                scheduledAt.remove(pos.asLong());
                setDirty();
            }
            return true;
        }
        if (state.getBlock() instanceof WetConcreteBlock wet && wet.grade() == grade) {
            ConcreteFlowCell existing = cells.get(pos.asLong());
            return existing != null && existing.batch().grade() == grade;
        }
        // Wooden formwork, barriers, hardened concrete, fluids and Groundworks
        // granular terrain are all impenetrable to fresh concrete.
        return false;
    }

    /** Called once per server tick from the shared Fabric server callback. */
    public void tick(ServerLevel level) {
        long now = level.getGameTime();
        if (!initialized) {
            initialized = true;
            for (Long key : cells.keySet()) schedule(key, now);
        }

        int processed = 0;
        while (!work.isEmpty() && processed < MAX_CELLS_PER_TICK) {
            Scheduled task = work.peek();
            if (task.when() > now) break;
            work.poll();
            if (!Objects.equals(scheduledAt.get(task.pos()), task.when())) continue;
            scheduledAt.remove(task.pos());
            ConcreteFlowCell cell = cells.get(task.pos());
            if (cell == null) continue;
            BlockPos pos = BlockPos.of(task.pos());

            if (!level.hasChunkAt(pos)) {
                schedule(task.pos(), now + UNLOADED_RETRY_TICKS);
                continue;
            }

            if (!(level.getBlockState(pos).getBlock() instanceof WetConcreteBlock)) {
                cells.remove(task.pos());
                setDirty();
                processed++;
                continue;
            }

            long age = ConcreteCuring.elapsed(now, cell.pouredAt());
            if (ConcreteCuring.isHardened(age)) {
                BlockState solid = ConcreteBlocks.solid(cell.batch().grade())
                        .defaultBlockState().setValue(ConcreteLayerBlock.LEVEL, layer(cell.units()));
                cells.remove(task.pos());
                setDirty();
                level.setBlock(pos, solid, 3);
                scheduleNeighborhood(pos, now + FLOW_INTERVAL_TICKS);
                processed++;
                continue;
            }

            boolean moved = false;
            if (ConcreteCuring.mayFlow(age)) {
                ConcreteFlowPhysics.Grid grid = new ConcreteFlowPhysics.Grid() {
                    private BlockPos block(ConcreteFlowPhysics.Position p) {
                        return new BlockPos(p.x(), p.y(), p.z());
                    }

                    @Override
                    public ConcreteFlowCell get(ConcreteFlowPhysics.Position p) {
                        BlockPos at = block(p);
                        ConcreteFlowCell stored = cells.get(at.asLong());
                        if (stored == null || !level.hasChunkAt(at)) return null;
                        if (!(level.getBlockState(at).getBlock() instanceof WetConcreteBlock)) {
                            return null;
                        }
                        return stored;
                    }

                    @Override
                    public boolean canAccept(ConcreteFlowPhysics.Position p) {
                        return canReceive(level, block(p), cell.batch().grade());
                    }

                    @Override
                    public void put(ConcreteFlowPhysics.Position p, ConcreteFlowCell value) {
                        setCell(level, block(p), value);
                    }
                };
                moved = ConcreteFlowPhysics.step(grid,
                        new ConcreteFlowPhysics.Position(pos.getX(), pos.getY(), pos.getZ()),
                        now);
            }

            ConcreteFlowCell updated = cells.get(task.pos());
            if (updated != null) {
                refreshBlock(level, pos, updated);
                long next = moved && ConcreteCuring.mayFlow(ConcreteCuring.elapsed(now, updated.pouredAt()))
                        ? now + FLOW_INTERVAL_TICKS
                        : ConcreteCuring.nextBoundary(updated.pouredAt(), now);
                schedule(task.pos(), next);
            }
            processed++;
        }
    }
}
