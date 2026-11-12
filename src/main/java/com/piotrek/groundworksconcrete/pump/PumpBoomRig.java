package com.piotrek.groundworksconcrete.pump;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Independent five-joint crane/boom mechanism; no Minecraft dependency.
 * Angles are relative to the previous section, positive is upward in the
 * mathematical model. Slew is horizontal turntable rotation.
 *
 * Each servo advances at most a fraction of a degree per server tick. A
 * candidate pose is tested before any real rig state is changed.
 */
public final class PumpBoomRig {
    public static final int SECTION_COUNT = 5;
    public static final double[] SECTION_LENGTHS = {10.0, 9.0, 9.0, 8.0, 8.0};
    public static final double MAX_REACH = 44.0;
    public static final double BOOM_BASE_HEIGHT = 3.85;
    public static final double BOOM_BASE_REAR_OFFSET = 0.75;
    public static final double JOINT_SPEED_DEG_PER_TICK = 0.60;
    public static final double SLEW_SPEED_DEG_PER_TICK = 0.42;
    public static final double MAX_SLEW = 180.0;

    private static final double[] HOME = {-5.0, 170.0, -170.0, 170.0, -170.0};
    private static final double[] MIN = {-12.0, -175.0, -175.0, -175.0, -175.0};
    private static final double[] MAX = {85.0, 175.0, 175.0, 175.0, 175.0};

    public record Point(double x, double y, double z) {
        public Point add(Point v) { return new Point(x + v.x, y + v.y, z + v.z); }
        public Point subtract(Point v) { return new Point(x - v.x, y - v.y, z - v.z); }
        public double distance(Point other) {
            double dx=x-other.x,dy=y-other.y,dz=z-other.z;
            return Math.sqrt(dx*dx+dy*dy+dz*dz);
        }
    }
    public record Segment(Point start, Point end, int section) {}
    public record Pose(List<Segment> segments, Point tip) {}

    private final double[] angles=HOME.clone();
    private final double[] targets=HOME.clone();
    private double slew, targetSlew;

    public double angle(int i) { checkJoint(i); return angles[i]; }
    public double target(int i) { checkJoint(i); return targets[i]; }
    public double slew() { return slew; }
    public double targetSlew() { return targetSlew; }
    public double[] angles() { return angles.clone(); }
    public double[] targets() { return targets.clone(); }

    public void requestJoint(int i,double degrees) {
        checkJoint(i);
        if (!Double.isFinite(degrees)) throw new IllegalArgumentException("Non-finite joint angle");
        targets[i]=Math.clamp(degrees,MIN[i],MAX[i]);
    }
    public void requestSlew(double degrees) {
        if (!Double.isFinite(degrees)) throw new IllegalArgumentException("Non-finite turntable angle");
        targetSlew=Math.clamp(degrees,-MAX_SLEW,MAX_SLEW);
    }
    public void stow() {
        System.arraycopy(HOME,0,targets,0,SECTION_COUNT);
        targetSlew=0.0;
    }
    /** Freeze actual hydraulic servo targets when emergency STOP is pressed. */
    public void halt() {
        System.arraycopy(angles,0,targets,0,SECTION_COUNT);
        targetSlew=slew;
    }
    public boolean isStowed() {
        if (Math.abs(slew)>1.0) return false;
        for(int i=0;i<SECTION_COUNT;i++) if(Math.abs(angles[i]-HOME[i])>1.0) return false;
        return true;
    }
    public boolean isMoving() {
        if(Math.abs(slew-targetSlew)>0.0001)return true;
        for(int i=0;i<SECTION_COUNT;i++) if(Math.abs(angles[i]-targets[i])>0.0001)return true;
        return false;
    }

    /** Called on the server only with all four outriggers locked and supported. */
    public boolean tick(double x,double y,double z,double truckYaw,
                        Predicate<Segment> segmentBlocked) {
        if(segmentBlocked==null)throw new IllegalArgumentException("Collision predicate missing");
        boolean changed=false;
        if(Math.abs(targetSlew-slew)>0.00001){
            double candidate=approach(slew,targetSlew,SLEW_SPEED_DEG_PER_TICK);
            if(!collides(pose(x,y,z,truckYaw,angles,candidate),segmentBlocked)){
                slew=candidate; changed=true;
            }
        }
        for(int i=0;i<SECTION_COUNT;i++){
            if(Math.abs(targets[i]-angles[i])<0.00001)continue;
            double next=approach(angles[i],targets[i],JOINT_SPEED_DEG_PER_TICK);
            double[] trial=angles.clone();
            trial[i]=next;
            if(!collides(pose(x,y,z,truckYaw,trial,slew),segmentBlocked)){
                angles[i]=next;changed=true;
            }
        }
        return changed;
    }

    public Pose pose(double x,double y,double z,double truckYaw) {
        return pose(x,y,z,truckYaw,angles,slew);
    }

