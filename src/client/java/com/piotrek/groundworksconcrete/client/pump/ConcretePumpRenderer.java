package com.piotrek.groundworksconcrete.client.pump;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import com.piotrek.groundworksconcrete.client.GroundworksConcreteClient;
import com.piotrek.groundworksconcrete.pump.ConcretePumpTruckEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Independently animated boom hierarchy and hydraulic outrigger models.
 * Reuses the compatible Groundworks Concrete cab/steel/teal/orange textures.
 */
public final class ConcretePumpRenderer
        extends EntityRenderer<ConcretePumpTruckEntity,ConcretePumpRenderState> {
    private static final int EMISSIVE=0xF000F0;
    private static final UvMapping UV=new UvMapping() {
        @Override public float getU(float u){return u;}
        @Override public float getV(float v){return v;}
    };
    private final ConcretePumpTruckModel model;

    public ConcretePumpRenderer(EntityRendererProvider.Context context){
        super(context);
        model=new ConcretePumpTruckModel(context.bakeLayer(
                GroundworksConcreteClient.PUMP_TRUCK_LAYER));
        shadowRadius=2.5F;
    }

    @Override
    public ConcretePumpRenderState createRenderState(){return new ConcretePumpRenderState();}

    @Override
    public void extractRenderState(ConcretePumpTruckEntity entity,
                                   ConcretePumpRenderState state,float partialTick){
        super.extractRenderState(entity,state,partialTick);
        state.yaw=entity.getYRot(partialTick);
        state.forwardSpeed=entity.speed();
        state.steerAngle=entity.steerAngle();
        state.wheelRotation=entity.wheelAngle();
        state.boomSlew=entity.slewAngle();
        for(int i=0;i<5;i++)state.joints[i]=entity.jointAngle(i);
        for(int i=0;i<4;i++){
            state.legExtensions[i]=entity.legExtension(i);
            state.legDrops[i]=entity.legDrop(i);
        }
        state.deployed=entity.outriggersRequested();
        state.supported=entity.isRigSupported();
        state.lights=entity.lightsOn();
        state.braking=entity.braking();
        state.hopperUnits=entity.displayedHopperUnits();
        state.selectedJoint=entity.selectedJoint();
        state.beaconFlash=(entity.tickCount/5)%2==0;
    }

    private static Identifier tex(String color){
        return Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID,
                "textures/entity/truck_"+color+".png");
    }

    private static void draw(SubmitNodeCollector collector,PoseStack pose,
                             ModelPart part,String texture,int light){
        collector.submitModelPart(part,pose,RenderTypes.entityCutout(tex(texture)),light,
                OverlayTexture.NO_OVERLAY,UV);
    }

    @Override
    public void submit(ConcretePumpRenderState state,PoseStack pose,
                       SubmitNodeCollector collector,CameraRenderState camera){
        pose.pushPose();
        pose.rotateDegrees(Axis.YP,-state.yaw);
        pose.scale(-1F,-1F,1F);
        pose.translate(0,-1.5,0);
        model.setupAnim(state);
        int light=state.lightCoords;

        draw(collector,pose,model.chassis(),"chassis",light);
        draw(collector,pose,model.cab(),"cab",light);
        draw(collector,pose,model.glass(),"glass",light);
        draw(collector,pose,model.pumpUnit(),"cab",light);
        draw(collector,pose,model.hopper(),"steel",light);
        draw(collector,pose,model.hydraulicBase(),"chassis",light);
        draw(collector,pose,model.wheels(),"tire",light);

        for(int leg=0;leg<4;leg++){
            draw(collector,pose,model.stabilizer(leg),"steel",light);
        }

        pose.pushPose();
        model.turntable().translateAndRotate(pose);
        draw(collector,pose,model.swivelBody(),"steel",light);
        // A five-level parent/child hierarchy preserves each real joint pivot:
        // each section receives accumulated transform of all earlier sections.
        for(int i=0;i<5;i++){
            model.section(i).translateAndRotate(pose);
            draw(collector,pose,model.arm(i),"drum",light);
            draw(collector,pose,model.stripe(i),"stripe",light);
            draw(collector,pose,model.hydraulic(i),"steel",light);
        }
        draw(collector,pose,model.hose(),"tire",light);
        pose.popPose();

        draw(collector,pose,model.lights(),"headlight",state.lights?EMISSIVE:light);
        if(state.lights&&state.beaconFlash)
            draw(collector,pose,model.beacons(),"beacon",EMISSIVE);
        pose.popPose();
    }
}
