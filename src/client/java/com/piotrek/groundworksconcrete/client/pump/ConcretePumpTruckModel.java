package com.piotrek.groundworksconcrete.client.pump;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 8x4 high-detail low-poly truck, Everdigm-inspired folded teal boom,
 * rear concrete hopper, hydraulic actuators and four deployable outriggers.
 * Model coordinate convention: 16px per Minecraft block, +Z is truck front.
 */
public final class ConcretePumpTruckModel extends EntityModel<ConcretePumpRenderState> {
    private final ModelPart chassis,cab,glass,pumpUnit,hopper,lenses,beacon;
    private final ModelPart wheels,turntable,hydraulicBase;
    private final ModelPart[] tire=new ModelPart[8],steer=new ModelPart[4];
    private final ModelPart[] segments=new ModelPart[5],arm=new ModelPart[5],
            stripes=new ModelPart[5],hydraulics=new ModelPart[5];
    private final ModelPart[] outriggers=new ModelPart[4],feet=new ModelPart[4];

    public ConcretePumpTruckModel(ModelPart root){
        super(root);
        chassis=root.getChild("chassis");
        cab=root.getChild("cab");
        glass=root.getChild("glass");
        pumpUnit=root.getChild("pump_unit");
        hopper=root.getChild("hopper");
        lenses=root.getChild("lights");
        beacon=root.getChild("beacons");
        wheels=root.getChild("wheels");
        hydraulicBase=root.getChild("hydraulic_base");
        turntable=root.getChild("boom_swivel");

        for(int i=0;i<4;i++){
            for(int side=0;side<2;side++){
                ModelPart assembly=wheels.getChild("wheel"+i+"_"+side);
                tire[i*2+side]=assembly.getChild("tire");
                if(i<2)steer[i*2+side]=assembly;
            }
        }
        ModelPart parent=turntable;
        for(int i=0;i<5;i++){
            segments[i]=parent.getChild("boom_section"+i);
            arm[i]=segments[i].getChild("arm");
            stripes[i]=segments[i].getChild("stripe");
            hydraulics[i]=segments[i].getChild("hydraulic");
            parent=segments[i];
        }
        for(int i=0;i<4;i++){
            outriggers[i]=root.getChild("stabilizer"+i);
            feet[i]=outriggers[i].getChild("foot");
        }
    }

    public ModelPart chassis(){return chassis;}
    public ModelPart cab(){return cab;}
    public ModelPart glass(){return glass;}
    public ModelPart pumpUnit(){return pumpUnit;}
    public ModelPart hopper(){return hopper;}
    public ModelPart lights(){return lenses;}
    public ModelPart beacons(){return beacon;}
    public ModelPart wheels(){return wheels;}
    public ModelPart hydraulicBase(){return hydraulicBase;}
    public ModelPart turntable(){return turntable;}
    public ModelPart swivelBody(){return turntable.getChild("body");}
    public ModelPart hose(){return segments[4].getChild("hose");}
    public ModelPart section(int i){return segments[i];}
    public ModelPart arm(int i){return arm[i];}
    public ModelPart stripe(int i){return stripes[i];}
    public ModelPart hydraulic(int i){return hydraulics[i];}
    public ModelPart stabilizer(int i){return outriggers[i];}

    @Override
    public void setupAnim(ConcretePumpRenderState state){
        float rotation=(float)Math.toRadians(-state.wheelRotation);
        for(ModelPart t:tire)t.xRot=rotation;
        float steerAngle=(float)Math.toRadians(state.steerAngle);
        for(ModelPart t:steer)t.yRot=steerAngle;
        turntable.yRot=(float)Math.toRadians(-state.boomSlew);
        for(int i=0;i<5;i++)segments[i].xRot=(float)Math.toRadians(-state.joints[i]);

        for(int i=0;i<4;i++){
            float extension=state.legExtensions[i];
            // Animated telescoping outriggers move outwards; jack/pad lowers.
            float sign=i%2==0?-1F:1F;
            feet[i].x=sign*(16F+extension*58F);
            // Horizontal beam first, then a vertically adjustable jack.
            // An uneven ground level of up to two full blocks is represented.
            feet[i].y=2F+extension*(6F+16F*state.legDrops[i]);
            feet[i].visible=extension>0.02F;
        }
        beacon.visible=state.lights&&state.beaconFlash;
    }

