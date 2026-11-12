package com.piotrek.groundworksconcrete.client.model;

import com.piotrek.groundworksconcrete.client.render.MixerTruckRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Custom low-poly four-axle rigid mixer truck.
 * A twelve-facet barrel rotates as a hierarchy around its longitudinal axis.
 * Each color group is submitted with a separately authored 16x16 texture.
 */
public final class MixerTruckModel extends EntityModel<MixerTruckRenderState> {
    private final ModelPart chassis, cab, windows, grille, wheels, drumMount;
    private final ModelPart drumShell, drumBands, drumMetal, rearChute;
    private final ModelPart headlamps, tailLamps, beacon;
    private final ModelPart[] steerers = new ModelPart[4];
    private final ModelPart[] tires = new ModelPart[8];

    public MixerTruckModel(ModelPart root) {
        super(root);
        chassis=root.getChild("chassis");
        cab=root.getChild("cab");
        windows=root.getChild("windows");
        grille=root.getChild("grille");
        rearChute=root.getChild("chute");
        headlamps=root.getChild("headlights");
        tailLamps=root.getChild("taillights");
        beacon=root.getChild("beacons");
        wheels=root.getChild("wheels");
        drumMount=root.getChild("drum_mount");
        drumShell=drumMount.getChild("shell");
        drumBands=drumMount.getChild("bands");
        drumMetal=drumMount.getChild("metal");
        for(int axle=0;axle<4;axle++){
            for(int side=0;side<2;side++){
                String name="axle"+axle+"_"+(side==0?"left":"right");
                ModelPart steer=wheels.getChild(name);
                tires[axle*2+side]=steer.getChild("tire");
                if(axle<2)steerers[axle*2+side]=steer;
            }
        }
    }

    public ModelPart chassis() {return chassis;}
    public ModelPart cab() {return cab;}
    public ModelPart windows() {return windows;}
    public ModelPart grille() {return grille;}
    public ModelPart wheels() {return wheels;}
    public ModelPart drumMount() {return drumMount;}
    public ModelPart drumShell() {return drumShell;}
    public ModelPart drumBands() {return drumBands;}
    public ModelPart drumMetal() {return drumMetal;}
    public ModelPart chute() {return rearChute;}
    public ModelPart headlights() {return headlamps;}
    public ModelPart taillights() {return tailLamps;}
    public ModelPart beacons() {return beacon;}

    @Override
    public void setupAnim(MixerTruckRenderState state) {
        float wheel = (float)Math.toRadians(-state.wheelRotation);
        for(ModelPart tire:tires)tire.xRot=wheel;
        float steer = (float)Math.toRadians(state.steerAngle);
        for(ModelPart front:steerers)front.yRot=steer;
        float drum = (float)Math.toRadians(state.drumAngle);
        drumShell.zRot=drum;
        drumBands.zRot=drum;
        drumMetal.zRot=drum;
        beacon.visible=state.beaconFlash;
    }

