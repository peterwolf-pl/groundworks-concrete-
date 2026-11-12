package com.piotrek.groundworksconcrete.vehicle;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteBatchTank;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import com.piotrek.groundworksconcrete.material.IngredientAmounts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Four-axle concrete mixer truck. Authoritative on the server.
 * Stage 4: road movement, storage, drum mixing rotation, lights and horn.
 * Reverse discharge and articulated chute intentionally deferred to Stage 5.
 */
public final class MixerTruckEntity extends Entity {
    public static final int CAPACITY_UNITS = 8 * ConcreteBatch.UNITS_PER_CUBIC_METRE;

    private static final EntityDataAccessor<Float> SPEED =
            SynchedEntityData.defineId(MixerTruckEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STEER =
            SynchedEntityData.defineId(MixerTruckEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WHEELS =
            SynchedEntityData.defineId(MixerTruckEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DRUM_ANGLE =
            SynchedEntityData.defineId(MixerTruckEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DRUM_RPM =
            SynchedEntityData.defineId(MixerTruckEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DRUM_ACTIVE =
            SynchedEntityData.defineId(MixerTruckEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LIGHTS =
            SynchedEntityData.defineId(MixerTruckEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BRAKING =
            SynchedEntityData.defineId(MixerTruckEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CARGO_UNITS =
            SynchedEntityData.defineId(MixerTruckEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CARGO_GRADE =
            SynchedEntityData.defineId(MixerTruckEntity.class, EntityDataSerializers.INT);

    private final MixerTruckDrive drive = new MixerTruckDrive();
    private final MixerTruckDrum drum = new MixerTruckDrum();
    private final ConcreteBatchTank tank = new ConcreteBatchTank(CAPACITY_UNITS);

    private float throttle;
    private float steer;
    private int freshInputTicks;
    private long lastHornTick = Long.MIN_VALUE / 2L;
    private long lastSwitchTick = Long.MIN_VALUE / 2L;

    public MixerTruckEntity(EntityType<?> type, Level level) { super(type, level); }

    @Override
    protected InterpolationHandler createInterpolationHandler() {
        return LinearInterpolationHandler.create(this, 3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPEED,0F);
        builder.define(STEER,0F);
        builder.define(WHEELS,0F);
        builder.define(DRUM_ANGLE,0F);
        builder.define(DRUM_RPM,MixerTruckDrum.DEFAULT_RPM);
        builder.define(DRUM_ACTIVE,true);
        builder.define(LIGHTS,false);
        builder.define(BRAKING,false);
        builder.define(CARGO_UNITS,0);
        builder.define(CARGO_GRADE,-1);
    }

    /** Called by the verified network handler; never trust client cargo values. */
    public void acceptControls(ServerPlayer player, float throttle, float steer,
                               boolean toggleDrum, boolean toggleLights, boolean horn) {
        if (level().isClientSide() || !isDriver(player)) return;
        if (!Float.isFinite(throttle) || !Float.isFinite(steer)) return;
        this.throttle = Mth.clamp(throttle,-1F,1F);
        this.steer = Mth.clamp(steer,-1F,1F);
        this.freshInputTicks = 5;
        long now = level().getGameTime();
        if ((toggleDrum || toggleLights) && now - lastSwitchTick >= 3) {
            if (toggleDrum) drum.toggle();
            if (toggleLights) entityData.set(LIGHTS,!entityData.get(LIGHTS));
            lastSwitchTick = now;
            entityData.set(DRUM_ACTIVE,drum.rotating());
        }
        if (horn && now - lastHornTick >= 15) {
            lastHornTick = now;
            level().playSound(null,getX(),getY()+1,getZ(),
                    SoundEvents.NOTE_BLOCK_BASS,SoundSource.PLAYERS,2F,0.55F);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel)) return;

        Entity riding = getControllingPassenger();
        if (riding instanceof ServerPlayer player) {
            if (freshInputTicks > 0) freshInputTicks--;
            else {
                var input = player.getLastClientInput();
                throttle = input.forward() ? 1F : input.backward() ? -1F : 0F;
                steer = input.left() ? -1F : input.right() ? 1F : 0F;
            }
        } else {
            throttle = 0F;
            steer = 0F;
            freshInputTicks = 0;
        }

        boolean brakes = Math.abs(drive.speed()) > 0.015F
                && throttle * drive.speed() < -0.01F;
        MixerTruckDrive.Result step = drive.step(throttle,steer,brakes);
        if (Math.abs(step.yawDeltaDegrees()) > 0.0001F) {
            setYRot(Mth.wrapDegrees(getYRot()+step.yawDeltaDegrees()));
            setYBodyRot(getYRot());
            setYHeadRot(getYRot());
        }

        Vec3 forward = forwardVector();
        Vec3 impulse = new Vec3(forward.x*step.speed(),getDeltaMovement().y - 0.08D,
                forward.z*step.speed());
        setDeltaMovement(impulse);
        move(MoverType.SELF,impulse);
        setDeltaMovement(Vec3.ZERO);

        drum.tick();
        entityData.set(SPEED,step.speed());
        entityData.set(STEER,step.steerDegrees());
        entityData.set(WHEELS,step.wheelDegrees());
        entityData.set(DRUM_ANGLE,drum.angleDegrees());
        entityData.set(DRUM_RPM,drum.rpm());
        entityData.set(DRUM_ACTIVE,drum.rotating());
        entityData.set(BRAKING,brakes);
    }

    public int receiveFresh(ConcreteBatch incoming) {
        if (!(level() instanceof ServerLevel) || incoming == null
                || incoming.phase() != ConcreteBatch.Phase.FRESH) return 0;
        ConcreteBatchTank.Transfer result = tank.insert(incoming);
        if (result.acceptedUnits() > 0) syncCargo();
        return result.acceptedUnits();
    }

    public boolean canReceiveAt(Vec3 hopperWorldPoint) {
        if (!(level() instanceof ServerLevel) || tank.freeUnits() <= 0) return false;
        Vec3 intake = position().add(forwardVector().scale(-2.10D)).add(0,2.35D,0);
        return intake.distanceToSqr(hopperWorldPoint) <= 3.75D*3.75D;
    }

    private void syncCargo() {
        ConcreteBatch batch = tank.snapshot();
        entityData.set(CARGO_UNITS,batch == null ? 0 : batch.volumeUnits());
        entityData.set(CARGO_GRADE,batch == null ? -1 : batch.grade().ordinal());
    }

    /** Admin-controlled speed (Stage 5 will add cab RPM key controls). */
    public void setDrumRpm(float rpm) {
        if (!(level() instanceof ServerLevel)) return;
        drum.setRpm(rpm);
        entityData.set(DRUM_RPM, drum.rpm());
    }

    public ConcreteBatch cargoSnapshot() { return tank.snapshot(); }
    public int storedUnits() { return tank.storedUnits(); }
    public int freeUnits() { return tank.freeUnits(); }
    public float getForwardSpeed() { return entityData.get(SPEED); }
    public float getSteerAngle() { return entityData.get(STEER); }
    public float getWheelRotation() { return entityData.get(WHEELS); }
    public float getDrumAngle() { return entityData.get(DRUM_ANGLE); }
    public float getDrumRpm() { return entityData.get(DRUM_RPM); }
    public boolean drumSpinning() { return entityData.get(DRUM_ACTIVE); }
    public boolean lightsOn() { return entityData.get(LIGHTS); }
    public boolean braking() { return entityData.get(BRAKING); }
    public int displayedUnits() { return entityData.get(CARGO_UNITS); }
    public String displayedGrade() {
        int index = entityData.get(CARGO_GRADE);
        return index < 0 || index >= ConcreteGrade.values().length
                ? "Empty" : ConcreteGrade.values()[index].name();
    }

    public boolean isDriver(Entity entity) {
        return entity != null && entity == getControllingPassenger();
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity first = getFirstPassenger();
        return first instanceof LivingEntity living ? living : null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return passenger instanceof LivingEntity && getPassengers().isEmpty();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hit) {
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        if (player.isSecondaryUseActive()) {
            if (storedUnits() != 0 || !getPassengers().isEmpty()) {
                player.sendOverlayMessage(Component.literal("Empty truck before picking it up."));
                return InteractionResult.FAIL;
            }
            if (!player.getAbilities().instabuild) {
                player.getInventory().add(new ItemStack(ConcreteTruckRegistry.MIXER_TRUCK_ITEM));
            }
            discard();
            return InteractionResult.SUCCESS;
        }
        if (getPassengers().isEmpty()) {
            player.startRiding(this);
        } else {
            player.sendOverlayMessage(Component.literal(String.format(Locale.ROOT,
                    "Mixer truck %s: %d/%d units, drum %.1f RPM, lights %s",
                    displayedGrade(),storedUnits(),CAPACITY_UNITS,
                    getDrumRpm(),lightsOn() ? "ON" : "OFF")));
        }
        return InteractionResult.SUCCESS;
    }

    public Vec3 forwardVector() {
        double radians = Math.toRadians(getYRot());
        return new Vec3(-Math.sin(radians),0,Math.cos(radians));
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        return position().add(getPassengerAttachmentPoint(passenger,
                getDimensions(getPose()),1.0F));
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger,EntityDimensions dimensions,float scale) {
        Vec3 forward = forwardVector();
        Vec3 right = new Vec3(forward.z,0,-forward.x);
        return forward.scale(2.30D).add(right.scale(0.45D)).add(0,1.9D,0);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 forward = forwardVector();
        return position().add(forward.z*-2.3,0.2,forward.x*2.3);
    }

    @Override
    public boolean hurtClient(DamageSource source) { return true; }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        if (!(source.getEntity() instanceof Player player) || storedUnits() > 0
                || !getPassengers().isEmpty()) return false;
        if (!player.getAbilities().instabuild) {
            spawnAtLocation(world, ConcreteTruckRegistry.MIXER_TRUCK_ITEM);
        }
        discard();
        return true;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        drive.restore(input.getFloatOr("Speed",0F),
                input.getFloatOr("Steer",0F),input.getFloatOr("Wheels",0F));
        boolean rotating = input.getBooleanOr("Rotating",true);
        float rpm = input.getFloatOr("Rpm",MixerTruckDrum.DEFAULT_RPM);
        if (!Float.isFinite(rpm) || rpm < 0F || rpm > MixerTruckDrum.MAX_RPM)
            rpm = MixerTruckDrum.DEFAULT_RPM;
        drum.restore(rotating,rpm,input.getFloatOr("DrumAngle",0F));
        entityData.set(LIGHTS,input.getBooleanOr("Lights",false));

        if (input.getBooleanOr("HasCargo",false)) {
            try {
                ConcreteGrade grade = ConcreteGrade.parse(input.getStringOr("Grade",""));
                int units = input.getIntOr("Units",0);
                if (units < 1 || units > CAPACITY_UNITS) throw new IllegalArgumentException("Invalid units");
                IngredientAmounts a = new IngredientAmounts(
                        input.getLongOr("CementG",0L),input.getLongOr("SandG",0L),
                        input.getLongOr("GravelG",0L),input.getLongOr("WaterMl",0L));
                ConcreteBatch cargo = new ConcreteBatch(grade,units,a,ConcreteBatch.Phase.FRESH);
                if (tank.insert(cargo).acceptedUnits() != units)
                    throw new IllegalArgumentException("Cannot restore entire load");
            } catch (RuntimeException malformed) {
                GroundworksConcreteMod.LOGGER.warn("Bad mixer truck load at {}: {}",
                        blockPosition(),malformed.getMessage());
            }
        }
        syncCargo();
        entityData.set(DRUM_ANGLE,drum.angleDegrees());
        entityData.set(DRUM_RPM,drum.rpm());
        entityData.set(DRUM_ACTIVE,drum.rotating());
        entityData.set(SPEED,drive.speed());
        entityData.set(STEER,drive.steering());
        entityData.set(WHEELS,drive.wheelDegrees());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putFloat("Speed",drive.speed());
        output.putFloat("Steer",drive.steering());
        output.putFloat("Wheels",drive.wheelDegrees());
        output.putFloat("DrumAngle",drum.angleDegrees());
        output.putFloat("Rpm",drum.rpm());
        output.putBoolean("Rotating",drum.rotating());
        output.putBoolean("Lights",lightsOn());
        ConcreteBatch batch = tank.snapshot();
        output.putBoolean("HasCargo",batch != null);
        if (batch != null) {
            IngredientAmounts a = batch.ingredients();
            output.putString("Grade",batch.grade().name());
            output.putInt("Units",batch.volumeUnits());
            output.putLong("CementG",a.cementGrams());
            output.putLong("SandG",a.sandGrams());
            output.putLong("GravelG",a.gravelGrams());
            output.putLong("WaterMl",a.waterMillilitres());
        }
    }

    @Override
    public boolean isPickable() { return !isRemoved(); }
    @Override
    public boolean isAttackable() { return true; }
    @Override
    public boolean canBeCollidedWith(@Nullable Entity other) { return other != null && !hasPassenger(other); }
    @Override
    public boolean canCollideWith(Entity other) { return false; }
    @Override
    public boolean isClientAuthoritative() { return false; }
    @Override
    protected boolean isLocalClientAuthoritative() { return false; }
    @Override
    public float maxUpStep() { return 0.6F; }
}