    public static Pose pose(double x,double y,double z,double truckYaw,
                            double[] jointAngles,double slewDegrees) {
        if(jointAngles.length!=SECTION_COUNT)throw new IllegalArgumentException("Five joints required");
        double forwardX=-Math.sin(Math.toRadians(truckYaw));
        double forwardZ=Math.cos(Math.toRadians(truckYaw));
        double rightX=forwardZ,rightZ=-forwardX;
        double slewR=Math.toRadians(slewDegrees);
        double aimX=-forwardX*Math.cos(slewR)+rightX*Math.sin(slewR);
        double aimZ=-forwardZ*Math.cos(slewR)+rightZ*Math.sin(slewR);
        Point previous=new Point(x-forwardX*BOOM_BASE_REAR_OFFSET,
                y+BOOM_BASE_HEIGHT,z-forwardZ*BOOM_BASE_REAR_OFFSET);
        ArrayList<Segment> sections=new ArrayList<>(SECTION_COUNT);
        double totalPitch=0;
        for(int i=0;i<SECTION_COUNT;i++){
            totalPitch+=Math.toRadians(jointAngles[i]);
            double dx=SECTION_LENGTHS[i]*Math.cos(totalPitch);
            double dy=SECTION_LENGTHS[i]*Math.sin(totalPitch);
            Point next=previous.add(new Point(aimX*dx,dy,aimZ*dx));
            sections.add(new Segment(previous,next,i));
            previous=next;
        }
        return new Pose(List.copyOf(sections),previous);
    }

    /**
     * Limited iterative inverse kinematics for positioning the hose over a
     * looked-at foundation. Geometry is solved in the boom's vertical plane.
     * It changes actuator TARGETS only. Actual hydraulic motion still passes
     * per-tick world collision checks before any section is moved.
     */
    public boolean aimAt(Point destination,double x,double y,double z,double truckYaw) {
        if(destination==null || !Double.isFinite(destination.x())
                || !Double.isFinite(destination.y()) || !Double.isFinite(destination.z()))
            return false;
        Point base=pose(x,y,z,truckYaw).segments().get(0).start();
        double dx=destination.x()-base.x();
        double dz=destination.z()-base.z();
        double up=destination.y()-base.y();
        double radial=Math.hypot(dx,dz);
        if(Math.hypot(radial,up)>MAX_REACH+1.0D)return false;

        double yaw=Math.toRadians(truckYaw);
        double forwardX=-Math.sin(yaw),forwardZ=Math.cos(yaw);
        double rightX=forwardZ,rightZ=-forwardX;
        double rear=-(dx*forwardX+dz*forwardZ);
        double sideways=dx*rightX+dz*rightZ;
        double solvedSlew=Math.clamp(Math.toDegrees(Math.atan2(sideways,rear)),
                -MAX_SLEW,MAX_SLEW);

        double[][] starting={
                angles.clone(),
                {35,-20,20,-20,5},
                {50,-65,75,-55,20},
                {10,30,-50,30,-10},
                {65,-100,110,-80,45},
                {0,0,0,0,0}
        };
        double[] best=null;
        double bestScore=Double.POSITIVE_INFINITY;
        for(double[] init:starting){
            double[] trial=init.clone();
            for(double step:new double[]{45,20,10,4,1.5,0.5,0.2}){
                for(int pass=0;pass<40;pass++){
                    boolean improved=false;
                    for(int i=0;i<SECTION_COUNT;i++){
                        double original=trial[i];
                        double baseline=planeError(trial,radial,up);
                        double winning=original;
                        for(int direction:new int[]{-1,1}){
                            trial[i]=Math.clamp(original+direction*step,MIN[i],MAX[i]);
                            double error=planeError(trial,radial,up);
                            if(error+1.0e-9<baseline){
                                baseline=error;winning=trial[i];
                            }
                        }
                        trial[i]=winning;
                        if(Math.abs(original-winning)>1.0e-8)improved=true;
                    }
                    if(!improved)break;
                }
            }
            double score=planeError(trial,radial,up);
            if(score<bestScore){best=trial;bestScore=score;}
        }
        if(best==null || Math.sqrt(bestScore)>1.50D)return false;
        targetSlew=solvedSlew;
        System.arraycopy(best,0,targets,0,SECTION_COUNT);
        return true;
    }

    private static double planeError(double[] joints,double radial,double up){
        double elevation=0;
        double actualRadial=0,actualUp=0;
        for(int i=0;i<SECTION_COUNT;i++){
            elevation+=Math.toRadians(joints[i]);
            actualRadial+=SECTION_LENGTHS[i]*Math.cos(elevation);
            actualUp+=SECTION_LENGTHS[i]*Math.sin(elevation);
        }
        return Math.pow(actualRadial-radial,2)+Math.pow(actualUp-up,2);
    }

    private static boolean collides(Pose pose,Predicate<Segment> blocked) {
        for(Segment section:pose.segments()) if(blocked.test(section))return true;
        return false;
    }
    private static double approach(double a,double b,double delta) {
        if(a<b)return Math.min(b,a+delta);
        return Math.max(b,a-delta);
    }
    private static void checkJoint(int i){
        if(i<0||i>=SECTION_COUNT)throw new IllegalArgumentException("Joint must be 0..4");
    }

    /** Persistent restore only accepts finite, in-range values. Targets start stationary. */
    public void restore(double savedSlew,double[] savedAngles){
        if(savedAngles==null||savedAngles.length!=SECTION_COUNT||!Double.isFinite(savedSlew)
                ||Math.abs(savedSlew)>MAX_SLEW)throw new IllegalArgumentException("Invalid boom rig state");
        for(int i=0;i<SECTION_COUNT;i++){
            if(!Double.isFinite(savedAngles[i])||savedAngles[i]<MIN[i]||savedAngles[i]>MAX[i])
                throw new IllegalArgumentException("Invalid angle for joint "+i);
        }
        this.slew=targetSlew=savedSlew;
        System.arraycopy(savedAngles,0,angles,0,SECTION_COUNT);
        System.arraycopy(savedAngles,0,targets,0,SECTION_COUNT);
    }
}