    private static CubeListBuilder cb(){return CubeListBuilder.create().texOffs(0,0);}
    public static LayerDefinition createBodyLayer(){
        MeshDefinition mesh=new MeshDefinition();
        PartDefinition root=mesh.getRoot();

        root.addOrReplaceChild("chassis",cb()
                .addBox(-10F,4F,-65F,20F,7F,126F)
                .addBox(-16F,7F,52F,32F,4F,6F)
                .addBox(-16F,7F,-64F,32F,4F,5F)
                .addBox(-19F,5F,-25F,38F,4F,30F)
                .addBox(-18F,1F,-50F,36F,4F,15F)
                .addBox(-14F,0F,20F,28F,8F,37F),PartPose.ZERO);
        root.addOrReplaceChild("cab",cb()
                .addBox(-16F,-8F,20F,32F,16F,38F)
                .addBox(-16F,-36F,20F,32F,5F,37F)
                .addBox(-16F,-35F,20F,3F,28F,4F)
                .addBox(13F,-35F,20F,3F,28F,4F)
                .addBox(-16F,-35F,53F,3F,28F,4F)
                .addBox(13F,-35F,53F,3F,28F,4F)
                .addBox(-15F,-11F,56F,30F,10F,3F)
                .addBox(-13F,-16F,25F,26F,5F,18F),PartPose.ZERO);
        root.addOrReplaceChild("glass",cb()
                .addBox(-13F,-32F,56F,26F,18F,1F)
                .addBox(-17F,-31F,28F,1F,18F,20F)
                .addBox(16F,-31F,28F,1F,18F,20F)
                .addBox(-12F,-18F,20F,24F,6F,1F),PartPose.ZERO);

        root.addOrReplaceChild("pump_unit",cb()
                .addBox(-17F,-9F,-57F,34F,16F,45F)
                .addBox(-18F,-12F,-30F,36F,10F,18F)
                .addBox(-16F,-16F,-10F,32F,13F,27F)
                .addBox(-11F,-21F,-21F,22F,9F,17F)
                .addBox(-14F,-3F,-63F,28F,14F,8F),PartPose.ZERO);
        root.addOrReplaceChild("hopper",cb()
                .addBox(-19F,-17F,-64F,38F,5F,7F)
                .addBox(-20F,-13F,-65F,4F,17F,13F)
                .addBox(16F,-13F,-65F,4F,17F,13F)
                .addBox(-17F,-13F,-56F,34F,13F,3F)
                .addBox(-17F,-8F,-64F,34F,3F,11F),PartPose.ZERO);

        root.addOrReplaceChild("hydraulic_base",cb()
                .addBox(-14F,-58F,-25F,28F,16F,24F)
                .addBox(-20F,-41F,-22F,40F,14F,18F)
                .addBox(-6F,-61F,-24F,12F,6F,12F),PartPose.ZERO);
        PartDefinition swivel=root.addOrReplaceChild("boom_swivel",
                CubeListBuilder.create(),PartPose.offset(0F,-62F,-12F));
        swivel.addOrReplaceChild("body",cb()
                .addBox(-14F,-5F,-11F,28F,11F,22F),PartPose.ZERO);
        double[] lengths={10,9,9,8,8};
        double[] home={-5,170,-170,170,-170};
        PartDefinition parent=swivel;
        for(int i=0;i<lengths.length;i++){
            final int len=(int)lengths[i]*16;
            String name="boom_section"+i;
            PartDefinition section=parent.addOrReplaceChild(name,CubeListBuilder.create(),
                    i==0 ? PartPose.offsetAndRotation(0F,0F,0F,0F,0F,0F)
                            : PartPose.offset(0F,0F,-(int)lengths[i-1]*16));
            section.addOrReplaceChild("arm",cb()
                    .addBox(-7F,-5F,-len,14F,10F,len)
                    .addBox(-9F,-8F,-12F,18F,16F,14F)
                    .addBox(-6F,-4F,-len,12F,8F,4F),PartPose.ZERO);
            section.addOrReplaceChild("stripe",cb()
                    .addBox(-8F,-6F,-len*0.76F,16F,3F,Math.min(15F,len*0.25F))
                    .addBox(-8F,-6F,-len*0.27F,16F,3F,Math.min(12F,len*0.14F)),
                    PartPose.ZERO);
            section.addOrReplaceChild("hydraulic",cb()
                    .addBox(-12F,6F,-Math.min(90F,len*0.50F),
                            5F,6F,Math.min(80F,len*0.47F))
                    .addBox(7F,6F,-Math.min(90F,len*0.50F),
                            5F,6F,Math.min(80F,len*0.47F))
                    .addBox(-10F,6F,-7F,20F,5F,12F),PartPose.ZERO);
            parent=section;
        }
        // Five articulations form a nested hierarchy; the distal flexible hose
        // will become functional in Stage 7.
        parent.addOrReplaceChild("hose",cb()
                .addBox(-4F,-2F,-12F,8F,8F,12F)
                .addBox(-3F,6F,-8F,6F,20F,6F),PartPose.offset(0F,0F,-8*16F));

        PartDefinition wheels=root.addOrReplaceChild("wheels",CubeListBuilder.create(),PartPose.ZERO);
        final float[] z={45F,18F,-28F,-56F};
        for(int i=0;i<4;i++)for(int side=0;side<2;side++){
            float x=side==0?-20F:20F;
            PartDefinition wheel=wheels.addOrReplaceChild("wheel"+i+"_"+side,
                    CubeListBuilder.create(),PartPose.offset(x,9F,z[i]));
            wheel.addOrReplaceChild("tire",cb()
                    .addBox(-5F,-10F,-10F,10F,20F,20F),PartPose.ZERO);
        }

        for(int i=0;i<4;i++) {
            float x=i%2==0?-17F:17F;
            float zBase=i<2?37F:-43F;
            float sign=i%2==0?-1F:1F;
            PartDefinition support=root.addOrReplaceChild("stabilizer"+i,cb()
                    .addBox(sign<0?-30F:0F,-4F,-6F,30F,8F,12F),
                    PartPose.offset(x,3F,zBase));
            support.addOrReplaceChild("foot",cb()
                    .addBox(-7F,-8F,-7F,14F,16F,14F)
                    .addBox(-12F,7F,-12F,24F,3F,24F)
                    .addBox(-5F,-15F,-5F,10F,9F,10F),
                    PartPose.ZERO);
        }
        root.addOrReplaceChild("lights",cb()
                .addBox(-15F,0F,59F,8F,6F,2F)
                .addBox(7F,0F,59F,8F,6F,2F)
                .addBox(-18F,1F,-68F,8F,6F,2F)
                .addBox(10F,1F,-68F,8F,6F,2F),PartPose.ZERO);
        root.addOrReplaceChild("beacons",cb()
                .addBox(-13F,-41F,37F,5F,5F,5F)
                .addBox(8F,-41F,37F,5F,5F,5F),PartPose.ZERO);
        return LayerDefinition.create(mesh,16,16);
    }
}
