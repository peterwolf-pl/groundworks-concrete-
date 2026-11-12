package com.piotrek.groundworksconcrete.pump;

import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import com.piotrek.groundworksconcrete.material.ConcreteBatch;
import com.piotrek.groundworksconcrete.material.ConcreteBatchTank;
import com.piotrek.groundworksconcrete.material.ConcreteGrade;
import com.piotrek.groundworksconcrete.material.IngredientAmounts;
import com.piotrek.groundworksconcrete.vehicle.ConcreteTruckRegistry;
import com.piotrek.groundworksconcrete.vehicle.MixerTruckDrive;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Mobile 8x4 pump chassis, four independently actuated outriggers, five-section
 * hydraulic boom and rear receiving hopper. Pumping through the hose and the
 * handheld remote are reserved for Stage 7.
 *
 * Server owns every actuator, ground support check, collision check and cargo.
 * Only an authenticated seated driver may control this vehicle through packets.
 */
public final class ConcretePumpTruckEntity extends Entity {
    public static final int HOPPER_CAPACITY = 2 * ConcreteBatch.UNITS_PER_CUBIC_METRE;

    private static final EntityDataAccessor<Float> SPEED =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STEER =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WHEELS =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SLEW =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> JOINT0 =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> JOINT1 =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> JOINT2 =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> JOINT3 =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> JOINT4 =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LEG0 =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LEG1 =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LEG2 =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LEG3 =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LEG_DROP0 =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LEG_DROP1 =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LEG_DROP2 =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LEG_DROP3 =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> LEGS_REQUESTED =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LOCKED =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LIGHTS =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BRAKING =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> HOPPER_UNITS =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HOPPER_GRADE =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SELECTED_JOINT =
            SynchedEntityData.defineId(ConcretePumpTruckEntity.class,EntityDataSerializers.INT);

    private final MixerTruckDrive drive = new MixerTruckDrive();
    private final PumpBoomRig rig = new PumpBoomRig();
    private final PumpOutriggers outriggers = new PumpOutriggers();
    private final ConcreteBatchTank hopper = new ConcreteBatchTank(HOPPER_CAPACITY);

    private float throttle,steer,jointAxis,slewAxis;
    private int freshControls;
    private int selectedJoint;
    private long lastToggleTick = Long.MIN_VALUE / 2L;
    private long lastHornTick = Long.MIN_VALUE / 2L;

    public ConcretePumpTruckEntity(EntityType<?> type, Level level) { super(type,level); }

