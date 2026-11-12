package com.piotrek.groundworksconcrete.machine;

import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import com.piotrek.groundworksconcrete.material.IngredientAmounts;

import java.util.Objects;

/**
 * Pure server-side process model. No energy or material is created by ticking.
 * One mixing cycle outputs exactly 512 Groundworks units (1 nominal m3).
 */
public final class ConcreteMixerProcess {
    public static final int MIX_TICKS = 200;
    public static final int BATCH_UNITS = ConcreteBatch.UNITS_PER_CUBIC_METRE;
    public static final int DRY_MIX_ITEM_UNITS = 64;

    // 1 water bucket is 1 full Minecraft cubic metre = 1,000 litres.
    public static final IngredientAmounts CEMENT_BAG = new IngredientAmounts(25_000, 0, 0, 0);
    public static final IngredientAmounts SAND_BLOCK = new IngredientAmounts(0, 750_000, 0, 0);
    public static final IngredientAmounts GRAVEL_BLOCK = new IngredientAmounts(0, 0, 1_150_000, 0);
    public static final IngredientAmounts WATER_BUCKET = new IngredientAmounts(0, 0, 0, 1_000_000);

    public static final IngredientAmounts MAX_STORAGE =
            new IngredientAmounts(4_000_000, 10_000_000, 12_000_000, 4_000_000);

    public enum StartResult { STARTED, BUSY, OUTPUT_NOT_EMPTY, INSUFFICIENT_INGREDIENTS }
    private ConcreteGrade selectedGrade = ConcreteGrade.B10;
    private IngredientAmounts inventory = IngredientAmounts.empty();
    private ConcreteBatch output;
    private int remainingTicks;

    public ConcreteGrade selectedGrade() { return selectedGrade; }
    public IngredientAmounts inventory() { return inventory; }
    public ConcreteBatch output() { return output; }
    public int remainingTicks() { return remainingTicks; }
    public boolean mixing() { return remainingTicks > 0; }
    public boolean ready() { return output != null && remainingTicks == 0; }

    /** Change grade without altering the current output batch. */
    public boolean choose(ConcreteGrade grade) {
        if (mixing()) return false;
        selectedGrade = Objects.requireNonNull(grade);
        return true;
    }

    public void cycleGrade() {
        if (mixing()) return;
        ConcreteGrade[] all = ConcreteGrade.values();
        selectedGrade = all[(selectedGrade.ordinal() + 1) % all.length];
    }

    /** Reject whole item when its entire payload does not fit the hopper. */
    public boolean add(IngredientAmounts ingredient) {
        if (ingredient == null) return false;
        IngredientAmounts next;
        try {
            next = inventory.plus(ingredient);
        } catch (ArithmeticException e) {
            return false;
        }
        if (next.cementGrams() > MAX_STORAGE.cementGrams()
                || next.sandGrams() > MAX_STORAGE.sandGrams()
                || next.gravelGrams() > MAX_STORAGE.gravelGrams()
                || next.waterMillilitres() > MAX_STORAGE.waterMillilitres()) {
            return false;
        }
        inventory = next;
        return true;
    }

    /** Existing dry-mix item is equivalent to 1/8 m3 of dry ingredients. */
    public boolean addDryMix(ConcreteGrade grade) {
        if (grade != selectedGrade) return false;
        return add(ConcreteBatch.dry(grade, DRY_MIX_ITEM_UNITS).ingredients());
    }

    public boolean canStart() {
        IngredientAmounts needed = ConcreteBatch.fresh(selectedGrade, BATCH_UNITS, 1.0)
                .ingredients();
        return inventory.cementGrams() >= needed.cementGrams()
                && inventory.sandGrams() >= needed.sandGrams()
                && inventory.gravelGrams() >= needed.gravelGrams()
                && inventory.waterMillilitres() >= needed.waterMillilitres();
    }

    /** Subtract each ingredient atomically before beginning a cycle. */
    public StartResult start() {
        if (mixing()) return StartResult.BUSY;
        if (output != null) return StartResult.OUTPUT_NOT_EMPTY;
        if (!canStart()) return StartResult.INSUFFICIENT_INGREDIENTS;
        ConcreteBatch batch = ConcreteBatch.fresh(selectedGrade, BATCH_UNITS, 1.0);
        inventory = inventory.minus(batch.ingredients());
        output = batch;
        remainingTicks = MIX_TICKS;
        return StartResult.STARTED;
    }

    /** @return true if cycle became ready on this tick */
    public boolean tick() {
        if (remainingTicks <= 0) return false;
        remainingTicks--;
        return remainingTicks == 0;
    }

    /** Called only after ConcreteWorldApi.pour accepted the given exact volume. */
    public void dischargeAccepted(int units) {
        if (!ready() || units < 1 || units > output.volumeUnits()) {
            throw new IllegalArgumentException("Invalid discharge quantity");
        }
        output = output.split(units).remaining();
    }

    /** Restore inventory and pending output from trusted, validated persistent state. */
    public static ConcreteMixerProcess restore(ConcreteGrade selected,
                                               IngredientAmounts inventory,
                                               ConcreteBatch output, int remainingTicks) {
        ConcreteMixerProcess machine = new ConcreteMixerProcess();
        machine.choose(Objects.requireNonNull(selected));
        if (inventory == null || !machine.add(inventory)) {
            throw new IllegalArgumentException("Invalid stored ingredients");
        }
        if (remainingTicks < 0 || remainingTicks > MIX_TICKS || (remainingTicks > 0 && output == null)) {
            throw new IllegalArgumentException("Invalid mixing time");
        }
        if (output != null && (output.phase() != ConcreteBatch.Phase.FRESH
                || output.volumeUnits() > BATCH_UNITS)) {
            throw new IllegalArgumentException("Invalid stored batch");
        }
        machine.output = output;
        machine.remainingTicks = remainingTicks;
        return machine;
    }
}
