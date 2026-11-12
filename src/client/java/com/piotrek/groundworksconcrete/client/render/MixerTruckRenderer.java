package com.piotrek.groundworksconcrete.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.piotrek.groundworksconcrete.GroundworksConcreteMod;
import com.piotrek.groundworksconcrete.client.GroundworksConcreteClient;
import com.piotrek.groundworksconcrete.client.model.MixerTruckModel;
import com.piotrek.groundworksconcrete.vehicle.MixerTruckEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.resources.Identifier;

/** Multipart colored model with illuminated lenses and moving drum. */
public final class MixerTruckRenderer extends EntityRenderer<MixerTruckEntity, MixerTruckRenderState> {
    private static final int BRIGHT=0xF000F0;
    private static final UvMapping UV=new UvMapping(){
        @Override public float getU(float u){return u;}
        @Override public float getV(float v){return v;}
    };
    private final MixerTruckModel model;

    public MixerTruckRenderer(EntityRendererProvider.Context context) {
        super(context);
        model=new MixerTruckModel(context.bakeLayer(GroundworksConcreteClient.MIXER_TRUCK_LAYER));
        shadowRadius=2.3F;
    }

    @Override public MixerTruckRenderState createRenderState(){return new MixerTruckRenderState();}

    @Override
    public void extractRenderState(MixerTruckEntity entity, MixerTruckRenderState state,
                                   float partialTick){
        super.extractRenderState(entity,state,partialTick);
        state.yaw=entity.getYRot(partialTick);
        state.speed=entity.getForwardSpeed();
        state.steerAngle=entity.getSteerAngle();
        state.wheelRotation=entity.getWheelRotation();
        state.drumActive=entity.drumSpinning();
        state.drumAngle=entity.getDrumAngle()
                +(entity.drumSpinning()?partialTick*entity.getDrumRpm()*360F/1200F:0F);
        state.cargoUnits=entity.displayedUnits();
        state.lightsOn=entity.lightsOn();
        state.braking=entity.braking();
        state.beaconFlash=state.lightsOn && ((entity.tickCount/5)%2 == 0);
    }

    private static Identifier texture(String name){
        return Identifier.fromNamespaceAndPath(GroundworksConcreteMod.MOD_ID,
                "textures/entity/truck_"+name+".png");
    }

    private void draw(SubmitNodeCollector collector,PoseStack pose,
                      net.minecraft.client.model.geom.ModelPart part,String tex,int light){
        collector.submitModelPart(part,pose,RenderTypes.entityCutout(texture(tex)),light,
                OverlayTexture.NO_OVERLAY,UV);
    }

    @Override
    public void submit(MixerTruckRenderState state,PoseStack pose,
                       SubmitNodeCollector collector,CameraRenderState camera){
        pose.pushPose();
        pose.rotateDegrees(Axis.YP,-state.yaw);
        pose.scale(-1F,-1F,1F);
        pose.translate(0F,-1.5F,0F);

        model.setupAnim(state);
        int light=state.lightCoords;
        draw(collector,pose,model.chassis(),"chassis",light);
        draw(collector,pose,model.cab(),"cab",light);
        draw(collector,pose,model.windows(),"glass",light);
        draw(collector,pose,model.grille(),"chassis",light);
        draw(collector,pose,model.chute(),"steel",light);
        draw(collector,pose,model.wheels(),"tire",light);

        pose.pushPose();
        model.drumMount().translateAndRotate(pose);
        draw(collector,pose,model.drumShell(),"drum",light);
        draw(collector,pose,model.drumBands(),"stripe",light);
        draw(collector,pose,model.drumMetal(),"steel",light);
        pose.popPose();

        draw(collector,pose,model.headlights(),"headlight",state.lightsOn?BRIGHT:light);
        draw(collector,pose,model.taillights(),"taillight",state.lightsOn||state.braking?BRIGHT:light);
        if(state.beaconFlash)draw(collector,pose,model.beacons(),"beacon",BRIGHT);
        pose.popPose();
    }
}
