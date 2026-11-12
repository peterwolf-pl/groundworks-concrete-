package com.piotrek.groundworksconcrete.pump;

/**
 * Independently actuated four outrigger legs with collision/ground support
 * supplied by the server world adapter. Each leg moves gradually and locks
 * only if all four feet have stable support on loaded terrain.
 */
public final class PumpOutriggers {
    public static final int COUNT=4;
    public static final double EXTENSION_PER_TICK=0.025;
    public static final double SIDE_EXTENSION=3.6;
    public static final double FRONT_OFFSET=2.5;
    public static final double REAR_OFFSET=-2.7;

    private final double[] extension=new double[COUNT];
    private boolean deploymentRequested;

    public record Foot(double x,double y,double z) {}

    public void requestDeploy(boolean deploy) { deploymentRequested=deploy; }
    public boolean deploymentRequested(){return deploymentRequested;}
    public double extension(int leg){
        if(leg<0||leg>=COUNT)throw new IllegalArgumentException("Leg must be 0..3");
        return extension[leg];
    }
    public boolean fullyExtended(){
        for(double e:extension)if(e<0.999)return false;
        return true;
    }
    public boolean fullyRetracted(){
        for(double e:extension)if(e>0.001)return false;
        return true;
    }
    public boolean locked(boolean[] grounded){
        if(grounded==null||grounded.length!=COUNT)return false;
        if(!deploymentRequested||!fullyExtended())return false;
        for(boolean b:grounded)if(!b)return false;
        return true;
    }

    public void tick() {
        for(int i=0;i<COUNT;i++) {
            extension[i]=Math.clamp(extension[i]+
                    (deploymentRequested?EXTENSION_PER_TICK:-EXTENSION_PER_TICK),0.0,1.0);
        }
    }

    /** 0/1 front left/right, 2/3 rear left/right. */
    public Foot foot(int leg,double x,double y,double z,double truckYaw) {
        double yaw=Math.toRadians(truckYaw);
        double forwardX=-Math.sin(yaw),forwardZ=Math.cos(yaw);
        double rightX=forwardZ,rightZ=-forwardX;
        double longitudinal=leg<2?FRONT_OFFSET:REAR_OFFSET;
        double side=(leg%2==0?-1.0:1.0)*(1.15+SIDE_EXTENSION*extension(leg));
        return new Foot(x+longitudinal*forwardX+side*rightX,
                y-0.1,z+longitudinal*forwardZ+side*rightZ);
    }

    /** All four legs are serialized; no automatic deployment after reload. */
    public void restore(boolean requested,double[] values){
        if(values==null||values.length!=COUNT)throw new IllegalArgumentException("Need four legs");
        for(int i=0;i<COUNT;i++){
            if(!Double.isFinite(values[i])||values[i]<0||values[i]>1)
                throw new IllegalArgumentException("Invalid leg extension");
        }
        this.deploymentRequested=requested;
        System.arraycopy(values,0,extension,0,COUNT);
    }
}