    @Override
    protected InterpolationHandler createInterpolationHandler() {
        return LinearInterpolationHandler.create(this,3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(SPEED,0F);b.define(STEER,0F);b.define(WHEELS,0F);
        b.define(SLEW,0F);
        b.define(JOINT0,-5F);b.define(JOINT1,170F);b.define(JOINT2,-170F);
        b.define(JOINT3,170F);b.define(JOINT4,-170F);
        b.define(LEG0,0F);b.define(LEG1,0F);b.define(LEG2,0F);b.define(LEG3,0F);
        b.define(LEG_DROP0,0F);b.define(LEG_DROP1,0F);
        b.define(LEG_DROP2,0F);b.define(LEG_DROP3,0F);
        b.define(LEGS_REQUESTED,false);b.define(LOCKED,false);
        b.define(LIGHTS,false);b.define(BRAKING,false);
        b.define(HOPPER_UNITS,0);b.define(HOPPER_GRADE,-1);b.define(SELECTED_JOINT,0);
    }

    public void acceptControls(ServerPlayer player,float throttle,float steer,
                               float slewAxis,float jointAxis,int joint,
                               boolean toggleLegs,boolean lights,boolean horn,boolean stow) {
        if(!(level() instanceof ServerLevel) || !isDriver(player)
                || !Float.isFinite(throttle) || !Float.isFinite(steer)
                || !Float.isFinite(slewAxis) || !Float.isFinite(jointAxis))return;
        this.throttle=Mth.clamp(throttle,-1F,1F);
        this.steer=Mth.clamp(steer,-1F,1F);
        this.slewAxis=Mth.clamp(slewAxis,-1F,1F);
        this.jointAxis=Mth.clamp(jointAxis,-1F,1F);
        this.selectedJoint=Mth.clamp(joint,0,PumpBoomRig.SECTION_COUNT-1);
        entityData.set(SELECTED_JOINT,selectedJoint);
        freshControls=5;
        long now=level().getGameTime();
        if((toggleLegs||lights||stow) && now-lastToggleTick>=3) {
            if(toggleLegs)requestOutriggers(!outriggers.deploymentRequested());
            if(lights)entityData.set(LIGHTS,!entityData.get(LIGHTS));
            if(stow)rig.stow();
            lastToggleTick=now;
        }
        if(horn && now-lastHornTick>=15) {
            lastHornTick=now;
            level().playSound(null,getX(),getY()+1,getZ(),
                    SoundEvents.NOTE_BLOCK_BASS,SoundSource.PLAYERS,2F,0.55F);
        }
    }

    /**
     * Hydraulic actuators never move with unsupported pads.
     * Retracting outriggers requires the boom in its verified stowed pose.
     */
    public boolean requestOutriggers(boolean deploy) {
        if(!(level() instanceof ServerLevel))return false;
        if(deploy) {
            if(Math.abs(drive.speed())>0.015F)return false;
        } else if(!rig.isStowed() || rig.isMoving()) {
            return false;
        }
        outriggers.requestDeploy(deploy);
        entityData.set(LEGS_REQUESTED,deploy);
        return true;
    }

    public boolean requestJoint(int joint,double degrees) {
        if(!(level() instanceof ServerLevel) || !isRigSupported())return false;
        rig.requestJoint(joint,degrees);
        return true;
    }

    public boolean requestSlew(double degrees) {
        if(!(level() instanceof ServerLevel) || !isRigSupported())return false;
        rig.requestSlew(degrees);
        return true;
    }

    public void stowBoom() {
        if(level() instanceof ServerLevel) rig.stow();
    }

    private boolean[] padSupport(ServerLevel world) {
        boolean[] result=new boolean[PumpOutriggers.COUNT];
        float[] groundDrop=new float[PumpOutriggers.COUNT];
        for(int i=0;i<PumpOutriggers.COUNT;i++) {
            PumpOutriggers.Foot foot=outriggers.foot(i,getX(),getY(),getZ(),getYRot());
            // A foot can accommodate up to two blocks of terrain elevation change.
            BlockPos top=BlockPos.containing(foot.x(),getY()-0.1D,foot.z());
            if(!world.hasChunkAt(top))continue;
            for(int depth=0;depth<=2;depth++) {
                BlockPos candidate=top.below(depth);
                if(!world.hasChunkAt(candidate))break;
                if(world.getBlockState(candidate).isFaceSturdy(world,candidate,Direction.UP)) {
                    result[i]=true;
                    groundDrop[i]=depth;
                    break;
                }
            }
        }
        entityData.set(LEG_DROP0,groundDrop[0]);
        entityData.set(LEG_DROP1,groundDrop[1]);
        entityData.set(LEG_DROP2,groundDrop[2]);
        entityData.set(LEG_DROP3,groundDrop[3]);
        return result;
    }

    public boolean isRigSupported() { return entityData.get(LOCKED); }
    public boolean outriggersRequested() { return entityData.get(LEGS_REQUESTED); }
    public boolean outriggersRetracted() { return outriggers.fullyRetracted(); }
    public boolean boomStowed() { return rig.isStowed(); }

    private boolean segmentBlocked(ServerLevel world,PumpBoomRig.Segment segment) {
        double length=segment.start().distance(segment.end());
        int samples=(int)Math.ceil(length/0.60D);
        for(int j=1;j<=samples;j++) {
            double t=j/(double)samples;
            double x=segment.start().x()+(segment.end().x()-segment.start().x())*t;
            double y=segment.start().y()+(segment.end().y()-segment.start().y())*t;
            double z=segment.start().z()+(segment.end().z()-segment.start().z())*t;
            if(y<getY()+0.2D)return true;
            // Ignore the machine's own chassis and fixed boom swivel.
            if(Math.hypot(x-getX(),z-getZ())<2.7D && y<getY()+5.1D)continue;
            BlockPos p=BlockPos.containing(x,y,z);
            if(!world.hasChunkAt(p))return true;
            if(!world.getBlockState(p).getCollisionShape(world,p).isEmpty())return true;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if(!(level() instanceof ServerLevel world))return;
        Entity passenger=getControllingPassenger();
        if(passenger instanceof ServerPlayer player) {
            if(freshControls>0)freshControls--;
            else {
                var input=player.getLastClientInput();
                throttle=input.forward()?1F:input.backward()?-1F:0F;
                steer=input.left()?-1F:input.right()?1F:0F;
                slewAxis=0; jointAxis=0;
            }
        } else {
            throttle=steer=slewAxis=jointAxis=0F;
            freshControls=0;
        }

        boolean parked=outriggers.fullyRetracted();
        MixerTruckDrive.Result motion=drive.step(parked?throttle:0F,
                parked?steer:0F,!parked);
        if(Math.abs(motion.yawDeltaDegrees())>0.0001F) {
            setYRot(Mth.wrapDegrees(getYRot()+motion.yawDeltaDegrees()));
            setYHeadRot(getYRot());setYBodyRot(getYRot());
        }
        if(parked) {
            double rad=Math.toRadians(getYRot());
            Vec3 v=new Vec3(-Math.sin(rad)*motion.speed(),
                    getDeltaMovement().y-0.08D,Math.cos(rad)*motion.speed());
            setDeltaMovement(v);
            move(MoverType.SELF,v);
            setDeltaMovement(Vec3.ZERO);
        } else {
            setDeltaMovement(Vec3.ZERO);
        }

        outriggers.tick();
        boolean supported=outriggers.locked(padSupport(world));
        entityData.set(LOCKED,supported);
        entityData.set(LEGS_REQUESTED,outriggers.deploymentRequested());

        if(supported) {
            if(Math.abs(jointAxis)>0.001F)
                rig.requestJoint(selectedJoint,rig.target(selectedJoint)+jointAxis*0.75D);
            if(Math.abs(slewAxis)>0.001F)
                rig.requestSlew(rig.targetSlew()+slewAxis*0.75D);
        }
        // Still allow safe folding of a previously extended boom when supports
        // remain locked; no movement is permitted when a foot loses support.
        if(supported)rig.tick(getX(),getY(),getZ(),getYRot(),
                section -> segmentBlocked(world,section));

        entityData.set(SPEED,motion.speed());
        entityData.set(STEER,motion.steerDegrees());
        entityData.set(WHEELS,motion.wheelDegrees());
        entityData.set(BRAKING,Math.abs(throttle)>0.001F && !parked);
        entityData.set(SLEW,(float)rig.slew());
        entityData.set(JOINT0,(float)rig.angle(0));entityData.set(JOINT1,(float)rig.angle(1));
        entityData.set(JOINT2,(float)rig.angle(2));entityData.set(JOINT3,(float)rig.angle(3));
        entityData.set(JOINT4,(float)rig.angle(4));
        entityData.set(LEG0,(float)outriggers.extension(0));
        entityData.set(LEG1,(float)outriggers.extension(1));
        entityData.set(LEG2,(float)outriggers.extension(2));
        entityData.set(LEG3,(float)outriggers.extension(3));
    }

    public PumpBoomRig.Pose boomPose() { return rig.pose(getX(),getY(),getZ(),getYRot()); }
    public int receiveConcrete(ConcreteBatch offered) {
        if(!(level() instanceof ServerLevel) || offered==null
                || offered.phase()!=ConcreteBatch.Phase.FRESH)return 0;
        int accepted=hopper.insert(offered).acceptedUnits();
        if(accepted>0)syncHopper();
        return accepted;
    }
    public ConcreteBatch hopperSnapshot(){return hopper.snapshot();}
    public int hopperUnits(){return hopper.storedUnits();}
    public int hopperCapacity(){return HOPPER_CAPACITY;}
    private void syncHopper(){
        ConcreteBatch batch=hopper.snapshot();
        entityData.set(HOPPER_UNITS,batch==null?0:batch.volumeUnits());
        entityData.set(HOPPER_GRADE,batch==null?-1:batch.grade().ordinal());
    }

    public boolean isDriver(Entity e){return e!=null && e==getControllingPassenger();}
    public float speed(){return entityData.get(SPEED);}
    public float steerAngle(){return entityData.get(STEER);}
    public float wheelAngle(){return entityData.get(WHEELS);}
    public float slewAngle(){return entityData.get(SLEW);}
    public float jointAngle(int i){
        return switch(i){
            case 0->entityData.get(JOINT0);case 1->entityData.get(JOINT1);
            case 2->entityData.get(JOINT2);case 3->entityData.get(JOINT3);
            case 4->entityData.get(JOINT4);
            default->throw new IllegalArgumentException("Joint 0..4");
        };
    }
    public float legExtension(int i){
        return switch(i){
            case 0->entityData.get(LEG0);case 1->entityData.get(LEG1);
            case 2->entityData.get(LEG2);case 3->entityData.get(LEG3);
            default->throw new IllegalArgumentException("Outrigger 0..3");
        };
    }
    public float legDrop(int i){
        return switch(i){
            case 0->entityData.get(LEG_DROP0);case 1->entityData.get(LEG_DROP1);
            case 2->entityData.get(LEG_DROP2);case 3->entityData.get(LEG_DROP3);
            default->throw new IllegalArgumentException("Outrigger 0..3");
        };
    }
    public int selectedJoint(){return entityData.get(SELECTED_JOINT);}
    public boolean lightsOn(){return entityData.get(LIGHTS);}
    public boolean braking(){return entityData.get(BRAKING);}
    public int displayedHopperUnits(){return entityData.get(HOPPER_UNITS);}
    public String hopperGrade(){
        int n=entityData.get(HOPPER_GRADE);
        return n>=0 && n<ConcreteGrade.values().length?ConcreteGrade.values()[n].name():"Empty";
    }

    @Override @Nullable
    public LivingEntity getControllingPassenger(){
        Entity e=getFirstPassenger();return e instanceof LivingEntity living?living:null;
    }
    @Override protected boolean canAddPassenger(Entity e){
        return e instanceof LivingEntity && getPassengers().isEmpty();
    }
    @Override
    public InteractionResult interact(Player player,InteractionHand hand,Vec3 hit){
        if(level().isClientSide())return InteractionResult.SUCCESS;
        if(player.isSecondaryUseActive()){
            if(!outriggers.fullyRetracted()||!rig.isStowed()
                    ||hopperUnits()>0||!getPassengers().isEmpty()){
                player.sendOverlayMessage(Component.literal(
                        "Stow boom, retract legs and empty hopper before pickup"));
                return InteractionResult.FAIL;
            }
            if(!player.getAbilities().instabuild)
                player.getInventory().add(new ItemStack(ConcreteTruckRegistry.PUMP_TRUCK_ITEM));
            discard();return InteractionResult.SUCCESS;
        }
        if(getPassengers().isEmpty())player.startRiding(this);
        else player.sendOverlayMessage(Component.literal(String.format(Locale.ROOT,
                "Pump: support %s, boom %s, hopper %d/1024 units",
                isRigSupported()?"LOCKED":"NOT LOCKED",boomStowed()?"STOWED":"DEPLOYED",
                hopperUnits())));
        return InteractionResult.SUCCESS;
    }
    public Vec3 forwardVector(){
        double yaw=Math.toRadians(getYRot());return new Vec3(-Math.sin(yaw),0,Math.cos(yaw));
    }
    @Override public Vec3 getPassengerRidingPosition(Entity e){
        return position().add(getPassengerAttachmentPoint(e,getDimensions(getPose()),1F));
    }
    @Override protected Vec3 getPassengerAttachmentPoint(Entity e,EntityDimensions d,float scale){
        Vec3 f=forwardVector();Vec3 right=new Vec3(f.z,0,-f.x);
        return f.scale(2.5).add(right.scale(0.42)).add(0,1.9,0);
    }
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity e){
        Vec3 f=forwardVector();return position().add(-f.z*2.4,0.2,f.x*2.4);
    }
    @Override public boolean hurtClient(DamageSource source){return true;}
    @Override
    public boolean hurtServer(ServerLevel level,DamageSource source,float amount){
        if(!(source.getEntity() instanceof Player player)
                ||hopperUnits()>0||!outriggers.fullyRetracted()
                ||!rig.isStowed()||!getPassengers().isEmpty())return false;
        if(!player.getAbilities().instabuild)
            spawnAtLocation(level,ConcreteTruckRegistry.PUMP_TRUCK_ITEM);
        discard();return true;
    }
    @Override
    protected void readAdditionalSaveData(ValueInput in){
        drive.restore(in.getFloatOr("Speed",0F),in.getFloatOr("Steer",0F),in.getFloatOr("Wheels",0F));
        entityData.set(LIGHTS,in.getBooleanOr("Lights",false));
        try {
            double[] joint=new double[5];
            for(int i=0;i<5;i++)joint[i]=in.getDoubleOr("BoomJoint"+i,new PumpBoomRig().angle(i));
            rig.restore(in.getDoubleOr("BoomSlew",0D),joint);
            double[] legs=new double[4];
            for(int i=0;i<4;i++)legs[i]=in.getDoubleOr("Leg"+i,0D);
            outriggers.restore(in.getBooleanOr("DeployRequested",false),legs);
        } catch(RuntimeException invalid){
            GroundworksConcreteMod.LOGGER.warn("Bad hydraulic state at {}: {}",
                    blockPosition(),invalid.getMessage());
            rig.stow();outriggers.requestDeploy(false);
        }
        if(in.getBooleanOr("HasHopper",false)){
            try {
                var grade=ConcreteGrade.parse(in.getStringOr("HopperGrade",""));
                int units=in.getIntOr("HopperUnits",0);
                if(units<=0||units>HOPPER_CAPACITY)throw new IllegalArgumentException("Bad hopper volume");
                var ingredients=new IngredientAmounts(
                        in.getLongOr("HopperCement",0),in.getLongOr("HopperSand",0),
                        in.getLongOr("HopperGravel",0),in.getLongOr("HopperWater",0));
                if(hopper.insert(new ConcreteBatch(grade,units,ingredients,
                        ConcreteBatch.Phase.FRESH)).acceptedUnits()!=units)
                    throw new IllegalArgumentException("Rejected saved hopper");
            } catch(RuntimeException invalid){
                GroundworksConcreteMod.LOGGER.warn("Bad pump hopper at {}: {}",
                        blockPosition(),invalid.getMessage());
            }
        }
        syncHopper();
        entityData.set(SPEED,drive.speed());
        entityData.set(STEER,drive.steering());
        entityData.set(WHEELS,drive.wheelDegrees());
        entityData.set(SLEW,(float)rig.slew());
        entityData.set(JOINT0,(float)rig.angle(0));entityData.set(JOINT1,(float)rig.angle(1));
        entityData.set(JOINT2,(float)rig.angle(2));entityData.set(JOINT3,(float)rig.angle(3));
        entityData.set(JOINT4,(float)rig.angle(4));
        entityData.set(LEG0,(float)outriggers.extension(0));entityData.set(LEG1,(float)outriggers.extension(1));
        entityData.set(LEG2,(float)outriggers.extension(2));entityData.set(LEG3,(float)outriggers.extension(3));
        entityData.set(LEGS_REQUESTED,outriggers.deploymentRequested());
        entityData.set(LOCKED,false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out){
        out.putFloat("Speed",drive.speed());
        out.putFloat("Steer",drive.steering());
        out.putFloat("Wheels",drive.wheelDegrees());
        out.putBoolean("Lights",lightsOn());
        out.putDouble("BoomSlew",rig.slew());
        for(int i=0;i<5;i++)out.putDouble("BoomJoint"+i,rig.angle(i));
        out.putBoolean("DeployRequested",outriggers.deploymentRequested());
        for(int i=0;i<4;i++)out.putDouble("Leg"+i,outriggers.extension(i));
        var batch=hopper.snapshot();
        out.putBoolean("HasHopper",batch!=null);
        if(batch!=null) {
            IngredientAmounts a=batch.ingredients();
            out.putString("HopperGrade",batch.grade().name());
            out.putInt("HopperUnits",batch.volumeUnits());
            out.putLong("HopperCement",a.cementGrams());
            out.putLong("HopperSand",a.sandGrams());
            out.putLong("HopperGravel",a.gravelGrams());
            out.putLong("HopperWater",a.waterMillilitres());
        }
    }
    @Override public boolean isPickable(){return !isRemoved();}
    @Override public boolean isAttackable(){return true;}
    @Override public boolean canBeCollidedWith(@Nullable Entity e){return e!=null&&!hasPassenger(e);}
    @Override public boolean canCollideWith(Entity e){return false;}
    @Override public boolean isClientAuthoritative(){return false;}
    @Override protected boolean isLocalClientAuthoritative(){return false;}
    @Override public float maxUpStep(){return 0.5F;}
}