    private static void part(PartDefinition root,String name,CubeListBuilder cubes,PartPose pose) {
        root.addOrReplaceChild(name,cubes,pose);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh=new MeshDefinition();
        PartDefinition root=mesh.getRoot();

        part(root,"chassis",CubeListBuilder.create()
                .texOffs(0,0).addBox(-9F,5F,-63F,18F,7F,115F)
                .texOffs(0,0).addBox(-16F,7F,47F,32F,5F,5F)
                .texOffs(0,0).addBox(-16F,7F,-65F,32F,5F,5F)
                .texOffs(0,0).addBox(-12F,1F,-48F,24F,5F,68F)
                .texOffs(0,0).addBox(-20F,0F,-59F,7F,2F,46F)
                .texOffs(0,0).addBox(13F,0F,-59F,7F,2F,46F)
                .texOffs(0,0).addBox(-16F,0F,11F,7F,8F,18F)
                .texOffs(0,0).addBox(9F,0F,11F,7F,8F,18F),PartPose.ZERO);

        // Hollow cab: A-pillars, door skins, roof, dash, seats, bumper.
        part(root,"cab",CubeListBuilder.create()
                .texOffs(0,0).addBox(-16F,-6F,18F,32F,13F,32F)
                .texOffs(0,0).addBox(-16F,-34F,19F,32F,5F,30F)
                .texOffs(0,0).addBox(-16F,-33F,19F,3F,28F,3F)
                .texOffs(0,0).addBox(13F,-33F,19F,3F,28F,3F)
                .texOffs(0,0).addBox(-16F,-33F,46F,3F,28F,3F)
                .texOffs(0,0).addBox(13F,-33F,46F,3F,28F,3F)
                .texOffs(0,0).addBox(-14F,-8F,18F,28F,5F,30F)
                .texOffs(0,0).addBox(-15F,-10F,47F,30F,3F,4F)
                .texOffs(0,0).addBox(-12F,-16F,28F,24F,4F,13F)
                .texOffs(0,0).addBox(-15F,0F,49F,30F,10F,4F),PartPose.ZERO);

        part(root,"windows",CubeListBuilder.create()
                .texOffs(0,0).addBox(-13F,-29F,48F,26F,18F,1F)
                .texOffs(0,0).addBox(-16.5F,-29F,25F,1F,17F,19F)
                .texOffs(0,0).addBox(15.5F,-29F,25F,1F,17F,19F)
                .texOffs(0,0).addBox(-11F,-14F,19F,22F,4F,1F),PartPose.ZERO);

        part(root,"grille",CubeListBuilder.create()
                .texOffs(0,0).addBox(-11F,-5F,52F,22F,12F,1.5F)
                .texOffs(0,0).addBox(-14F,8F,52F,28F,4F,2F)
                .texOffs(0,0).addBox(-12F,13F,42F,6F,4F,4F)
                .texOffs(0,0).addBox(6F,13F,42F,6F,4F,4F),PartPose.ZERO);

        part(root,"chute",CubeListBuilder.create()
                .texOffs(0,0).addBox(-12F,-9F,-69F,24F,3F,12F)
                .texOffs(0,0).addBox(-11F,-6F,-75F,22F,2F,20F)
                .texOffs(0,0).addBox(-15F,-30F,-61F,3F,24F,3F)
                .texOffs(0,0).addBox(-14F,-32F,-61F,26F,2F,3F)
                .texOffs(0,0).addBox(11F,-29F,-61F,2F,24F,3F)
                .texOffs(0,0).addBox(-11F,-24F,-64F,22F,4F,3F),PartPose.ZERO);

        // Real moving model part, tilted in X and independently rolled about Z.
        PartDefinition mount=root.addOrReplaceChild("drum_mount",
                CubeListBuilder.create(),PartPose.offsetAndRotation(0F,-15F,-19F,-0.16F,0F,0F));
        PartDefinition shell=mount.addOrReplaceChild("shell",
                CubeListBuilder.create(),PartPose.ZERO);
        PartDefinition bands=mount.addOrReplaceChild("bands",
                CubeListBuilder.create(),PartPose.ZERO);
        PartDefinition metal=mount.addOrReplaceChild("metal",
                CubeListBuilder.create(),PartPose.ZERO);

        final int facets=12;
        final float radius=18.0F;
        final float width=(float)(2*Math.PI*radius/facets)+0.5F;
        for(int n=0;n<facets;n++){
            float a=(float)(2*Math.PI*n/facets);
            float x=(float)Math.sin(a)*radius;
            float y=(float)-Math.cos(a)*radius;
            PartPose pose=PartPose.offsetAndRotation(x,y,0,0,0,a);
            shell.addOrReplaceChild("shell"+n,
                    CubeListBuilder.create().texOffs(0,0)
                            .addBox(-width/2F,-1.8F,-26F,width,3.6F,52F),pose);
            bands.addOrReplaceChild("band"+n,
                    CubeListBuilder.create().texOffs(0,0)
                            .addBox(-width/2F,-2.0F,-16F,width,4.0F,6.0F)
                            .addBox(-width/2F,-2.0F,11F,width,4.0F,6.0F),pose);
        }
        metal.addOrReplaceChild("neck",CubeListBuilder.create().texOffs(0,0)
                .addBox(-8F,-8F,-31F,16F,16F,7F)
                .addBox(-9F,-9F,23F,18F,18F,6F),PartPose.ZERO);

        PartDefinition wheels=root.addOrReplaceChild("wheels",CubeListBuilder.create(),PartPose.ZERO);
        float[] positions={38F,12F,-32F,-55F};
        for(int axle=0;axle<4;axle++){
            for(int side=0;side<2;side++){
                float x=side==0?-18F:18F;
                String name="axle"+axle+"_"+(side==0?"left":"right");
                PartDefinition axleRoot=wheels.addOrReplaceChild(name,CubeListBuilder.create(),
                        PartPose.offset(x,8F,positions[axle]));
                axleRoot.addOrReplaceChild("tire",CubeListBuilder.create().texOffs(0,0)
                        .addBox(-4F,-9F,-9F,8F,18F,18F),PartPose.ZERO);
            }
        }

        part(root,"headlights",CubeListBuilder.create().texOffs(0,0)
                .addBox(-14F,0F,54F,8F,5F,1F).addBox(6F,0F,54F,8F,5F,1F),PartPose.ZERO);
        part(root,"taillights",CubeListBuilder.create().texOffs(0,0)
                .addBox(-17F,1F,-67F,7F,5F,1F)
                .addBox(10F,1F,-67F,7F,5F,1F),PartPose.ZERO);
        part(root,"beacons",CubeListBuilder.create().texOffs(0,0)
                .addBox(-13F,-39F,34F,5F,5F,5F)
                .addBox(8F,-39F,34F,5F,5F,5F),PartPose.ZERO);

        return LayerDefinition.create(mesh,16,16);
    }
}
